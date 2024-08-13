package com.yinhai.mids.business.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.compute.*;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.ComputeService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
public class ComputeServiceImpl implements ComputeService {

    private static final Log log = LogFactory.get();

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private InstanceMapper instanceMapper;

    @Resource
    private StudyMapper studyMapper;

    @Resource
    private DiagMapper diagMapper;

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
    public void register(String computeSeriesId) {
        ComputeSeriesPO computeSeriesPO = computeSeriesMapper.selectById(computeSeriesId);
        if (computeSeriesPO == null) {
            log.error("计算序列{}不存在", computeSeriesId);
            return;
        }

        StudyPO studyPO = studyMapper.selectById(computeSeriesPO.getStudyId());
        if (studyPO == null) {
            log.error("计算序列{}对应检查不存在", computeSeriesId);
            computeSeriesMapper.updateById(new ComputeSeriesPO().setId(computeSeriesId)
                    .setComputeStatus(ComputeStatus.COMPUTE_ERROR)
                    .setErrorMessage(StrUtil.format("计算序列{}对应检查不存在", computeSeriesId)));
            return;
        }

        LambdaQueryWrapper<InstancePO> queryWrapper = Wrappers.<InstancePO>lambdaQuery()
                .select(InstancePO::getAccessPath, InstancePO::getSopInstanceUid)
                .eq(InstancePO::getSeriesId, computeSeriesPO.getSeriesId());
        List<InstancePO> instancePOList = instanceMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(instancePOList)) {
            log.error("计算序列{}对应实例不存在", computeSeriesId);
            computeSeriesMapper.updateById(new ComputeSeriesPO().setId(computeSeriesId)
                    .setComputeStatus(ComputeStatus.COMPUTE_ERROR)
                    .setErrorMessage(StrUtil.format("计算序列{}对应实例不存在", computeSeriesId)));
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
        File tempZip = readDicomFromFSAndZip(instancePOList);
        try {
            response = keyaClient.register(keyaProperties.getRegisterUrl(), tempZip, registerParam);
            if (response.getCode() == 1) {
                computeSeriesMapper.updateById(new ComputeSeriesPO().setId(computeSeriesId)
                        .setApplyId(applyId)
                        .setComputeStatus(ComputeStatus.IN_COMPUTE)
                        .setComputeStartTime(MapperKit.executeForDate())
                        .setComputeResponse(JsonKit.toJsonString(response)));
            } else {
                computeSeriesMapper.updateById(new ComputeSeriesPO().setId(computeSeriesId)
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
            computeSeriesMapper.updateById(new ComputeSeriesPO().setId(computeSeriesId)
                    .setErrorMessage(errorMsg)
                    .setComputeStatus(ComputeStatus.COMPUTE_ERROR)
                    .setComputeResponse(response != null ? JsonKit.toJsonString(response) : null));
        } finally {
            FileUtil.del(tempZip);
        }
    }

    /**
     * 根据accessPath从fs读取文件并压缩
     *
     * @param instancePOList 实例列表
     * @return {@link File } 压缩临时文件
     * @author zhuhs 2024/07/11 14:39
     */
    private File readDicomFromFSAndZip(List<InstancePO> instancePOList) {
        // 创建临时文件用于压缩DICOM文件
        File tempZip;
        try {
            tempZip = Files.createTempFile(null, ".zip").toFile();
        } catch (IOException e) {
            log.error(e);
            throw new AppException("创建临时文件异常");
        }
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(tempZip.toPath()))) {
            for (InstancePO instancePO : instancePOList) {
                TaFSObject fsObject = fsManager.getObject("mids", instancePO.getAccessPath());
                zipOutputStream.putNextEntry(new ZipEntry(instancePO.getSopInstanceUid()));
                try (InputStream inputStream = fsObject.getInputstream()) {
                    IoUtil.copy(inputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            return tempZip;
        } catch (IOException e) {
            FileUtil.del(tempZip);
            log.error(e);
            throw new AppException("读取并压缩DICOM文件异常");
        }
    }

    @Override
    public void result(String applyId) {
        KeyaResponse keyaResponse = keyaClient.result(keyaProperties.getResultUrl(), applyId);
        // KeyaResponse keyaResponse = JsonKit.parseObject(ResourceUtil.readUtf8Str("apiResult.json"), KeyaResponse.class);
        if (keyaResponse.getCode() == 1) {
            String dataContent = JsonKit.parseTree(JsonKit.toJsonString(keyaResponse)).get("data").toString();
            KeyaComputeResult computeResult = JsonKit.parseObject(dataContent, KeyaComputeResult.class);
            AppAssert.notNull(computeResult, "解析分析结果异常");

            List<DiagPO> diagPOList = new ArrayList<>();
            KeyaComputeResult.Result result = computeResult.getResult();
            CollUtil.addIfAbsent(diagPOList, getCalciumOverviewPO(applyId, result));
            CollUtil.addIfAbsent(diagPOList, getPneumoniaOverviewPO(applyId, result));
            DiagPO noduleDiagPO = getNoduleOverviewPO(applyId, result);
            CollUtil.addIfAbsent(diagPOList, noduleDiagPO);
            CollUtil.addIfAbsent(diagPOList, getFracOverviewPO(applyId, result));
            diagMapper.insertBatch(diagPOList);
            saveDetailAndAnnotations(applyId, result, noduleDiagPO);
            computeSeriesMapper.update(new ComputeSeriesPO().setComputeStatus(ComputeStatus.COMPUTE_SUCCESS)
                            .setComputeResponse(JsonKit.toJsonString(keyaResponse)),
                    Wrappers.<ComputeSeriesPO>lambdaQuery().eq(ComputeSeriesPO::getApplyId, applyId));
        } else {
            computeSeriesMapper.update(new ComputeSeriesPO().setComputeStatus(ComputeStatus.COMPUTE_FAILED)
                            .setComputeResponse(JsonKit.toJsonString(keyaResponse)),
                    Wrappers.<ComputeSeriesPO>lambdaQuery().eq(ComputeSeriesPO::getApplyId, applyId));
        }
    }

    private DiagPO getCalciumOverviewPO(String applyId, KeyaComputeResult.Result result) {
        KeyaComputeResult.Result.Calcium calcium = result.getCalcium();
        if (calcium == null) {
            return null;
        }
        DiagPO calciumOverview = new DiagPO();
        calciumOverview.setApplyId(applyId);
        calciumOverview.setType("calcium");
        calciumOverview.setSeriesUid(calcium.getSeriesInstanceUID());
        calciumOverview.setFinding(calcium.getFinding());
        calciumOverview.setHasLesion(calcium.isHasLesion());
        calciumOverview.setNumber(calcium.getNumber());
        return calciumOverview;
    }

    private DiagPO getPneumoniaOverviewPO(String applyId, KeyaComputeResult.Result result) {
        KeyaComputeResult.Result.Pneumonia pneumonia = result.getPneumonia();
        if (pneumonia == null) {
            return null;
        }
        DiagPO pneumoniaOverview = new DiagPO();
        pneumoniaOverview.setApplyId(applyId);
        pneumoniaOverview.setType("pneumonia");
        pneumoniaOverview.setSeriesUid(pneumonia.getSeriesInstanceUID());
        pneumoniaOverview.setDiagnosis(pneumonia.getDiagnosis());
        pneumoniaOverview.setFinding(pneumonia.getFinding());
        pneumoniaOverview.setHasLesion(pneumonia.isHasLesion());
        pneumoniaOverview.setNumber(pneumonia.getNumber());
        return pneumoniaOverview;
    }

    private DiagPO getNoduleOverviewPO(String applyId, KeyaComputeResult.Result result) {
        KeyaComputeResult.Result.Nodule nodule = result.getNodule();
        if (nodule == null) {
            return null;
        }
        DiagPO noduleOverview = new DiagPO();
        noduleOverview.setApplyId(applyId);
        noduleOverview.setType("nodule");
        noduleOverview.setSeriesUid(nodule.getSeriesInstanceUID());
        noduleOverview.setDiagnosis(nodule.getDiagnosis());
        noduleOverview.setFinding(nodule.getFinding());
        noduleOverview.setHasLesion(nodule.isHasLesion());
        noduleOverview.setNumber(nodule.getNumber());
        return noduleOverview;
    }

    private DiagPO getFracOverviewPO(String applyId, KeyaComputeResult.Result result) {
        KeyaComputeResult.Result.Frac frac = result.getFrac();
        if (frac == null) {
            return null;
        }
        DiagPO fracOverview = new DiagPO();
        fracOverview.setApplyId(applyId);
        fracOverview.setType("frac");
        fracOverview.setSeriesUid(frac.getSeriesInstanceUID());
        fracOverview.setDiagnosis(frac.getDiagnosis());
        fracOverview.setFinding(frac.getFinding());
        fracOverview.setHasLesion(frac.isHasLesion());
        fracOverview.setNumber(frac.getNumber());
        return fracOverview;
    }

    private void saveDetailAndAnnotations(String applyId, KeyaComputeResult.Result result, DiagPO noduleDiagPO) {
        if (result.getNodule() == null) {
            return;
        }
        List<KeyaComputeResult.Result.Nodule.VolumeDetail> volumeDetailList = result.getNodule().getVolumeDetailList();
        if (CollUtil.isEmpty(volumeDetailList)) {
            return;
        }
        List<FocalDetailPO> focalDetailPOList = new ArrayList<>();
        Map<FocalAnnoPO, FocalDetailPO> detailAnnotationPOMap = new HashMap<>();
        for (KeyaComputeResult.Result.Nodule.VolumeDetail volumeDetail : volumeDetailList) {
            FocalDetailPO focalDetailPO = new FocalDetailPO();
            focalDetailPO.setDiagId(noduleDiagPO.getId());
            focalDetailPO.setApplyId(applyId);
            focalDetailPO.setSopInstanceUid(volumeDetail.getSopInstanceUID());
            focalDetailPO.setVocabularyEntry(volumeDetail.getVocabularyEntry());
            focalDetailPO.setType(volumeDetail.getType());
            focalDetailPO.setLobeSegment(volumeDetail.getLobeSegment());
            focalDetailPO.setLobe(volumeDetail.getLobe());
            focalDetailPO.setVolume(volumeDetail.getVolume());
            focalDetailPO.setRiskCode(volumeDetail.getRiskCode());
            KeyaComputeResult.Result.Nodule.VolumeDetail.CtMeasures ctMeasures = volumeDetail.getCtMeasures();
            if (ctMeasures != null) {
                focalDetailPO.setCtMeasuresMean(ctMeasures.getMean());
                focalDetailPO.setCtMeasuresMinimum(ctMeasures.getMinimum());
                focalDetailPO.setCtMeasuresMaximum(ctMeasures.getMaximum());
            }
            KeyaComputeResult.Result.Nodule.VolumeDetail.EllipsoidAxis ellipsoidAxis = volumeDetail.getEllipsoidAxis();
            if (ellipsoidAxis != null) {
                focalDetailPO.setEllipsoidAxisLeast(ellipsoidAxis.getLeast());
                focalDetailPO.setEllipsoidAxisMinor(ellipsoidAxis.getMinor());
                focalDetailPO.setEllipsoidAxisMajor(ellipsoidAxis.getMajor());
            }
            focalDetailPOList.add(focalDetailPO);
            List<KeyaComputeResult.Result.Nodule.VolumeDetail.Annotation> annotationList = volumeDetail.getAnnotation();
            if (CollUtil.isEmpty(annotationList)) {
                continue;
            }
            for (KeyaComputeResult.Result.Nodule.VolumeDetail.Annotation annotation : annotationList) {
                List<KeyaComputeResult.Result.Nodule.VolumeDetail.Annotation.Point> points = annotation.getPoints();
                if (CollUtil.isNotEmpty(points)) {
                    String annotationUid = IdUtil.fastSimpleUUID();
                    for (KeyaComputeResult.Result.Nodule.VolumeDetail.Annotation.Point point : points) {
                        FocalAnnoPO annotationPO = new FocalAnnoPO();
                        annotationPO.setApplyId(applyId);
                        annotationPO.setAnnotationUid(annotationUid);
                        annotationPO.setType(annotation.getType());
                        annotationPO.setPointX(point.getX());
                        annotationPO.setPointY(point.getY());
                        annotationPO.setPointZ(point.getZ());
                        detailAnnotationPOMap.put(annotationPO, focalDetailPO);
                    }
                }
            }
        }
        volumeDetailMapper.insertBatch(focalDetailPOList);
        detailAnnotationPOMap.forEach((key, value) -> key.setFocalDetailId(value.getId()));
        detailAnnotationMapper.insertBatch(detailAnnotationPOMap.keySet());
    }

}
