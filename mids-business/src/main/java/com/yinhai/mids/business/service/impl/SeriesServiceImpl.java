package com.yinhai.mids.business.service.impl;

import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.SeriesComputePO;
import com.yinhai.mids.business.mapper.SeriesComputeMapper;
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
    private SeriesComputeMapper seriesComputeMapper;

    @Override
    public void reAnalyse(String seriesComputeId) {
        AppAssert.notBlank(seriesComputeId, "计算序列ID不能为空");
        AppAssert.notNull(seriesComputeMapper.selectById(seriesComputeId), "计算序列不存在！");
        seriesComputeMapper.updateById(new SeriesComputePO().setId(seriesComputeId)
                .setComputeStatus(ComputeStatus.WAIT_COMPUTE));
    }
}
