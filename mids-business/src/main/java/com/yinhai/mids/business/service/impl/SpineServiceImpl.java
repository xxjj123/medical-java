package com.yinhai.mids.business.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dtflys.forest.http.ForestResponse;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.dto.SpineRecogToDoTask;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
import com.yinhai.mids.business.entity.po.InstanceInfoPO;
import com.yinhai.mids.business.entity.po.SpineRecogTaskPO;
import com.yinhai.mids.business.entity.vo.SpineInfoVO;
import com.yinhai.mids.business.job.TaskLockManager;
import com.yinhai.mids.business.mapper.ComputeSeriesMapper;
import com.yinhai.mids.business.mapper.InstanceInfoMapper;
import com.yinhai.mids.business.mapper.SpineRecogTaskMapper;
import com.yinhai.mids.business.service.ComputeSeriesService;
import com.yinhai.mids.business.service.SpineService;
import com.yinhai.mids.business.spine.SpineClient;
import com.yinhai.mids.business.spine.SpineProperties;
import com.yinhai.mids.business.spine.SpineResponse;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.module.mybatis.UpdateEntity;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/12/2
 */
@Service
@TaTransactional
public class SpineServiceImpl implements SpineService {

    private static final Log log = LogFactory.get();

    @Resource
    private InstanceInfoMapper instanceInfoMapper;

    @Resource
    private SpineRecogTaskMapper spineRecogTaskMapper;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private ComputeSeriesService computeSeriesService;

    @Resource
    private SpineClient spineClient;

    @Resource
    private SpineProperties spineProperties;

    @Resource
    private ITaFSManager<FSManager> fsManager;

    @Override
    public void recognize(SpineRecogToDoTask recogTask) {
        ForestResponse<SpineResponse> connectResponse = spineClient.testConnect(spineProperties.getQueryUrl());
        if (connectResponse.isError()) {
            log.error("连接脊柱AI服务失败，请检查网络配置或脊柱AI服务是否正常");
            return;
        }

        String spineRecogTaskId = recogTask.getSpineRecogTaskId();
        String computeSeriesId = recogTask.getComputeSeriesId();
        LambdaQueryWrapper<InstanceInfoPO> queryWrapper = Wrappers.<InstanceInfoPO>lambdaQuery().select(
                InstanceInfoPO::getAccessPath,
                InstanceInfoPO::getSopInstanceUid
        ).eq(InstanceInfoPO::getSeriesId, recogTask.getSeriesId());
        List<InstanceInfoPO> instanceInfoList = instanceInfoMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(instanceInfoList)) {
            log.error("SpineRecogTask: {} 实例信息为空", spineRecogTaskId);
            updateRecogTaskStatus(spineRecogTaskId, computeSeriesId, -1, null, null, "实例信息为空");
            return;
        }

        try {
            TaFSObject fsObject = fsManager.getObject("mids", instanceInfoList.get(0).getAccessPath());
            ForestResponse<SpineResponse> resp = spineClient.getBoneInfo(spineProperties.getQueryUrl(), fsObject.getInputstream());
            if (resp.isError()) {
                log.error("连接脊柱AI服务失败，请检查网络配置或脊柱AI服务是否正常");
                return;
            }
            SpineResponse response = resp.getResult();
            String responseJson = JsonKit.toJsonString(response);
            if (response.getCode() == 0) {
                updateRecogTaskStatus(spineRecogTaskId, computeSeriesId, 1, responseJson, 1, null);
            } else {
                updateRecogTaskStatus(spineRecogTaskId, computeSeriesId, 1, responseJson, 0, null);
            }
        } catch (Exception e) {
            log.error(e);
            updateRecogTaskStatus(spineRecogTaskId, computeSeriesId, -1, null, null, getErrorMessage(e));
        }
    }

    private void updateRecogTaskStatus(String spineRecogTaskId, String computeSeriesId, Integer taskStatus,
                                       String recogResponse, Integer recogResult, String errorMessage) {
        SpineRecogTaskPO task = UpdateEntity.of(SpineRecogTaskPO.class);
        task.setTaskStatus(taskStatus);
        task.setRecogResponse(recogResponse);
        task.setRecogResult(recogResult);
        task.setErrorMessage(errorMessage);
        spineRecogTaskMapper.updateSetterInvoked(task,
                Wrappers.<SpineRecogTaskPO>lambdaQuery().eq(SpineRecogTaskPO::getSpineRecogTaskId, spineRecogTaskId));
        computeSeriesService.refreshComputeStatus(computeSeriesId);
    }

    private String getErrorMessage(Exception e) {
        if (e instanceof AppException) {
            return ((AppException) e).getErrorMessage();
        } else {
            return ExceptionUtil.getRootCauseMessage(e);
        }
    }

    @Async
    @Override
    public void lockedAsyncApply(SpineRecogToDoTask recogTask) {
        TaskLockManager.lock(TaskType.SPINE_RECOG, recogTask.getSpineRecogTaskId(), 60 * 2, () -> recognize(recogTask));
    }

    @Override
    public SpineInfoVO querySpineInfo(String computeSeriesId) {
        ComputeSeriesPO computeSeries = computeSeriesMapper.selectById(computeSeriesId);
        AppAssert.notNull(computeSeries, "该序列不存在！");
        AppAssert.equals(computeSeries.getComputeStatus(), ComputeStatus.COMPUTE_SUCCESS, "当前序列计算状态非成功状态，无法查看脊柱情况");

        SpineRecogTaskPO spineRecogTaskPO = spineRecogTaskMapper.selectOne(Wrappers.<SpineRecogTaskPO>lambdaQuery()
                .eq(SpineRecogTaskPO::getComputeSeriesId, computeSeriesId));
        SpineInfoVO spineInfoVO = new SpineInfoVO();
        AppAssert.notBlank(spineRecogTaskPO.getRecogResponse(), "脊柱识别结果丢失！");
        spineInfoVO.setData(JsonKit.parseObject(spineRecogTaskPO.getRecogResponse(), SpineResponse.class).getTemplate());
        return spineInfoVO;
    }
}
