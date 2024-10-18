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
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.dto.KeyaApplyToDoTask;
import com.yinhai.mids.business.entity.dto.KeyaQueryToDoTask;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.job.TaskLockManager;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.ComputeTaskService;
import com.yinhai.mids.business.service.KeyaService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.module.mybatis.UpdateEntity;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author zhuhs
 * @date 2024/10/16
 */
@Service
@TaTransactional
public class KeyaServiceImpl implements KeyaService {

    private static final Log log = LogFactory.get();

    @Resource
    private InstanceInfoMapper instanceInfoMapper;

    @Resource
    private KeyaApplyTaskMapper applyTaskMapper;

    @Resource
    private KeyaQueryTaskMapper queryTaskMapper;

    @Resource
    private DiagnosisMapper diagnosisMapper;

    @Resource
    private NoduleLesionMapper noduleLesionMapper;

    @Resource
    private ComputeTaskService computeTaskService;

    @Resource
    private ITaFSManager<FSManager> fsManager;

    @Resource
    private KeyaClient keyaClient;

    @Resource
    private KeyaProperties keyaProperties;

    @SuppressWarnings("unchecked")
    @Override
    public void apply(KeyaApplyToDoTask applyTask) {
        ForestResponse<KeyaResponse> connectResponse = keyaClient.testConnect(keyaProperties.getRegisterUrl());
        if (connectResponse.isError()) {
            log.error("连接AI服务失败，请检查网络配置或AI服务是否正常");
            return;
        }

        String applyTaskId = applyTask.getApplyTaskId();
        String computeTaskId = applyTask.getComputeTaskId();
        LambdaQueryWrapper<InstanceInfoPO> queryWrapper = Wrappers.<InstanceInfoPO>lambdaQuery().select(
                InstanceInfoPO::getAccessPath,
                InstanceInfoPO::getSopInstanceUid
        ).eq(InstanceInfoPO::getSeriesInfoId, applyTask.getSeriesInfoId());
        List<InstanceInfoPO> instanceInfoList = instanceInfoMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(instanceInfoList)) {
            log.error("KeyaApplyTask: {} 实例信息为空", applyTaskId);
            updateApplyTaskStatus(applyTaskId, computeTaskId, -1, null, null, null, null, "实例信息为空");
            return;
        }

        RegisterParam registerParam = new RegisterParam();
        String applyId = IdUtil.fastSimpleUUID();
        registerParam.setApplyId(applyId);
        registerParam.setAccessionNumber(applyTask.getAccessionNumber());
        registerParam.setStudyInstanceUID(applyTask.getStudyInstanceUid());
        registerParam.setHospitalId("ctbay99");
        registerParam.setExaminedName("胸部平扫");
        registerParam.setPatientName(applyTask.getPatientName());
        registerParam.setPatientAge(applyTask.getPatientAge());
        registerParam.setStudyDate(DateUtil.formatDateTime(applyTask.getStudyDateAndTime()));
        registerParam.setCallbackUrl(keyaProperties.getPushCallbackUrl());

        File tempZip = null;
        KeyaResponse response;
        try {
            tempZip = readDicom(instanceInfoList);
            ForestResponse<KeyaResponse> resp = keyaClient.applyCompute(keyaProperties.getRegisterUrl(), tempZip, registerParam);
            if (resp.isError()) {
                log.error("连接AI服务失败，请检查网络配置或AI服务是否正常");
                return;
            }
            response = resp.getResult();
            String responseJson = JsonKit.toJsonString(response);
            if (response.getCode() == 1) {
                updateApplyTaskStatus(applyTaskId, computeTaskId, 1, applyId, DbClock.now(), responseJson, 1, null);
                KeyaQueryTaskPO queryTask = new KeyaQueryTaskPO();
                queryTask.setComputeTaskId(computeTaskId);
                queryTask.setApplyTaskId(applyTaskId);
                queryTask.setApplyId(applyId);
                queryTask.setTaskStatus(0);
                queryTaskMapper.insert(queryTask);
            } else {
                updateApplyTaskStatus(applyTaskId, computeTaskId, 2, applyId, DbClock.now(), responseJson, 0, null);
            }
        } catch (Exception e) {
            log.error(e);
            updateApplyTaskStatus(applyTaskId, computeTaskId, -1, null, null, null, null, getErrorMessage(e));
        } finally {
            FileUtil.del(tempZip);
        }
    }

    private String getErrorMessage(Exception e) {
        if (e instanceof AppException) {
            return ((AppException) e).getErrorMessage();
        } else {
            return ExceptionUtil.getRootCauseMessage(e);
        }
    }

    /**
     * 读取DICOM文件
     */
    private File readDicom(List<InstanceInfoPO> instanceInfoList) {
        // 创建临时文件用于压缩DICOM文件
        File tempZip;
        try {
            tempZip = Files.createTempFile(null, ".zip").toFile();
        } catch (IOException e) {
            log.error(e);
            throw new AppException("创建临时文件异常");
        }
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(tempZip.toPath()))) {
            for (InstanceInfoPO instanceInfo : instanceInfoList) {
                TaFSObject fsObject = fsManager.getObject("mids", instanceInfo.getAccessPath());
                zipOutputStream.putNextEntry(new ZipEntry(instanceInfo.getSopInstanceUid()));
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

    private void updateApplyTaskStatus(String applyTaskId, String computeTaskId, Integer taskStatus, String applyId,
                                       Date applyTime, String applyResponse, Integer applyResult, String errorMessage) {
        KeyaApplyTaskPO task = UpdateEntity.of(KeyaApplyTaskPO.class);
        task.setTaskStatus(taskStatus);
        task.setApplyId(applyId);
        task.setApplyTime(applyTime);
        task.setApplyResponse(applyResponse);
        task.setApplyResult(applyResult);
        task.setErrorMessage(errorMessage);
        task.setPushTime(null);
        task.setPushContent(null);
        task.setPushResult(null);
        applyTaskMapper.updateSetterInvoked(task,
                Wrappers.<KeyaApplyTaskPO>lambdaQuery().eq(KeyaApplyTaskPO::getApplyTaskId, applyTaskId));
        computeTaskService.refreshComputeStatus(computeTaskId);
        queryTaskMapper.delete(
                Wrappers.<KeyaQueryTaskPO>lambdaQuery().eq(KeyaQueryTaskPO::getApplyTaskId, applyTaskId));
    }

    @Async
    @Override
    public void lockedAsyncApply(KeyaApplyToDoTask applyTask) {
        TaskLockManager.lock(TaskType.KEYA_APPLY, applyTask.getApplyTaskId(), 60 * 2, () -> apply(applyTask));
    }

    @Override
    public void query(KeyaQueryToDoTask queryTask) {
        String applyId = queryTask.getApplyId();
        ForestResponse<KeyaResponse> resp = keyaClient.queryComputeResult(keyaProperties.getResultUrl(), applyId);
        if (resp.isError()) {
            log.error("连接AI服务失败，请检查网络配置或AI服务是否正常");
            return;
        }

        String queryTaskId = queryTask.getQueryTaskId();
        String computeTaskId = queryTask.getComputeTaskId();
        KeyaResponse keyaResponse = resp.getResult();
        if (keyaResponse.getCode() == 2
            && DateUtil.between(queryTask.getApplyTime(), DbClock.now(), DateUnit.MINUTE) < 10
            && StrUtil.equalsAny(keyaResponse.getMessage(), "当前申请尚未开始分析，等待中。", "正在分析中。",
                "创建分析任务成功，正在分析中。")) {
            return;
        }

        if (keyaResponse.getCode() != 1) {
            updateQueryTaskStatus(queryTaskId, computeTaskId, 1, JsonKit.toJsonString(keyaResponse), 0, null);
            return;
        }

        String dataContent = JsonKit.parseTree(JsonKit.toJsonString(keyaResponse)).get("data").toString();
        KeyaComputeResult computeResult = JsonKit.parseObject(dataContent, KeyaComputeResult.class);

        if (computeResult == null || BeanUtil.getProperty(computeResult.getResult(), "nodule") == null) {
            updateQueryTaskStatus(queryTaskId, computeTaskId, -1, JsonKit.toJsonString(keyaResponse), 1, "计算结果为空");
            return;
        }
        saveNodule(queryTask, computeResult.getResult().getNodule());
        updateQueryTaskStatus(queryTaskId, computeTaskId, 1, JsonKit.toJsonString(keyaResponse), 1, null);
        KeyaApplyTaskPO applyTask = new KeyaApplyTaskPO();
        applyTask.setApplyTaskId(queryTask.getApplyTaskId());
        applyTask.setTaskStatus(2);
        applyTaskMapper.updateById(applyTask);
    }

    private void saveNodule(KeyaQueryToDoTask queryTask, KeyaComputeResult.Result.Nodule nodule) {
        String computeTaskId = queryTask.getComputeTaskId();
        diagnosisMapper.delete(Wrappers.<DiagnosisPO>lambdaQuery().eq(DiagnosisPO::getComputeSeriesId, computeTaskId));
        noduleLesionMapper.delete(
                Wrappers.<NoduleLesionPO>lambdaQuery().eq(NoduleLesionPO::getComputeSeriesId, computeTaskId));

        // 主要诊断信息
        DiagnosisPO diagnosisPO = new DiagnosisPO();
        diagnosisPO.setComputeSeriesId(computeTaskId);
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
            noduleLesionPO.setComputeSeriesId(computeTaskId);
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
                Integer imageCount = queryTask.getImageCount();
                if (minZ > imageCount) {
                    minZ = 2 * imageCount - point2.getZ();
                }
                if (maxZ > imageCount) {
                    maxZ = 2 * imageCount - point1.getZ();
                }
                noduleLesionPO.setPoints(StrUtil.join(StrPool.COMMA, ListUtil.of(
                        point1.getX(), point2.getX(), point1.getY(), point2.getY(), minZ, maxZ)));
            }
            noduleLesionPOList.add(noduleLesionPO);
        }

        // 设置IM
        for (NoduleLesionPO noduleLesionPO : noduleLesionPOList) {
            List<Integer> points = StrUtil.split(noduleLesionPO.getPoints(), ',').stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            noduleLesionPO.setIm((points.get(4) + points.get(5)) / 2);
        }

        noduleLesionMapper.insertBatch(noduleLesionPOList);
    }

    private void updateQueryTaskStatus(String queryTaskId, String computeTaskId, Integer taskStatus, String queryResponse,
                                       Integer queryResult, String errorMessage) {
        KeyaQueryTaskPO task = UpdateEntity.of(KeyaQueryTaskPO.class);
        task.setTaskStatus(taskStatus);
        task.setQueryResponse(queryResponse);
        task.setQueryResult(queryResult);
        task.setErrorMessage(errorMessage);
        queryTaskMapper.updateSetterInvoked(task,
                Wrappers.<KeyaQueryTaskPO>lambdaQuery().eq(KeyaQueryTaskPO::getQueryTaskId, queryTaskId));
        computeTaskService.refreshComputeStatus(computeTaskId);
    }

    @Async
    @Override
    public void lockedAsyncQuery(KeyaQueryToDoTask queryTask) {
        TaskLockManager.lock(TaskType.KEYA_QUERY, queryTask.getQueryTaskId(), 60 * 2, () -> query(queryTask));
    }

    @Override
    public void onApplyPush(Map<String, Object> pushParamMap) {
        String code = (String) pushParamMap.get("code");
        AppAssert.notBlank(code, "code为空");
        String applyId = (String) pushParamMap.get("applyId");
        AppAssert.notBlank(applyId, "applyId为空");
        KeyaApplyTaskPO applyTask = applyTaskMapper.selectOne(
                Wrappers.<KeyaApplyTaskPO>lambdaQuery().eq(KeyaApplyTaskPO::getApplyId, applyId));
        if (applyTask == null) {
            return;
        }
        boolean success = StrUtil.equals("1", code);
        applyTask.setTaskStatus(2);
        applyTask.setPushTime(DbClock.now());
        applyTask.setPushContent(JsonKit.toJsonString(pushParamMap));
        applyTask.setPushResult(success ? 1 : 0);
        applyTaskMapper.updateById(applyTask);
        computeTaskService.refreshComputeStatus(applyTask.getComputeTaskId());
    }
}
