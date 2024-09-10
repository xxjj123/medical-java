package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dtflys.forest.http.ForestResponse;
import com.yinhai.mids.business.compute.*;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.ComputeService;
import com.yinhai.mids.common.util.DbKit;
import com.yinhai.mids.common.util.JsonKit;
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
import java.util.List;
import java.util.stream.Collectors;
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
    private SeriesMapper seriesMapper;

    @Resource
    private DiagnosisMapper diagnosisMapper;

    @Resource
    private NoduleLesionMapper noduleLesionMapper;

    @Resource
    private ITaFSManager<FSManager> fsManager;

    @Resource
    private KeyaClient keyaClient;

    @Resource
    private KeyaProperties keyaProperties;

    @SuppressWarnings("unchecked")
    @Override
    public void applyCompute(String computeSeriesId) {
        ForestResponse<KeyaResponse> connectResponse = keyaClient.testConnect(keyaProperties.getRegisterUrl());
        if (connectResponse.isError()) {
            log.error("连接AI服务失败，请检查网络配置或AI服务是否正常");
            return;
        }

        ComputeSeriesPO computeSeriesPO = computeSeriesMapper.selectById(computeSeriesId);
        if (computeSeriesPO == null) {
            log.error("计算序列{}不存在", computeSeriesId);
            return;
        }
        computeSeriesMapper.updateById(new ComputeSeriesPO().setId(computeSeriesId).setComputeResponse(null).setErrorMessage(null));

        StudyPO studyPO = studyMapper.selectById(computeSeriesPO.getStudyId());
        if (studyPO == null) {
            String errorMsg = StrUtil.format("计算序列{}对应检查不存在", computeSeriesId);
            log.error(errorMsg);
            updateComputeStatus(computeSeriesId, ComputeStatus.COMPUTE_ERROR, null, errorMsg);
            return;
        }

        LambdaQueryWrapper<InstancePO> queryWrapper = Wrappers.<InstancePO>lambdaQuery()
                .select(InstancePO::getAccessPath, InstancePO::getSopInstanceUid)
                .eq(InstancePO::getSeriesId, computeSeriesPO.getSeriesId());
        List<InstancePO> instancePOList = instanceMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(instancePOList)) {
            String errorMsg = StrUtil.format("计算序列{}对应检查不存在", computeSeriesId);
            log.error(errorMsg);
            updateComputeStatus(computeSeriesId, ComputeStatus.COMPUTE_ERROR, null, errorMsg);
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

        File tempZip = null;
        KeyaResponse response = null;
        try {
            tempZip = readDicomFromFSAndZip(instancePOList);
            ForestResponse<KeyaResponse> resp = keyaClient.applyCompute(keyaProperties.getRegisterUrl(), tempZip, registerParam);
            if (resp.isError()) {
                log.error("连接AI服务失败，请检查网络配置或AI服务是否正常");
                return;
            }
            response = resp.getResult();
            if (response.getCode() == 1) {
                computeSeriesMapper.updateById(new ComputeSeriesPO().setId(computeSeriesId)
                        .setApplyId(applyId)
                        .setComputeStatus(ComputeStatus.IN_COMPUTE)
                        .setComputeStartTime(DbKit.now())
                        .setComputeResponse(JsonKit.toJsonString(response)));
            } else {
                updateComputeStatus(computeSeriesId, ComputeStatus.COMPUTE_ERROR, response, "申请AI分析失败");
            }
        } catch (Exception e) {
            log.error(e);
            String errorMsg;
            if (e instanceof AppException) {
                errorMsg = ((AppException) e).getErrorMessage();
            } else {
                errorMsg = ExceptionUtil.getRootCauseMessage(e);
            }
            updateComputeStatus(computeSeriesId, ComputeStatus.COMPUTE_ERROR, response, errorMsg);
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
    public void queryComputeResult(String applyId) {
        ForestResponse<KeyaResponse> connectResponse = keyaClient.testConnect(keyaProperties.getRegisterUrl());
        if (connectResponse.isError()) {
            log.error("连接AI服务失败，请检查网络配置或AI服务是否正常");
            return;
        }

        ComputeSeriesPO computeSeries = computeSeriesMapper.selectOne(
                Wrappers.<ComputeSeriesPO>lambdaQuery().eq(ComputeSeriesPO::getApplyId, applyId));
        if (computeSeries == null) {
            log.error("applyId {} 对应计算序列不存在", applyId);
            return;
        }
        String computeSeriesId = computeSeries.getId();

        ForestResponse<KeyaResponse> resp = keyaClient.queryComputeResult(keyaProperties.getResultUrl(), applyId);
        if (resp.isError()) {
            log.error("连接AI服务失败，请检查网络配置或AI服务是否正常");
            return;
        }
        KeyaResponse keyaResponse = resp.getResult();
        if (keyaResponse.getCode() == 2
            && DateUtil.between(computeSeries.getComputeStartTime(), DbKit.now(), DateUnit.MINUTE) < 10
            && StrUtil.equalsAny(keyaResponse.getMessage(), "当前申请尚未开始分析，等待中。", "正在分析中。",
                "创建分析任务成功，正在分析中。")) {
            return;
        }

        if (keyaResponse.getCode() != 1) {
            updateComputeStatus(computeSeriesId, ComputeStatus.COMPUTE_FAILED, keyaResponse, null);
            return;
        }

        String dataContent = JsonKit.parseTree(JsonKit.toJsonString(keyaResponse)).get("data").toString();
        KeyaComputeResult computeResult = JsonKit.parseObject(dataContent, KeyaComputeResult.class);

        if (computeResult == null || BeanUtil.getProperty(computeResult.getResult(), "nodule") == null) {
            updateComputeStatus(computeSeriesId, ComputeStatus.COMPUTE_FAILED, keyaResponse, "计算结果为空");
            return;
        }
        saveNodule(computeSeries, computeResult.getResult().getNodule());
        updateComputeStatus(computeSeriesId, ComputeStatus.COMPUTE_SUCCESS, keyaResponse, null);
    }

    @SuppressWarnings("unchecked")
    private void saveNodule(ComputeSeriesPO computeSeries, KeyaComputeResult.Result.Nodule nodule) {
        String computeSeriesId = computeSeries.getId();
        diagnosisMapper.delete(Wrappers.<DiagnosisPO>lambdaQuery().eq(DiagnosisPO::getComputeSeriesId, computeSeriesId));
        noduleLesionMapper.delete(Wrappers.<NoduleLesionPO>lambdaQuery().eq(NoduleLesionPO::getComputeSeriesId, computeSeriesId));

        SeriesPO seriesPO = seriesMapper.selectById(computeSeries.getSeriesId());


        // 主要诊断信息
        DiagnosisPO diagnosisPO = new DiagnosisPO();
        diagnosisPO.setComputeSeriesId(computeSeriesId);
        diagnosisPO.setType("nodule");
        diagnosisPO.setDiagnosis(nodule.getDiagnosis());
        diagnosisPO.setFinding(nodule.getFinding());
        diagnosisPO.setHasLesion(nodule.isHasLesion());
        diagnosisPO.setNumber(nodule.getNumber());
        diagnosisMapper.insert(diagnosisPO);

        // 结节详细信息
        List<KeyaComputeResult.Result.Nodule.VolumeDetail> volumeDetailList = nodule.getVolumeDetailList();
        if (CollUtil.isEmpty(volumeDetailList)) {
            return;
        }
        List<NoduleLesionPO> noduleLesionPOList = new ArrayList<>();
        for (KeyaComputeResult.Result.Nodule.VolumeDetail volumeDetail : volumeDetailList) {
            NoduleLesionPO noduleLesionPO = new NoduleLesionPO();
            noduleLesionPO.setDataType(0);
            noduleLesionPO.setChecked(true);
            noduleLesionPO.setComputeSeriesId(computeSeriesId);
            noduleLesionPO.setSopInstanceUid(volumeDetail.getSopInstanceUID());
            noduleLesionPO.setVocabularyEntry(volumeDetail.getVocabularyEntry());
            noduleLesionPO.setType(volumeDetail.getType());
            noduleLesionPO.setLobeSegment(volumeDetail.getLobeSegment());
            noduleLesionPO.setLobe(volumeDetail.getLobe());
            noduleLesionPO.setVolume(volumeDetail.getVolume());
            noduleLesionPO.setRiskCode(volumeDetail.getRiskCode());
            KeyaComputeResult.Result.Nodule.VolumeDetail.CtMeasures ctMeasures = volumeDetail.getCtMeasures();
            if (ctMeasures != null) {
                noduleLesionPO.setCtMeasuresMean(ctMeasures.getMean());
                noduleLesionPO.setCtMeasuresMinimum(ctMeasures.getMinimum());
                noduleLesionPO.setCtMeasuresMaximum(ctMeasures.getMaximum());
            }
            KeyaComputeResult.Result.Nodule.VolumeDetail.EllipsoidAxis ellipsoidAxis = volumeDetail.getEllipsoidAxis();
            if (ellipsoidAxis != null) {
                noduleLesionPO.setEllipsoidAxisLeast(ellipsoidAxis.getLeast());
                noduleLesionPO.setEllipsoidAxisMinor(ellipsoidAxis.getMinor());
                noduleLesionPO.setEllipsoidAxisMajor(ellipsoidAxis.getMajor());
            }
            List<KeyaComputeResult.Result.Nodule.VolumeDetail.Annotation> annotationList = volumeDetail.getAnnotation();
            if (CollUtil.isNotEmpty(annotationList)) {
                KeyaComputeResult.Result.Nodule.VolumeDetail.Annotation annotation = annotationList.get(0);
                KeyaComputeResult.Result.Nodule.VolumeDetail.Annotation.Point point1 = annotation.getPoints().get(0);
                KeyaComputeResult.Result.Nodule.VolumeDetail.Annotation.Point point2 = annotation.getPoints().get(1);

                int minZ = point1.getZ();
                int maxZ = point2.getZ();
                Integer imageCount = seriesPO.getImageCount();
                if(minZ > imageCount){
                    minZ = 2*imageCount - point2.getZ();
                }
                if(maxZ > imageCount){
                    maxZ = 2*imageCount - point1.getZ();
                }
                noduleLesionPO.setPoints(StrUtil.join(StrPool.COMMA, ListUtil.of(
                        point1.getX(), point2.getX(), point1.getY(), point2.getY(), minZ,maxZ)));
            }
            noduleLesionPOList.add(noduleLesionPO);
        }

        // 设置IM
//        List<InstancePO> instancePOList = instanceMapper.selectList(Wrappers.<InstancePO>lambdaQuery()
//                .select(InstancePO::getInstanceNumber, InstancePO::getSopInstanceUid)
//                .eq(InstancePO::getSeriesId, computeSeries.getSeriesId()));
//        Map<String, Integer> imMap = new HashMap<>();
//        for (InstancePO instancePO : instancePOList) {
//            imMap.put(instancePO.getSopInstanceUid(), instancePO.getInstanceNumber());
//        }
        for (NoduleLesionPO noduleLesionPO : noduleLesionPOList) {
            List<Integer> points = StrUtil.split(noduleLesionPO.getPoints(),',').stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());;
            noduleLesionPO.setIm((points.get(4) + points.get(5)) / 2);
        }

        noduleLesionMapper.insertBatch(noduleLesionPOList);
    }

    private void updateComputeStatus(String computeSeriesId, String computeStatus, KeyaResponse computeResponse, String errorMsg) {
        ComputeSeriesPO computeSeriesPO = new ComputeSeriesPO();
        computeSeriesPO.setId(computeSeriesId);
        computeSeriesPO.setComputeStatus(computeStatus);
        computeSeriesPO.setComputeResponse(JsonKit.toJsonString(computeResponse));
        computeSeriesPO.setErrorMessage(errorMsg);
        computeSeriesMapper.updateById(computeSeriesPO);
    }
}
