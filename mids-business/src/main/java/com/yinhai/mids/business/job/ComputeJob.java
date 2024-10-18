package com.yinhai.mids.business.job;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.OldComputeSeriesPO;
import com.yinhai.mids.business.mapper.OldComputeSeriesMapper;
import com.yinhai.mids.business.service.ComputeService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.mids.common.util.PageKit;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuhs
 * @date 2024/7/10 11:29
 */
// @Component
public class ComputeJob {

    @Resource
    private OldComputeSeriesMapper oldComputeSeriesMapper;

    @Resource
    private ComputeService computeService;

    @Scheduled(fixedRate = 30, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    @SuppressWarnings("unchecked")
    public void compute() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<OldComputeSeriesPO> seriesList = oldComputeSeriesMapper.selectList(Wrappers.<OldComputeSeriesPO>lambdaQuery()
                .select(OldComputeSeriesPO::getId).eq(OldComputeSeriesPO::getComputeStatus, ComputeStatus.WAIT_COMPUTE)
                .orderByAsc(OldComputeSeriesPO::getCreateTime));
        seriesList.forEach(e -> computeService.lockedAsyncApplyCompute(e.getId()));
    }

    @Scheduled(fixedRate = 10, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    @SuppressWarnings("unchecked")
    public void computeResult() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<OldComputeSeriesPO> seriesList = oldComputeSeriesMapper.selectList(Wrappers.<OldComputeSeriesPO>lambdaQuery()
                .select(OldComputeSeriesPO::getApplyId).eq(OldComputeSeriesPO::getComputeStatus, ComputeStatus.IN_COMPUTE)
                .lt(OldComputeSeriesPO::getComputeStartTime, DateUtil.offsetMinute(DbClock.now(), -5))
                .orderByAsc(OldComputeSeriesPO::getComputeStartTime));
        seriesList.forEach(e -> computeService.lockedAsyncQueryComputeResult(e.getApplyId()));
    }
}
