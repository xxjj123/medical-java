package com.yinhai.mids.business.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dtflys.forest.http.ForestResponse;
import com.yinhai.mids.business.constant.MprType;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.dto.MprPushParam;
import com.yinhai.mids.business.entity.dto.MprToDoTask;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.ContextUploadResult;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.job.TaskLockManager;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.mpr.MprClient;
import com.yinhai.mids.business.mpr.MprProperties;
import com.yinhai.mids.business.mpr.MprResponse;
import com.yinhai.mids.business.mpr.RegisterParam;
import com.yinhai.mids.business.service.ComputeSeriesService;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.business.service.InstanceInfoService;
import com.yinhai.mids.business.service.MprService;
import com.yinhai.mids.common.module.mybatis.UpdateEntity;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.core.utils.ZipUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuhs
 * @date 2024/7/18 15:25
 */
@Service
@TaTransactional
public class MprServiceImpl implements MprService {

    private static final Log log = LogFactory.get();

    @Resource
    private InstanceInfoMapper instanceInfoMapper;

    @Resource
    private SeriesInfoMapper seriesInfoMapper;

    @Resource
    private MprTaskMapper mprTaskMapper;

    @Resource
    private MprModelMapper mprModelMapper;

    @Resource
    private MprSliceMapper mprSliceMapper;

    @Resource
    private ComputeSeriesService computeSeriesService;

    @Resource
    private InstanceInfoService instanceInfoService;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private MprClient mprClient;

    @Resource
    private MprProperties mprProperties;

    @Override
    @SuppressWarnings("unchecked")
    public void mpr(MprToDoTask mprTask) {
        ForestResponse<MprResponse> connectResponse = mprClient.health(mprProperties.getHealthUrl());
        if (connectResponse.isError()) {
            log.error("连接MPR服务失败，请检查网络配置或MPR服务是否正常");
            return;
        }
        MprResponse healthResponse = connectResponse.getResult();
        if (healthResponse.getCode() != 1) {
            log.error("MPR服务健康状态异常：code = {}, message = {}", healthResponse.getMessage(), healthResponse.getMessage());
            return;
        }
        String applyId = mprTask.getApplyId();
        String computeSeriesIds = mprTask.getComputeSeriesIds();
        LambdaQueryWrapper<InstanceInfoPO> queryWrapper = Wrappers.<InstanceInfoPO>lambdaQuery()
                .select(InstanceInfoPO::getAccessPath, InstanceInfoPO::getSopInstanceUid)
                .eq(InstanceInfoPO::getSeriesId, mprTask.getSeriesId());
        List<InstanceInfoPO> instanceInfoList = instanceInfoMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(instanceInfoList)) {
            log.error("MprTask: 实例信息为空，seriesId = {}", mprTask.getSeriesId());
            updateMprTaskStatus(applyId, computeSeriesIds, -1, null, null, null, null);
            return;
        }
        RegisterParam registerParam = new RegisterParam();
        registerParam.setApplyId(applyId);
        registerParam.setSeriesId(mprTask.getSeriesId());
        registerParam.setTypes(mprTask.getMprTypes());
        registerParam.setCallbackUrl(mprProperties.getPushCallbackUrl());

        File tempZip = null;
        MprResponse response;
        try {
            tempZip = instanceInfoService.readDicom(instanceInfoList);
            ForestResponse<MprResponse> resp = mprClient.register(mprProperties.getRegisterUrl(), tempZip, registerParam);
            if (resp.isError()) {
                log.error("连接MPR服务失败，请检查网络配置或MPR服务是否正常");
                return;
            }
            response = resp.getResult();
            if (response.getCode() == 4) {
                log.error("MPR繁忙：code = {}, message = {}", response.getMessage(), response.getMessage());
                return;
            }
            if (response.getCode() == 5) {
                log.error("调用已经在处理中，applyId = {}", applyId);
                return;
            }
            String responseJson = JsonKit.toJsonString(response);
            if (response.getCode() == 1) {
                updateMprTaskStatus(applyId, computeSeriesIds, 1, DbClock.now(), responseJson, 1, null);
            } else {
                updateMprTaskStatus(applyId, computeSeriesIds, 2, DbClock.now(), responseJson, 0, null);
            }
        } catch (Exception e) {
            log.error(e);
            updateMprTaskStatus(applyId, computeSeriesIds, -1, DbClock.now(), null, null, getErrorMessage(e));
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

    private void updateMprTaskStatus(String applyId, String computeSeriesIds, Integer taskStatus, Date mprTime,
                                     String mprResponse, Integer mprResult, String errorMessage) {
        MprTaskPO task = UpdateEntity.of(MprTaskPO.class);
        task.setTaskStatus(taskStatus);
        task.setMprTime(mprTime);
        task.setMprResponse(mprResponse);
        task.setMprResult(mprResult);
        task.setErrorMessage(errorMessage);
        mprTaskMapper.updateSetterInvoked(task, Wrappers.<MprTaskPO>lambdaUpdate().eq(MprTaskPO::getApplyId, applyId));
        StrUtil.split(computeSeriesIds, StrPool.COMMA).forEach(it -> computeSeriesService.refreshComputeStatus(it));
    }

    @Override
    public void lockedAsyncMpr(MprToDoTask mprTask) {
        TaskLockManager.lock(TaskType.MPR, mprTask.getApplyId(), 60 * 2, () -> mpr(mprTask));
    }

    @Async
    @Override
    public void onMprPush(MprPushParam mprPushParam) {
        String code = mprPushParam.getCode();
        String applyId = mprPushParam.getApplyId();
        String type = mprPushParam.getType();
        boolean exists = mprTaskMapper.exists(Wrappers.<MprTaskPO>lambdaQuery().eq(MprTaskPO::getApplyId, applyId));
        if (!exists) {
            return;
        }
        if (!StrUtil.equals("1", code)) {
            updateAfterPushMprTaskStatus(2, mprPushParam, 0, null);
            return;
        }
        if (mprPushParam.getException() != null) {
            updateAfterPushMprTaskStatus(-1, mprPushParam, 0, getErrorMessage(mprPushParam.getException()));
            return;
        }
        if (mprPushParam.getFile() == null) {
            updateAfterPushMprTaskStatus(-1, mprPushParam, 0, "MPR结果文件为空");
            return;
        }
        SeriesInfoPO seriesInfo = seriesInfoMapper.selectById(mprPushParam.getSeriesId());
        if (seriesInfo == null) {
            updateAfterPushMprTaskStatus(-1, mprPushParam, 1, "序列信息不存在");
            return;
        }

        File tempDir = null;
        try {
            tempDir = createMprFileTempDir(mprPushParam.getFile());
            if (StrUtil.equals(type, MprType.SLICE)) {
                saveSliceFile(tempDir, seriesInfo);
            }
            if (StrUtil.equals(type, MprType.LUNG)) {
                saveModelFile(type, tempDir, seriesInfo);
            }
        } catch (Exception e) {
            log.error(e);
            updateAfterPushMprTaskStatus(-1, mprPushParam, 1, getErrorMessage(e));
            return;
        } finally {
            FileUtil.del(tempDir);
        }
        updateAfterPushMprTaskStatus(2, mprPushParam, 1, null);
    }

    /**
     * 保存3D模型文件
     */
    private void saveModelFile(String type, File tempDir, SeriesInfoPO seriesInfo) throws IOException {
        boolean exists = mprModelMapper.exists(Wrappers.<MprModelPO>lambdaQuery()
                .eq(MprModelPO::getMprType, type).eq(MprModelPO::getSeriesId, seriesInfo.getSeriesId()));
        if (exists) {
            return;
        }
        List<File> files = FileUtil.loopFiles(tempDir, 1, FileUtil::isFile);
        MprModelPO mprModel = new MprModelPO();
        mprModel.setStudyId(seriesInfo.getStudyId());
        mprModel.setSeriesId(seriesInfo.getSeriesId());
        mprModel.setStudyInstanceUid(seriesInfo.getStudyInstanceUid());
        mprModel.setSeriesInstanceUid(seriesInfo.getSeriesInstanceUid());
        mprModel.setMprType(type);
        mprModel.setAccessPath(fileStoreService.upload(new ContextFSObject<>(files.get(0))).getAccessPath());
        mprModelMapper.insert(mprModel);
    }

    /**
     * 保存切片文件
     */
    private void saveSliceFile(File tempDir, SeriesInfoPO seriesInfo) throws IOException {
        boolean exists = mprSliceMapper.exists(
                Wrappers.<MprSlicePO>lambdaQuery().eq(MprSlicePO::getSeriesId, seriesInfo.getSeriesId()));
        if (exists) {
            return;
        }
        List<File> files = FileUtil.loopFiles(tempDir, FileUtil::isFile);
        List<ContextFSObject<String>> contextFSObjects = new ArrayList<>();
        for (File file : files) {
            ContextFSObject<String> cfo = new ContextFSObject<>(file);
            cfo.setContext(file.getName());
            contextFSObjects.add(cfo);
        }
        List<ContextUploadResult<String>> uploadResults = fileStoreService.upload(contextFSObjects);
        List<MprSlicePO> mprSliceList = new ArrayList<>();
        Map<String, Integer> viewTotalMap = new HashMap<>();
        for (ContextUploadResult<String> uploadResult : uploadResults) {
            MprSlicePO mprSlice = new MprSlicePO();
            mprSlice.setStudyId(seriesInfo.getStudyId());
            mprSlice.setSeriesId(seriesInfo.getSeriesId());
            mprSlice.setStudyInstanceUid(seriesInfo.getStudyInstanceUid());
            mprSlice.setSeriesInstanceUid(seriesInfo.getSeriesInstanceUid());
            String fileName = uploadResult.getContext();
            String withoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            String[] parts = withoutExtension.split("_");
            mprSlice.setViewName(parts[0]);
            mprSlice.setViewIndex(Integer.parseInt(parts[1]));
            viewTotalMap.put(mprSlice.getViewName(), viewTotalMap.getOrDefault(mprSlice.getViewName(), 0) + 1);
            mprSlice.setAccessPath(uploadResult.getAccessPath());
            mprSliceList.add(mprSlice);
        }
        for (MprSlicePO mprSlice : mprSliceList) {
            mprSlice.setViewTotal(viewTotalMap.get(mprSlice.getViewName()));
        }
        mprSliceMapper.insert(mprSliceList);
    }

    @SuppressWarnings("unchecked")
    private void updateAfterPushMprTaskStatus(Integer taskStatus, MprPushParam mprPushParam, Integer pushResult,
                                              String errorMessage) {
        MprTaskPO task = UpdateEntity.of(MprTaskPO.class);
        task.setTaskStatus(taskStatus);
        task.setPushTime(mprPushParam.getPushTime());
        task.setPushContent(JsonKit.toJsonString(mprPushParam));
        task.setPushResult(pushResult);
        task.setErrorMessage(errorMessage);

        mprTaskMapper.updateSetterInvoked(task, Wrappers.<MprTaskPO>lambdaUpdate()
                .eq(MprTaskPO::getApplyId, mprPushParam.getApplyId())
                .eq(MprTaskPO::getMprType, mprPushParam.getType()));

        List<MprTaskPO> mprTasks = mprTaskMapper.selectList(Wrappers.<MprTaskPO>lambdaQuery()
                .select(MprTaskPO::getComputeSeriesId)
                .eq(MprTaskPO::getApplyId, mprPushParam.getApplyId()));
        mprTasks.stream().map(MprTaskPO::getComputeSeriesId)
                .collect(Collectors.toSet())
                .forEach(i -> computeSeriesService.refreshComputeStatus(i));
    }

    /**
     * 创建一个临时文件夹，用于存储解压后的文件
     */
    private File createMprFileTempDir(File tempZip) {
        try {
            File tempDir = Files.createTempDirectory("mpr_file").toFile();
            ZipUtil.extractingAllFile(tempZip.getAbsolutePath(), tempDir.getAbsolutePath());
            return tempDir;
        } catch (IOException e) {
            log.error(e);
            throw new AppException("解压MPR文件异常");
        } finally {
            FileUtil.del(tempZip);
        }
    }
}
