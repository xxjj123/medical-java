package com.yinhai.mids.business.mapper;

import com.yinhai.mids.business.entity.dto.ViewCount;
import com.yinhai.mids.business.entity.po.MprSlicePO;
import com.yinhai.mids.common.module.mybatis.SuperMapper;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/10/22
 */
public interface MprSliceMapper extends SuperMapper<MprSlicePO> {

    List<ViewCount> queryViewTotal(String seriesId);
}
