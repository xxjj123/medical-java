package com.yinhai.mids.business.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
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
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private ComputeService computeService;

    @Resource
    private TaskLockService taskLockService;

    @Override
    public void reCompute(String computeSeriesId) {
        AppAssert.notBlank(computeSeriesId, "计算序列ID不能为空");
        AppAssert.notNull(computeSeriesMapper.selectById(computeSeriesId), "计算序列不存在！");
        computeSeriesMapper.update(new ComputeSeriesPO(), Wrappers.<ComputeSeriesPO>lambdaUpdate()
                .eq(ComputeSeriesPO::getId, computeSeriesId)
                .set(ComputeSeriesPO::getComputeStatus, ComputeStatus.WAIT_COMPUTE)
                .set(ComputeSeriesPO::getErrorMessage, null)
                .set(ComputeSeriesPO::getComputeResponse, null));
        taskLockService.unlock(TaskType.COMPUTE, computeSeriesId);
        TransactionKit.doAfterTxCommit(() -> computeService.lockedAsyncApplyCompute(computeSeriesId));
    }

    @Override
    public void onComputePush(Map<String, Object> pushParamMap) {
        String code = (String) pushParamMap.get("code");
        AppAssert.notBlank(code, "code为空");
        String applyId = (String) pushParamMap.get("applyId");
        AppAssert.notBlank(applyId, "applyId为空");
        ComputeSeriesPO computeSeriesPO = computeSeriesMapper.selectOne(
                Wrappers.<ComputeSeriesPO>lambdaQuery().eq(ComputeSeriesPO::getApplyId, applyId));
        if (computeSeriesPO == null) {
            log.error("applyId {} 对应计算序列不存在", applyId);
            return;
        }
        if (StrUtil.equals(code, "1")) {
            TransactionKit.doAfterTxCommit(() -> computeService.lockedAsyncQueryComputeResult(applyId));
        }
        if (StrUtil.equalsAny(code, "2", "3")) {
            computeSeriesMapper.update(new ComputeSeriesPO(), Wrappers.<ComputeSeriesPO>lambdaUpdate()
                    .eq(ComputeSeriesPO::getId, computeSeriesPO.getId())
                    .set(ComputeSeriesPO::getComputeStatus, ComputeStatus.COMPUTE_FAILED)
                    .set(ComputeSeriesPO::getComputeResponse, JsonKit.toJsonString(pushParamMap))
                    .set(ComputeSeriesPO::getErrorMessage, pushParamMap.get("message")));
        }
    }
}
