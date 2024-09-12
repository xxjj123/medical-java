package com.yinhai.mids.business.service;

/**
 * @author zhuhs
 * @date 2024/7/18 15:23
 */
public interface MprService {

    void doMprAnalyse(String seriesId);

    /**
     * 异步执行三维分析，并且保证同时只有一处执行
     *
     * @param seriesId 序列ID
     */
    void lockedAsyncDoMprAnalyse(String seriesId);
}
