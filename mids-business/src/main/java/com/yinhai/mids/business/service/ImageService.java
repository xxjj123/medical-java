package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.vo.ImageInitInfoVO;

/**
 * @author zhuhs
 * @date 2024/8/27
 */
public interface ImageService {

    /**
     * 查询影像初始化数据
     *
     * @param computeSeriesId computeSeriesId
     * @return {@link ImageInitInfoVO }
     * @author zhuhs 2024/08/27
     */
    ImageInitInfoVO queryInitInfo(String computeSeriesId);

}
