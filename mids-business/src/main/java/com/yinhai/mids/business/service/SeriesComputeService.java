package com.yinhai.mids.business.service;

import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/7/15 15:02
 */
public interface SeriesComputeService {

    void reAnalyse(String seriesComputeId);

    void onAnalysePush(Map<String, Object> pushParamMap);
}
