package com.yinhai.mids.business.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.analysis.*;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.AnalysisService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author zhuhs
 * @date 2024/7/15 15:45
 */
@Service
@TaTransactional
public class AnalysisServiceImpl implements AnalysisService {

    private static final Log log = LogFactory.get();

    @Resource
    private SeriesComputeMapper seriesComputeMapper;

    @Resource
    private InstanceMapper instanceMapper;

    @Resource
    private StudyMapper studyMapper;

    @Resource
    private AnalysisOverviewMapper analysisOverviewMapper;

    @Resource
    private VolumeDetailMapper volumeDetailMapper;

    @Resource
    private DetailAnnotationMapper detailAnnotationMapper;

    @Resource
    private ITaFSManager<FSManager> fsManager;

    @Resource
    private KeyaClient keyaClient;

    @Resource
    private KeyaProperties keyaProperties;

    @SuppressWarnings("unchecked")
    @Override
    @Async("asyncJobExecutorService")
    public void register(String seriesComputeId) {
        SeriesComputePO seriesComputePO = seriesComputeMapper.selectById(seriesComputeId);
        if (seriesComputePO == null) {
            log.error("计算序列{}不存在", seriesComputeId);
            return;
        }

        StudyPO studyPO = studyMapper.selectById(seriesComputePO.getStudyId());
        if (studyPO == null) {
            log.error("计算序列{}对应检查不存在", seriesComputeId);
            seriesComputeMapper.updateById(new SeriesComputePO().setId(seriesComputeId)
                    .setComputeStatus(ComputeStatus.COMPUTE_ERROR)
                    .setErrorMessage(StrUtil.format("计算序列{}对应检查不存在", seriesComputeId)));
            return;
        }

        LambdaQueryWrapper<InstancePO> queryWrapper = Wrappers.<InstancePO>lambdaQuery()
                .select(InstancePO::getAccessPath, InstancePO::getSopInstanceUid)
                .eq(InstancePO::getSeriesId, seriesComputePO.getSeriesId());
        List<InstancePO> instancePOList = instanceMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(instancePOList)) {
            log.error("计算序列{}对应实例不存在", seriesComputeId);
            seriesComputeMapper.updateById(new SeriesComputePO().setId(seriesComputeId)
                    .setComputeStatus(ComputeStatus.COMPUTE_ERROR)
                    .setErrorMessage(StrUtil.format("计算序列{}对应实例不存在", seriesComputeId)));
            return;
        }

        RegisterParam registerParam = new RegisterParam();
        String applyId = IdUtil.fastSimpleUUID();
        registerParam.setApplyId(applyId);
        registerParam.setAccessionNumber(studyPO.getAccessionNumber());
        registerParam.setStudyInstanceUID(studyPO.getStudyInstanceUid());
        registerParam.setHospitalId("ctbay99");
        registerParam.setExaminedName("胸部平扫");
        registerParam.setPatientName(studyPO.getPatientName());
        registerParam.setPatientAge(studyPO.getPatientAge());
        registerParam.setStudyDate(DateUtil.formatDateTime(studyPO.getStudyDateAndTime()));
        registerParam.setCallbackUrl(keyaProperties.getPushCallbackUrl());

        KeyaResponse response = null;
        try (InputStream inputStream = readDicomFromFSAndZip(instancePOList)) {
            response = keyaClient.register(keyaProperties.getRegisterUrl(), inputStream, registerParam);
            if (response.getCode() == 1) {
                seriesComputeMapper.updateById(new SeriesComputePO().setId(seriesComputeId)
                        .setApplyId(applyId)
                        .setComputeStatus(ComputeStatus.IN_COMPUTE)
                        .setComputeStartTime(MapperKit.executeForDate())
                        .setComputeResponse(JsonKit.toJsonString(response)));
            } else {
                seriesComputeMapper.updateById(new SeriesComputePO().setId(seriesComputeId)
                        .setErrorMessage("申请AI分析失败")
                        .setComputeStatus(ComputeStatus.COMPUTE_ERROR)
                        .setComputeResponse(JsonKit.toJsonString(response)));
            }
        } catch (Exception e) {
            log.error(e);
            String errorMsg;
            if (e instanceof AppException) {
                errorMsg = ((AppException) e).getErrorMessage();
            } else {
                errorMsg = ExceptionUtil.getRootCauseMessage(e);
            }
            seriesComputeMapper.updateById(new SeriesComputePO().setId(seriesComputeId)
                    .setErrorMessage(errorMsg)
                    .setComputeStatus(ComputeStatus.COMPUTE_ERROR)
                    .setComputeResponse(response != null ? JsonKit.toJsonString(response) : null));
        }
    }

    /**
     * 根据accessPath从fs读取文件并压缩
     *
     * @param instancePOList 实例列表
     * @return {@link File }
     * @author zhuhs 2024/07/11 14:39
     */
    private InputStream readDicomFromFSAndZip(List<InstancePO> instancePOList) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (InstancePO instancePO : instancePOList) {
                TaFSObject fsObject = fsManager.getObject("mids", instancePO.getAccessPath());
                zipOutputStream.putNextEntry(new ZipEntry(instancePO.getSopInstanceUid()));
                try (InputStream inputStream = fsObject.getInputstream()) {
                    IoUtil.copy(inputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
            return IoUtil.toStream(outputStream);
        } catch (IOException e) {
            throw new AppException("读取并压缩DICOM文件异常");
        }
    }

    @Override
    @Async("asyncJobExecutorService")
    public void result(String applyId) {
        KeyaResponse keyaResponse = keyaClient.result(keyaProperties.getResultUrl(), applyId);
        // KeyaResponse keyaResponse = JsonKit.parseObject(ResourceUtil.readUtf8Str("apiResult.json"), KeyaResponse.class);
        if (keyaResponse.getCode() == 1) {
            String dataContent = JsonKit.parseTree(JsonKit.toJsonString(keyaResponse)).get("data").toString();
            KeyaAnalyseResult analyseResult = JsonKit.parseObject(dataContent, KeyaAnalyseResult.class);
            AppAssert.notNull(analyseResult, "解析分析结果异常");

            List<AnalysisOverviewPO> analysisOverviewPOList = new ArrayList<>();
            KeyaAnalyseResult.Result result = analyseResult.getResult();
            CollUtil.addIfAbsent(analysisOverviewPOList, getCalciumOverviewPO(applyId, result));
            CollUtil.addIfAbsent(analysisOverviewPOList, getPneumoniaOverviewPO(applyId, result));
            AnalysisOverviewPO noduleOverviewPO = getNoduleOverviewPO(applyId, result);
            CollUtil.addIfAbsent(analysisOverviewPOList, noduleOverviewPO);
            CollUtil.addIfAbsent(analysisOverviewPOList, getFracOverviewPO(applyId, result));
            analysisOverviewMapper.insertBatch(analysisOverviewPOList);
            saveDetailAndAnnotations(applyId, result, noduleOverviewPO);
            seriesComputeMapper.update(new SeriesComputePO().setComputeStatus(ComputeStatus.COMPUTE_SUCCESS)
                            .setComputeResponse(JsonKit.toJsonString(keyaResponse)),
                    Wrappers.<SeriesComputePO>lambdaQuery().eq(SeriesComputePO::getApplyId, applyId));
        } else {
            seriesComputeMapper.update(new SeriesComputePO().setComputeStatus(ComputeStatus.COMPUTE_FAILED)
                            .setComputeResponse(JsonKit.toJsonString(keyaResponse)),
                    Wrappers.<SeriesComputePO>lambdaQuery().eq(SeriesComputePO::getApplyId, applyId));
        }
    }

    private AnalysisOverviewPO getCalciumOverviewPO(String applyId, KeyaAnalyseResult.Result result) {
        KeyaAnalyseResult.Result.Calcium calcium = result.getCalcium();
        if (calcium == null) {
            return null;
        }
        AnalysisOverviewPO calciumOverview = new AnalysisOverviewPO();
        calciumOverview.setApplyId(applyId);
        calciumOverview.setType("calcium");
        calciumOverview.setSeriesUid(calcium.getSeriesInstanceUID());
        calciumOverview.setFinding(calcium.getFinding());
        calciumOverview.setHasLesion(calcium.isHasLesion());
        calciumOverview.setNumber(calcium.getNumber());
        return calciumOverview;
    }

    private AnalysisOverviewPO getPneumoniaOverviewPO(String applyId, KeyaAnalyseResult.Result result) {
        KeyaAnalyseResult.Result.Pneumonia pneumonia = result.getPneumonia();
        if (pneumonia == null) {
            return null;
        }
        AnalysisOverviewPO pneumoniaOverview = new AnalysisOverviewPO();
        pneumoniaOverview.setApplyId(applyId);
        pneumoniaOverview.setType("pneumonia");
        pneumoniaOverview.setSeriesUid(pneumonia.getSeriesInstanceUID());
        pneumoniaOverview.setDiagnosis(pneumonia.getDiagnosis());
        pneumoniaOverview.setFinding(pneumonia.getFinding());
        pneumoniaOverview.setHasLesion(pneumonia.isHasLesion());
        pneumoniaOverview.setNumber(pneumonia.getNumber());
        return pneumoniaOverview;
    }

    private AnalysisOverviewPO getNoduleOverviewPO(String applyId, KeyaAnalyseResult.Result result) {
        KeyaAnalyseResult.Result.Nodule nodule = result.getNodule();
        if (nodule == null) {
            return null;
        }
        AnalysisOverviewPO noduleOverview = new AnalysisOverviewPO();
        noduleOverview.setApplyId(applyId);
        noduleOverview.setType("nodule");
        noduleOverview.setSeriesUid(nodule.getSeriesInstanceUID());
        noduleOverview.setDiagnosis(nodule.getDiagnosis());
        noduleOverview.setFinding(nodule.getFinding());
        noduleOverview.setHasLesion(nodule.isHasLesion());
        noduleOverview.setNumber(nodule.getNumber());
        return noduleOverview;
    }

    private AnalysisOverviewPO getFracOverviewPO(String applyId, KeyaAnalyseResult.Result result) {
        KeyaAnalyseResult.Result.Frac frac = result.getFrac();
        if (frac == null) {
            return null;
        }
        AnalysisOverviewPO fracOverview = new AnalysisOverviewPO();
        fracOverview.setApplyId(applyId);
        fracOverview.setType("frac");
        fracOverview.setSeriesUid(frac.getSeriesInstanceUID());
        fracOverview.setDiagnosis(frac.getDiagnosis());
        fracOverview.setFinding(frac.getFinding());
        fracOverview.setHasLesion(frac.isHasLesion());
        fracOverview.setNumber(frac.getNumber());
        return fracOverview;
    }

    private void saveDetailAndAnnotations(String applyId, KeyaAnalyseResult.Result result, AnalysisOverviewPO noduleOverviewPO) {
        if (result.getNodule() == null) {
            return;
        }
        List<KeyaAnalyseResult.Result.Nodule.VolumeDetail> volumeDetailList = result.getNodule().getVolumeDetailList();
        if (CollUtil.isEmpty(volumeDetailList)) {
            return;
        }
        List<VolumeDetailPO> volumeDetailPOList = new ArrayList<>();
        Map<DetailAnnotationPO, VolumeDetailPO> detailAnnotationPOMap = new HashMap<>();
        for (KeyaAnalyseResult.Result.Nodule.VolumeDetail volumeDetail : volumeDetailList) {
            VolumeDetailPO volumeDetailPO = new VolumeDetailPO();
            volumeDetailPO.setOverviewId(noduleOverviewPO.getId());
            volumeDetailPO.setApplyId(applyId);
            volumeDetailPO.setSopInstanceUid(volumeDetail.getSopInstanceUID());
            volumeDetailPO.setVocabularyEntry(volumeDetail.getVocabularyEntry());
            volumeDetailPO.setType(volumeDetail.getType());
            volumeDetailPO.setLobeSegment(volumeDetail.getLobeSegment());
            volumeDetailPO.setLobe(volumeDetail.getLobe());
            volumeDetailPO.setVolume(volumeDetail.getVolume());
            volumeDetailPO.setRiskCode(volumeDetail.getRiskCode());
            KeyaAnalyseResult.Result.Nodule.VolumeDetail.CtMeasures ctMeasures = volumeDetail.getCtMeasures();
            if (ctMeasures != null) {
                volumeDetailPO.setCtMeasuresMean(ctMeasures.getMean());
                volumeDetailPO.setCtMeasuresMinimum(ctMeasures.getMinimum());
                volumeDetailPO.setCtMeasuresMaximum(ctMeasures.getMaximum());
            }
            KeyaAnalyseResult.Result.Nodule.VolumeDetail.EllipsoidAxis ellipsoidAxis = volumeDetail.getEllipsoidAxis();
            if (ellipsoidAxis != null) {
                volumeDetailPO.setEllipsoidAxisLeast(ellipsoidAxis.getLeast());
                volumeDetailPO.setEllipsoidAxisMinor(ellipsoidAxis.getMinor());
                volumeDetailPO.setEllipsoidAxisMajor(ellipsoidAxis.getMajor());
            }
            volumeDetailPOList.add(volumeDetailPO);
            List<KeyaAnalyseResult.Result.Nodule.VolumeDetail.Annotation> annotationList = volumeDetail.getAnnotation();
            if (CollUtil.isEmpty(annotationList)) {
                continue;
            }
            for (KeyaAnalyseResult.Result.Nodule.VolumeDetail.Annotation annotation : annotationList) {
                List<KeyaAnalyseResult.Result.Nodule.VolumeDetail.Annotation.Point> points = annotation.getPoints();
                if (CollUtil.isNotEmpty(points)) {
                    String annotationUid = IdUtil.fastSimpleUUID();
                    for (KeyaAnalyseResult.Result.Nodule.VolumeDetail.Annotation.Point point : points) {
                        DetailAnnotationPO annotationPO = new DetailAnnotationPO();
                        annotationPO.setApplyId(applyId);
                        annotationPO.setAnnotationUid(annotationUid);
                        annotationPO.setType(annotation.getType());
                        annotationPO.setPointX(point.getX());
                        annotationPO.setPointY(point.getY());
                        annotationPO.setPointZ(point.getZ());
                        detailAnnotationPOMap.put(annotationPO, volumeDetailPO);
                    }
                }
            }
        }
        volumeDetailMapper.insertBatch(volumeDetailPOList);
        detailAnnotationPOMap.forEach((key, value) -> key.setVolumeDetailId(value.getId()));
        detailAnnotationMapper.insertBatch(detailAnnotationPOMap.keySet());
    }

}
