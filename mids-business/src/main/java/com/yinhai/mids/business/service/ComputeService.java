package com.yinhai.mids.business.service;

/**
 * @author zhuhs
 * @date 2024/7/15 15:45
 */
public interface ComputeService {

    void applyCompute(String computeSeriesId);

    void queryComputeResult(String applyId);
}
