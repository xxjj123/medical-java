package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.dto.SpineRecogToDoTask;
import com.yinhai.mids.business.entity.vo.SpineInfoVO;

/**
 * @author zhuhs
 * @date 2024/12/2
 */
public interface SpineService {

    void recognize(SpineRecogToDoTask recogTask);

    void lockedAsyncApply(SpineRecogToDoTask recogTask);

    SpineInfoVO querySpineInfo(String computeSeriesId);
}
