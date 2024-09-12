package com.yinhai.mids.business.service;

/**
 * @author zhuhs
 * @date 2024/7/15 15:45
 */
public interface ComputeService {

    void applyCompute(String computeSeriesId);

    /**
     * 异步发起AI计算，并且保证同时只有一处执行
     *
     * @param computeSeriesId 计算序列ID
     */
    void lockedAsyncApplyCompute(String computeSeriesId);

    void queryComputeResult(String applyId);

    /**
     * 异步查询AI计算结果，并且保证同时只有一处执行
     *
     * @param applyId applyId
     */
    void lockedAsyncQueryComputeResult(String applyId);
}
