package com.yinhai.mids.business.service.impl;

import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.SeriesPO;
import com.yinhai.mids.business.mapper.SeriesMapper;
import com.yinhai.mids.business.service.SeriesService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhuhs
 * @date 2024/7/10 13:52
 */
@Service
@TaTransactional
public class SeriesServiceImpl implements SeriesService {

    @Resource
    private SeriesMapper seriesMapper;

    @Override
    public void reAnalyse(String seriesId) {
        AppAssert.notBlank(seriesId, "序列ID不能为空");
        AppAssert.notNull(seriesMapper.selectById(seriesId), "序列不存在！");
        seriesMapper.updateById(new SeriesPO().setId(seriesId).setComputeStatus(ComputeStatus.WAIT_COMPUTE));
    }
}
