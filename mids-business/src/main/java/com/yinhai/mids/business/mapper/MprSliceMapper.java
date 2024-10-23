package com.yinhai.mids.business.mapper;

import com.yinhai.mids.business.entity.po.MprSlicePO;
import com.yinhai.mids.common.module.mybatis.SuperMapper;

import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/10/22
 */
public interface MprSliceMapper extends SuperMapper<MprSlicePO> {

    Map<String, Integer> queryViewTotal(String seriesId);
}
