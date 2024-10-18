package com.yinhai.mids.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.ComputeType;
import com.yinhai.mids.business.entity.dto.LungTaskInfo;
import com.yinhai.mids.business.entity.po.ComputeTaskPO;
import com.yinhai.mids.business.mapper.ComputeTaskMapper;
import com.yinhai.mids.business.service.ComputeTaskService;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhuhs
 * @date 2024/10/17
 */
@Service
@TaTransactional
public class ComputeTaskServiceImpl implements ComputeTaskService {

    @Resource
    private ComputeTaskMapper computeTaskMapper;

    @Override
    public void refreshComputeStatus(String computeTaskId) {
        ComputeTaskPO computeTask = computeTaskMapper.selectById(computeTaskId);
        if (computeTask == null) {
            return;
        }

        String computeStatus = ComputeStatus.IN_COMPUTE;
        Integer computeType = computeTask.getComputeType();
        if (ComputeType.LUNG == computeType) {
            LungTaskInfo lungTaskInfo = computeTaskMapper.queryLungTaskInfo(computeTaskId);
            if (lungTaskInfo == null) {
                return;
            }
            Integer applyTaskStatus = lungTaskInfo.getApplyTaskStatus();
            Integer applyResult = lungTaskInfo.getApplyResult();
            Integer pushResult = lungTaskInfo.getPushResult();
            Integer queryTaskStatus = lungTaskInfo.getQueryTaskStatus();
            Integer queryResult = lungTaskInfo.getQueryResult();
            Integer mprTaskStatus = lungTaskInfo.getMprTaskStatus();
            Integer mprResult = lungTaskInfo.getMprResult();

            if (applyTaskStatus == 0 && mprTaskStatus == 0) {
                computeStatus = ComputeStatus.WAIT_COMPUTE;
            }
            if (applyResult == 0 || pushResult == 0 || mprResult == 0 || queryResult == 0) {
                computeStatus = ComputeStatus.COMPUTE_FAILED;
            }
            if (queryTaskStatus == 1 && mprTaskStatus == 2 && queryResult == 1 && mprResult == 1) {
                computeStatus = ComputeStatus.COMPUTE_SUCCESS;
            }
            if (applyTaskStatus == -1 || queryTaskStatus == -1 || mprTaskStatus == -1) {
                computeStatus = ComputeStatus.COMPUTE_ERROR;
            }
        }
        computeTaskMapper.update(new ComputeTaskPO(), Wrappers.<ComputeTaskPO>lambdaUpdate()
                .eq(ComputeTaskPO::getComputeTaskId, computeTaskId)
                .set(ComputeTaskPO::getComputeStatus, Integer.valueOf(computeStatus))
        );
    }
}
