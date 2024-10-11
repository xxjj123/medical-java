package com.yinhai.mids.business.job;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
import com.yinhai.mids.business.mapper.ComputeSeriesMapper;
import com.yinhai.mids.business.service.ComputeService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.DbKit;
import com.yinhai.mids.common.util.PageKit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuhs
 * @date 2024/7/10 11:29
 */
@Component
public class ComputeJob {

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private ComputeService computeService;

    @Scheduled(fixedRate = 30, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    @SuppressWarnings("unchecked")
    public void compute() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<ComputeSeriesPO> seriesList = computeSeriesMapper.selectList(Wrappers.<ComputeSeriesPO>lambdaQuery()
                        .select(ComputeSeriesPO::getId).eq(ComputeSeriesPO::getComputeStatus, ComputeStatus.WAIT_COMPUTE)
                        .orderByAsc(ComputeSeriesPO::getCreateTime));
        seriesList.forEach(e -> computeService.lockedAsyncApplyCompute(e.getId()));
    }

    @Scheduled(fixedRate = 10, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    @SuppressWarnings("unchecked")
    public void computeResult() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<ComputeSeriesPO> seriesList = computeSeriesMapper.selectList(Wrappers.<ComputeSeriesPO>lambdaQuery()
                        .select(ComputeSeriesPO::getApplyId).eq(ComputeSeriesPO::getComputeStatus, ComputeStatus.IN_COMPUTE)
                        .lt(ComputeSeriesPO::getComputeStartTime, DateUtil.offsetMinute(DbKit.now(), -5))
                        .orderByAsc(ComputeSeriesPO::getComputeStartTime));
        seriesList.forEach(e -> computeService.lockedAsyncQueryComputeResult(e.getApplyId()));
    }
}
