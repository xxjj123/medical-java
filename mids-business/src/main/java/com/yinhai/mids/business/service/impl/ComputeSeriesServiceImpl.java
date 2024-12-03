package com.yinhai.mids.business.service.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.ComputeType;
import com.yinhai.mids.business.entity.dto.LungTaskInfo;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
import com.yinhai.mids.business.entity.po.SpineRecogTaskPO;
import com.yinhai.mids.business.mapper.ComputeSeriesMapper;
import com.yinhai.mids.business.mapper.SpineRecogTaskMapper;
import com.yinhai.mids.business.service.ComputeSeriesService;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/8 15:25
 */
@Service
@TaTransactional
public class ComputeSeriesServiceImpl implements ComputeSeriesService {

    private static final Log log = LogFactory.get();

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private SpineRecogTaskMapper spineRecogTaskMapper;

    @Override
    public void refreshComputeStatus(String computeSeriesId) {
        ComputeSeriesPO computeSeries = computeSeriesMapper.selectById(computeSeriesId);
        if (computeSeries == null) {
            return;
        }

        int computeStatus = ComputeStatus.IN_COMPUTE;
        Integer computeType = computeSeries.getComputeType();
        if (ComputeType.LUNG == computeType) {
            LungTaskInfo lungTaskInfo = computeSeriesMapper.queryLungTaskInfo(computeSeriesId);
            if (lungTaskInfo == null) {
                return;
            }
            Integer applyTaskStatus = lungTaskInfo.getApplyTaskStatus();
            Integer applyResult = lungTaskInfo.getApplyResult();
            Integer pushResult = lungTaskInfo.getPushResult();
            Integer queryTaskStatus = lungTaskInfo.getQueryTaskStatus();
            Integer queryResult = lungTaskInfo.getQueryResult();
            List<LungTaskInfo.MprTaskInfo> mprTaskInfoList = lungTaskInfo.getMprTaskInfoList();

            if (applyTaskStatus == 0
                && mprTaskInfoList.stream().map(LungTaskInfo.MprTaskInfo::getMprTaskStatus).allMatch(it -> it == 0)) {
                computeStatus = ComputeStatus.WAIT_COMPUTE;
            }
            if (applyResult == 0 || pushResult == 0 || queryResult == 0
                || mprTaskInfoList.stream().map(LungTaskInfo.MprTaskInfo::getMprResult).anyMatch(it -> it == 0)
                || mprTaskInfoList.stream().map(LungTaskInfo.MprTaskInfo::getMprPushResult).anyMatch(it -> it == 0)) {
                computeStatus = ComputeStatus.COMPUTE_FAILED;
            }
            if (applyTaskStatus == 2 && queryTaskStatus == 1 && pushResult == 1 && queryResult == 1
                && mprTaskInfoList.stream().map(LungTaskInfo.MprTaskInfo::getMprTaskStatus).allMatch(it -> it == 2)
                && mprTaskInfoList.stream().map(LungTaskInfo.MprTaskInfo::getMprPushResult).allMatch(it -> it == 1)) {
                computeStatus = ComputeStatus.COMPUTE_SUCCESS;
            }
            if (applyTaskStatus == -1 || queryTaskStatus == -1
                || mprTaskInfoList.stream().map(LungTaskInfo.MprTaskInfo::getMprTaskStatus).anyMatch(it -> it == -1)) {
                computeStatus = ComputeStatus.COMPUTE_ERROR;
            }
        }

        if (ComputeType.SPINE == computeType) {
            SpineRecogTaskPO spineRecogTaskPO = spineRecogTaskMapper.selectOne(Wrappers.<SpineRecogTaskPO>lambdaQuery()
                    .select(SpineRecogTaskPO::getTaskStatus, SpineRecogTaskPO::getRecogResult)
                    .eq(SpineRecogTaskPO::getComputeSeriesId, computeSeriesId));
            if (spineRecogTaskPO.getTaskStatus() == 0) {
                computeStatus = ComputeStatus.WAIT_COMPUTE;
            }
            if (spineRecogTaskPO.getTaskStatus() == -1) {
                computeStatus = ComputeStatus.COMPUTE_ERROR;
            }
            if (spineRecogTaskPO.getTaskStatus() == 1 && spineRecogTaskPO.getRecogResult() == 0) {
                computeStatus = ComputeStatus.COMPUTE_FAILED;
            }
            if (spineRecogTaskPO.getTaskStatus() == 1 && spineRecogTaskPO.getRecogResult() == 1) {
                computeStatus = ComputeStatus.COMPUTE_SUCCESS;
            }
        }

        computeSeriesMapper.update(new ComputeSeriesPO(), Wrappers.<ComputeSeriesPO>lambdaUpdate()
                .eq(ComputeSeriesPO::getComputeSeriesId, computeSeriesId)
                .set(ComputeSeriesPO::getComputeStatus, computeStatus)
        );
    }
}
