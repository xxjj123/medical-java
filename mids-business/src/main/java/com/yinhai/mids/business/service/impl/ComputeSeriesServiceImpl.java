package com.yinhai.mids.business.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.ComputeType;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.dto.LungTaskInfo;
import com.yinhai.mids.business.entity.po.OldComputeSeriesPO;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
import com.yinhai.mids.business.mapper.OldComputeSeriesMapper;
import com.yinhai.mids.business.mapper.ComputeSeriesMapper;
import com.yinhai.mids.business.service.ComputeSeriesService;
import com.yinhai.mids.business.service.ComputeService;
import com.yinhai.mids.business.service.TaskLockService;
import com.yinhai.mids.business.util.TransactionKit;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/7/8 15:25
 */
@Service
@TaTransactional
public class ComputeSeriesServiceImpl implements ComputeSeriesService {

    private static final Log log = LogFactory.get();

    @Resource
    private OldComputeSeriesMapper oldComputeSeriesMapper;

    @Resource
    private ComputeService computeService;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private TaskLockService taskLockService;

    @Override
    public void reCompute(String computeSeriesId) {
        AppAssert.notBlank(computeSeriesId, "计算序列ID不能为空");
        AppAssert.notNull(oldComputeSeriesMapper.selectById(computeSeriesId), "计算序列不存在！");
        oldComputeSeriesMapper.update(new OldComputeSeriesPO(), Wrappers.<OldComputeSeriesPO>lambdaUpdate()
                .eq(OldComputeSeriesPO::getId, computeSeriesId)
                .set(OldComputeSeriesPO::getComputeStatus, ComputeStatus.WAIT_COMPUTE)
                .set(OldComputeSeriesPO::getErrorMessage, null)
                .set(OldComputeSeriesPO::getComputeResponse, null));
        taskLockService.unlock(TaskType.COMPUTE, computeSeriesId);
        TransactionKit.doAfterTxCommit(() -> computeService.lockedAsyncApplyCompute(computeSeriesId));
    }

    @Override
    public void onComputePush(Map<String, Object> pushParamMap) {
        String code = (String) pushParamMap.get("code");
        AppAssert.notBlank(code, "code为空");
        String applyId = (String) pushParamMap.get("applyId");
        AppAssert.notBlank(applyId, "applyId为空");
        OldComputeSeriesPO oldComputeSeriesPO = oldComputeSeriesMapper.selectOne(
                Wrappers.<OldComputeSeriesPO>lambdaQuery().eq(OldComputeSeriesPO::getApplyId, applyId));
        if (oldComputeSeriesPO == null) {
            log.error("applyId {} 对应计算序列不存在", applyId);
            return;
        }
        if (StrUtil.equals(code, "1")) {
            TransactionKit.doAfterTxCommit(() -> computeService.lockedAsyncQueryComputeResult(applyId));
        }
        if (StrUtil.equalsAny(code, "2", "3")) {
            oldComputeSeriesMapper.update(new OldComputeSeriesPO(), Wrappers.<OldComputeSeriesPO>lambdaUpdate()
                    .eq(OldComputeSeriesPO::getId, oldComputeSeriesPO.getId())
                    .set(OldComputeSeriesPO::getComputeStatus, ComputeStatus.COMPUTE_FAILED)
                    .set(OldComputeSeriesPO::getComputeResponse, JsonKit.toJsonString(pushParamMap))
                    .set(OldComputeSeriesPO::getErrorMessage, pushParamMap.get("message")));
        }
    }

    @Override
    public void refreshComputeStatus(String computeSeriesId) {
        ComputeSeriesPO computeSeries = computeSeriesMapper.selectById(computeSeriesId);
        if (computeSeries == null) {
            return;
        }

        String computeStatus = ComputeStatus.IN_COMPUTE;
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
        computeSeriesMapper.update(new ComputeSeriesPO(), Wrappers.<ComputeSeriesPO>lambdaUpdate()
                .eq(ComputeSeriesPO::getComputeSeriesId, computeSeriesId)
                .set(ComputeSeriesPO::getComputeStatus, Integer.valueOf(computeStatus))
        );
    }
}
