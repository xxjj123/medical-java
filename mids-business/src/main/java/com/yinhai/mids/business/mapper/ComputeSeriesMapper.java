package com.yinhai.mids.business.mapper;

import com.yinhai.mids.business.entity.dto.LungTaskInfo;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
import com.yinhai.mids.common.module.mybatis.SuperMapper;

/**
 * @author zhuhs
 * @date 2024/10/15
 */
public interface ComputeSeriesMapper extends SuperMapper<ComputeSeriesPO> {

    LungTaskInfo queryLungTaskInfo(String computeSeriesId);
}
