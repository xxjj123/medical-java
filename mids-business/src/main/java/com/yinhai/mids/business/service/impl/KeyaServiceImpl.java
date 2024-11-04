package com.yinhai.mids.business.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dtflys.forest.http.ForestResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.yinhai.mids.business.constant.DiagnosisType;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.dto.KeyaApplyToDoTask;
import com.yinhai.mids.business.entity.dto.KeyaQueryToDoTask;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.job.TaskLockManager;
import com.yinhai.mids.business.keya.*;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.ComputeSeriesService;
import com.yinhai.mids.business.service.InstanceInfoService;
import com.yinhai.mids.business.service.KeyaService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.module.mybatis.UpdateEntity;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private PneumoniaLesionMapper pneumoniaLesionMapper;

    @Resource
    private PneumoniaContourMapper pneumoniaContourMapper;

    @Resource
    private FracLesionMapper fracLesionMapper;

    @Resource
    private ComputeSeriesService computeSeriesService;

    @Resource
    private InstanceInfoService instanceInfoService;

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
        String computeSeriesId = applyTask.getComputeSeriesId();
        LambdaQueryWrapper<InstanceInfoPO> queryWrapper = Wrappers.<InstanceInfoPO>lambdaQuery().select(
                InstanceInfoPO::getAccessPath,
                InstanceInfoPO::getSopInstanceUid
        ).eq(InstanceInfoPO::getSeriesId, applyTask.getSeriesId());
        List<InstanceInfoPO> instanceInfoList = instanceInfoMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(instanceInfoList)) {
            log.error("KeyaApplyTask: {} 实例信息为空", applyTaskId);
            updateApplyTaskStatus(applyTaskId, computeSeriesId, -1, null, null, null, null, "实例信息为空");
            return;
        }

        RegisterParam registerParam = new RegisterParam();
        String applyId = IdUtil.fastSimpleUUID();
        registerParam.setApplyId(applyId);
        registerParam.setAccessionNumber(applyTask.getAccessionNumber());
        registerParam.setStudyInstanceUID(applyTask.getStudyInstanceUid());
        registerParam.setHospitalId("1299");
        registerParam.setExaminedName("胸部平扫");
        registerParam.setPatientName(applyTask.getPatientName());
        registerParam.setPatientAge(applyTask.getPatientAge());
        registerParam.setStudyDate(DateUtil.formatDateTime(applyTask.getStudyDateAndTime()));
        registerParam.setCallbackUrl(keyaProperties.getPushCallbackUrl());

        File tempZip = null;
        KeyaResponse response;
        try {
            tempZip = instanceInfoService.readDicom(instanceInfoList);
            ForestResponse<KeyaResponse> resp = keyaClient.applyCompute(keyaProperties.getRegisterUrl(), tempZip, registerParam);
            if (resp.isError()) {
                log.error("连接AI服务失败，请检查网络配置或AI服务是否正常");
                return;
            }
            response = resp.getResult();
            String responseJson = JsonKit.toJsonString(response);
            if (response.getCode() == 1) {
                updateApplyTaskStatus(applyTaskId, computeSeriesId, 1, applyId, DbClock.now(), responseJson, 1, null);
            } else {
                updateApplyTaskStatus(applyTaskId, computeSeriesId, 2, applyId, DbClock.now(), responseJson, 0, null);
            }
        } catch (Exception e) {
            log.error(e);
            updateApplyTaskStatus(applyTaskId, computeSeriesId, -1, null, null, null, null, getErrorMessage(e));
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

    private void updateApplyTaskStatus(String applyTaskId, String computeSeriesId, Integer taskStatus, String applyId,
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
        computeSeriesService.refreshComputeStatus(computeSeriesId);
        queryTaskMapper.delete(
                Wrappers.<KeyaQueryTaskPO>lambdaQuery().eq(KeyaQueryTaskPO::getApplyTaskId, applyTaskId));
    }

    @Async
    @Override
    public void lockedAsyncApply(KeyaApplyToDoTask applyTask) {
        TaskLockManager.lock(TaskType.KEYA_APPLY, applyTask.getApplyTaskId(), 60 * 2, () -> apply(applyTask));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void query(KeyaQueryToDoTask queryTask) {
        String applyId = queryTask.getApplyId();
        ForestResponse<KeyaResponse> resp = keyaClient.queryComputeResult(keyaProperties.getResultUrl(), applyId);
        if (resp.isError()) {
            log.error("连接AI服务失败，请检查网络配置或AI服务是否正常");
            return;
        }

        String queryTaskId = queryTask.getQueryTaskId();
        String computeSeriesId = queryTask.getComputeSeriesId();
        String applyTaskId = queryTask.getApplyTaskId();
        KeyaResponse keyaResponse = resp.getResult();
        if (keyaResponse.getCode() == 2
            && DateUtil.between(queryTask.getApplyTime(), DbClock.now(), DateUnit.MINUTE) < 10
            && StrUtil.equalsAny(keyaResponse.getMessage(), "当前申请尚未开始分析，等待中。", "正在分析中。",
                "创建分析任务成功，正在分析中。")) {
            return;
        }
        String keyaResponseJson = JsonKit.toJsonString(keyaResponse);

        if (keyaResponse.getCode() != 1) {
            updateQueryTaskStatus(queryTaskId, computeSeriesId, 1, keyaResponseJson, 0, null, applyTaskId);
            return;
        }
        JsonNode resultTree = JsonKit.parseTree(keyaResponseJson);
        if (resultTree.at("/data/result").isMissingNode()) {
            updateQueryTaskStatus(queryTaskId, computeSeriesId, -1, keyaResponseJson, 1, "计算结果为空", applyTaskId);
            return;
        }

        KeyaApplyTaskPO applyTaskPO = applyTaskMapper.selectOne(Wrappers.<KeyaApplyTaskPO>lambdaQuery()
                .select(KeyaApplyTaskPO::getPushResult, KeyaApplyTaskPO::getPushContent)
                .eq(KeyaApplyTaskPO::getApplyTaskId, queryTask.getApplyTaskId())
                .eq(KeyaApplyTaskPO::getApplyId, queryTask.getApplyId()));
        String pushContent = applyTaskPO.getPushContent();
        if (applyTaskPO.getPushResult() != 1 || StrUtil.isEmpty(pushContent)) {
            updateQueryTaskStatus(queryTaskId, computeSeriesId, -1, keyaResponseJson, 1, "推送结果异常", applyTaskId);
            return;
        }
        JsonNode detailTextNode = JsonKit.parseTree(pushContent).at("/detail");
        if (detailTextNode.isMissingNode()) {
            saveNodule(queryTask, resultTree, null);
        } else {
            JsonNode detailNode = JsonKit.parseTree(detailTextNode.asText());
            saveNodule(queryTask, resultTree, detailNode);
            savePneumonia(queryTask, resultTree, detailNode);
            saveFrac(queryTask, resultTree, detailNode);
        }

        updateQueryTaskStatus(queryTaskId, computeSeriesId, 1, keyaResponseJson, 1, null, applyTaskId);
    }

    private void saveFrac(KeyaQueryToDoTask queryTask, JsonNode resultTree, JsonNode detailNode) {
        String computeSeriesId = queryTask.getComputeSeriesId();
        diagnosisMapper.delete(Wrappers.<DiagnosisPO>lambdaQuery()
                .eq(DiagnosisPO::getType, DiagnosisType.FRAC)
                .eq(DiagnosisPO::getComputeSeriesId, computeSeriesId));
        fracLesionMapper.delete(
                Wrappers.<FracLesionPO>lambdaQuery().eq(FracLesionPO::getComputeSeriesId, computeSeriesId));

        JsonNode node = detailNode.at("/frac/infos");
        if (node.isMissingNode()) {
            return;
        }

        // 主要诊断信息
        DiagnosisPO diagnosisPO = new DiagnosisPO();
        diagnosisPO.setComputeSeriesId(computeSeriesId);
        diagnosisPO.setType(DiagnosisType.FRAC);
        diagnosisPO.setDiagnosis(parseDiagnosis(resultTree, DiagnosisType.FRAC));
        diagnosisPO.setFinding(parseFinding(resultTree, DiagnosisType.FRAC));
        diagnosisPO.setHasLesion(parseHasLesion(resultTree, DiagnosisType.FRAC));
        diagnosisPO.setNumber(parseNumber(resultTree, DiagnosisType.FRAC));
        diagnosisMapper.insert(diagnosisPO);

        // 骨折详细信息
        List<KeyaFracInfo> fracInfos = JsonKit.parseObject(node.toString(), new TypeReference<List<KeyaFracInfo>>() {
        });
        if (CollUtil.isEmpty(fracInfos)) {
            return;
        }
        List<FracLesionPO> fracLesionPOList = new ArrayList<>();
        for (KeyaFracInfo fracInfo : fracInfos) {
            FracLesionPO fracLesionPO = new FracLesionPO();
            fracLesionPO.setDataType(0);
            fracLesionPO.setComputeSeriesId(computeSeriesId);
            fracLesionPO.setChecked(true);
            fracLesionPO.setRibSide(fracInfo.getRibSide());
            fracLesionPO.setRibType(fracInfo.getRibType());
            fracLesionPO.setRibNum(fracInfo.getRibNum());
            fracLesionPO.setFracClass(fracInfo.getFracClass());
            fracLesionPO.setFracBBox(Joiner.on(StrPool.COMMA).join(fracInfo.getFracBBox()));
            fracLesionPOList.add(fracLesionPO);
        }
        fracLesionMapper.insert(fracLesionPOList);
    }

    private void savePneumonia(KeyaQueryToDoTask queryTask, JsonNode resultTree, JsonNode detailNode) {
        String computeSeriesId = queryTask.getComputeSeriesId();
        diagnosisMapper.delete(Wrappers.<DiagnosisPO>lambdaQuery()
                .eq(DiagnosisPO::getType, DiagnosisType.PNEUMONIA)
                .eq(DiagnosisPO::getComputeSeriesId, computeSeriesId));
        pneumoniaLesionMapper.delete(
                Wrappers.<PneumoniaLesionPO>lambdaQuery().eq(PneumoniaLesionPO::getComputeSeriesId, computeSeriesId));

        JsonNode node = detailNode.at("/pneumonia/infos");
        if (node.isMissingNode()) {
            return;
        }

        // 主要诊断信息
        DiagnosisPO diagnosisPO = new DiagnosisPO();
        diagnosisPO.setComputeSeriesId(computeSeriesId);
        diagnosisPO.setType(DiagnosisType.PNEUMONIA);
        diagnosisPO.setDiagnosis(parseDiagnosis(resultTree, DiagnosisType.PNEUMONIA));
        diagnosisPO.setFinding(parseFinding(resultTree, DiagnosisType.PNEUMONIA));
        diagnosisPO.setHasLesion(parseHasLesion(resultTree, DiagnosisType.PNEUMONIA));
        diagnosisPO.setNumber(parseNumber(resultTree, DiagnosisType.PNEUMONIA));
        diagnosisMapper.insert(diagnosisPO);

        // 肺炎详细信息
        List<KeyaPneumoniaInfo> pneumoniaInfos = JsonKit.parseObject(node.toString(), new TypeReference<List<KeyaPneumoniaInfo>>() {
        });
        if (CollUtil.isEmpty(pneumoniaInfos)) {
            return;
        }
        List<PneumoniaLesionPO> pneumoniaLesionPOList = new ArrayList<>();
        for (KeyaPneumoniaInfo pneumoniaInfo : pneumoniaInfos) {
            PneumoniaLesionPO pneumoniaLesionPO = new PneumoniaLesionPO();
            pneumoniaLesionPO.setDataType(0);
            pneumoniaLesionPO.setComputeSeriesId(computeSeriesId);
            pneumoniaLesionPO.setChecked(true);
            pneumoniaLesionPO.setLobeName(pneumoniaInfo.getName());
            pneumoniaLesionPO.setLobeVolume(pneumoniaInfo.getLobeVolume());
            pneumoniaLesionPO.setDiseaseVolume(pneumoniaInfo.getDiseaseVolume());
            pneumoniaLesionPO.setIntensity(pneumoniaInfo.getIntensity());
            pneumoniaLesionPO.setDiseaseClass(pneumoniaInfo.getDiseaseClass());
            pneumoniaLesionPOList.add(pneumoniaLesionPO);
        }
        pneumoniaLesionMapper.insert(pneumoniaLesionPOList);

        List<PneumoniaContourPO> pneumoniaContourPOList = new ArrayList<>();
        JsonNode contourNode = detailNode.at("/pneumonia/contours");
        contourNode.fieldNames().forEachRemaining(viewName -> {
            contourNode.get(viewName).elements().forEachRemaining(n -> {
                JsonNode contours = n.get("contours");
                contours.fieldNames().forEachRemaining(instanceNumber -> {
                    JsonNode jsonNode = contours.get(instanceNumber);
                    PneumoniaContourPO contourPO = new PneumoniaContourPO();
                    contourPO.setComputeSeriesId(computeSeriesId);
                    contourPO.setViewName(viewName);
                    contourPO.setInstanceNumber(Integer.parseInt(instanceNumber));
                    contourPO.setPoints(jsonNode.toString());
                    pneumoniaContourPOList.add(contourPO);
                });
            });
        });
        if (CollUtil.isNotEmpty(pneumoniaContourPOList)) {
            pneumoniaContourMapper.insert(pneumoniaContourPOList);
        }
    }

    private void saveNodule(KeyaQueryToDoTask queryTask, JsonNode resultTree, @Nullable JsonNode detailNode) {
        String computeSeriesId = queryTask.getComputeSeriesId();
        diagnosisMapper.delete(Wrappers.<DiagnosisPO>lambdaQuery()
                .eq(DiagnosisPO::getType, DiagnosisType.NODULE)
                .eq(DiagnosisPO::getComputeSeriesId, computeSeriesId));
        noduleLesionMapper.delete(
                Wrappers.<NoduleLesionPO>lambdaQuery().eq(NoduleLesionPO::getComputeSeriesId, computeSeriesId));

        List<KeyaNoduleInfo> noduleInfos;
        if (detailNode != null) {
            JsonNode node = detailNode.at("/nodule/infos");
            if (node.isMissingNode()) {
                return;
            }
            noduleInfos = JsonKit.parseObject(node.toString(), new TypeReference<List<KeyaNoduleInfo>>() {
            });
        } else {
            JsonNode node = resultTree.at("/data/result/nodule/volumeDetailList");
            noduleInfos = JsonKit.parseObject(node.toString(), new TypeReference<List<KeyaNoduleInfo>>() {
            });
        }

        // 主要诊断信息
        DiagnosisPO diagnosisPO = new DiagnosisPO();
        diagnosisPO.setComputeSeriesId(computeSeriesId);
        diagnosisPO.setType(DiagnosisType.NODULE);
        diagnosisPO.setDiagnosis(parseDiagnosis(resultTree, DiagnosisType.NODULE));
        diagnosisPO.setFinding(parseFinding(resultTree, DiagnosisType.NODULE));
        diagnosisPO.setHasLesion(parseHasLesion(resultTree, DiagnosisType.NODULE));
        diagnosisPO.setNumber(parseNumber(resultTree, DiagnosisType.NODULE));
        diagnosisMapper.insert(diagnosisPO);

        // 结节详细信息
        if (CollUtil.isEmpty(noduleInfos)) {
            return;
        }
        List<NoduleLesionPO> noduleLesionPOList = new ArrayList<>();
        for (KeyaNoduleInfo noduleInfo : noduleInfos) {
            NoduleLesionPO noduleLesionPO = new NoduleLesionPO();
            noduleLesionPO.setDataType(0);
            noduleLesionPO.setChecked(true);
            noduleLesionPO.setComputeSeriesId(computeSeriesId);
            if (noduleInfo.getInstanceUID() == null) {
                noduleLesionPO.setSopInstanceUid(noduleInfo.getSopInstanceUID());
            } else {
                noduleLesionPO.setSopInstanceUid(noduleInfo.getInstanceUID());
            }
            noduleLesionPO.setVocabularyEntry(noduleInfo.getVocabularyEntry());
            noduleLesionPO.setType(noduleInfo.getType());
            noduleLesionPO.setLobeSegment(noduleInfo.getLobeSegment());
            noduleLesionPO.setLobe(noduleInfo.getLobe());
            noduleLesionPO.setVolume(noduleInfo.getVolume());
            KeyaNoduleInfo.CtMeasures ctMeasures = noduleInfo.getCtMeasures();
            if (ctMeasures != null) {
                noduleLesionPO.setCtMeasuresMean(ctMeasures.getMean());
                noduleLesionPO.setCtMeasuresMinimum(ctMeasures.getMinimum());
                noduleLesionPO.setCtMeasuresMaximum(ctMeasures.getMaximum());
            }
            KeyaNoduleInfo.EllipsoidAxis ellipsoidAxis = noduleInfo.getEllipsoidAxis();
            if (ellipsoidAxis != null) {
                noduleLesionPO.setEllipsoidAxisLeast(ellipsoidAxis.getLeast());
                noduleLesionPO.setEllipsoidAxisMinor(ellipsoidAxis.getMinor());
                noduleLesionPO.setEllipsoidAxisMajor(ellipsoidAxis.getMajor());
            }

            List<KeyaNoduleInfo.Point> points;
            if (CollUtil.isNotEmpty(noduleInfo.getAnnotation())) {
                points = noduleInfo.getAnnotation().get(0).getPoints();
            } else {
                points = noduleInfo.getPoints();
            }
            if (CollUtil.isNotEmpty(points)) {
                KeyaNoduleInfo.Point point1 = points.get(0);
                KeyaNoduleInfo.Point point2 = points.get(1);
                int minZ = point1.getZ();
                int maxZ = point2.getZ();
                Integer imageCount = queryTask.getImageCount();
                if (minZ > imageCount) {
                    minZ = 2 * imageCount - point2.getZ();
                }
                if (maxZ > imageCount) {
                    maxZ = 2 * imageCount - point1.getZ();
                }
                noduleLesionPO.setPoints(Joiner.on(StrPool.COMMA)
                        .join(point1.getX(), point2.getX(), point1.getY(), point2.getY(), minZ, maxZ));

                // IM
                noduleLesionPO.setIm((minZ + maxZ) / 2);
            }
            if (noduleInfo.getRiskCode() != null) {
                noduleLesionPO.setRiskCode(noduleInfo.getRiskCode());
            } else {
                noduleLesionPO.setRiskCode(getRiskCode(noduleLesionPO.getType(),
                        noduleLesionPO.getEllipsoidAxisLeast(), noduleLesionPO.getEllipsoidAxisMajor()));
            }
            noduleLesionPOList.add(noduleLesionPO);
        }
        noduleLesionMapper.insert(noduleLesionPOList);
    }

    private void updateQueryTaskStatus(String queryTaskId, String computeSeriesId, Integer taskStatus, String queryResponse,
                                       Integer queryResult, String errorMessage, String applyTaskId) {
        KeyaQueryTaskPO task = UpdateEntity.of(KeyaQueryTaskPO.class);
        task.setTaskStatus(taskStatus);
        task.setQueryResponse(queryResponse);
        task.setQueryResult(queryResult);
        task.setErrorMessage(errorMessage);
        queryTaskMapper.updateSetterInvoked(task,
                Wrappers.<KeyaQueryTaskPO>lambdaQuery().eq(KeyaQueryTaskPO::getQueryTaskId, queryTaskId));
        computeSeriesService.refreshComputeStatus(computeSeriesId);
        KeyaApplyTaskPO applyTask = new KeyaApplyTaskPO();
        applyTask.setApplyTaskId(applyTaskId);
        applyTask.setTaskStatus(2);
        applyTaskMapper.updateById(applyTask);
    }

    private Integer getRiskCode(String type, Double ellipsoidAxisLeast, Double ellipsoidAxisMajor) {
        if (type == null || ellipsoidAxisLeast == null || ellipsoidAxisMajor == null) {
            return null;
        }
        double diameter = (ellipsoidAxisLeast + ellipsoidAxisMajor) / 2.0;
        switch (type) {
            case "Mass":
                return 3;
            case "Solid":
                if (diameter < 5.0) {
                    return 1;
                } else {
                    if (diameter >= 5.0 && diameter < 8.0) {
                        return 2;
                    }
                    return 3;
                }
            case "Mixed":
                if (diameter <= 8.0) {
                    return 2;
                }
                return 3;
            case "GCN":
                if (diameter <= 5.0) {
                    return 1;
                }
                return 2;
            case "Calcified":
            default:
                return 1;
        }
    }

    private String parseDiagnosis(JsonNode node, String diagnosisType) {
        return node.at("/data/result/" + diagnosisType + "/diagnosis").asText(null);
    }

    private String parseFinding(JsonNode node, String diagnosisType) {
        return node.at("/data/result/" + diagnosisType + "/finding").asText(null);
    }

    private Boolean parseHasLesion(JsonNode node, String diagnosisType) {
        JsonNode hasLesionNode = node.at("/data/result/" + diagnosisType + "/hasLesion");
        if (hasLesionNode.isMissingNode()) {
            return null;
        } else {
            return hasLesionNode.asBoolean();
        }
    }

    private Integer parseNumber(JsonNode node, String diagnosisType) {
        JsonNode hasLesionNode = node.at("/data/result/" + diagnosisType + "/number");
        if (hasLesionNode.isMissingNode()) {
            return null;
        } else {
            return hasLesionNode.asInt();
        }
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
            log.debug("applyTask不存在，applyId = {}", applyId);
            return;
        }
        boolean success = StrUtil.equals("1", code);
        applyTask.setTaskStatus(2);
        applyTask.setPushTime(DbClock.now());
        applyTask.setPushContent(JsonKit.toJsonString(pushParamMap));
        applyTask.setPushResult(success ? 1 : 0);
        applyTaskMapper.updateById(applyTask);
        computeSeriesService.refreshComputeStatus(applyTask.getComputeSeriesId());

        if (success) {
            KeyaQueryTaskPO queryTask = new KeyaQueryTaskPO();
            queryTask.setComputeSeriesId(applyTask.getComputeSeriesId());
            queryTask.setApplyTaskId(applyTask.getApplyTaskId());
            queryTask.setApplyId(applyId);
            queryTask.setTaskStatus(0);
            queryTaskMapper.insert(queryTask);
        }
    }
}
