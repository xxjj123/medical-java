package com.yinhai.mids.business.job;

import cn.hutool.core.date.DateUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.SeriesPO;
import com.yinhai.mids.business.mapper.SeriesMapper;
import com.yinhai.mids.business.service.MprService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.mids.common.util.PageKit;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuhs
 * @date 2024/9/6
 */
// @Component
public class MprAnalyseJob {

    private static final Log log = LogFactory.get();

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private MprService mprService;

    @Scheduled(fixedRate = 30, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    @SuppressWarnings("unchecked")
    public void mpr() {
        PageKit.startPage(PageRequest.of(1, 3));
        List<SeriesPO> seriesPOList = seriesMapper.selectList(Wrappers.<SeriesPO>lambdaQuery().select(SeriesPO::getId)
                .and(q -> q.eq(SeriesPO::getMprStatus, ComputeStatus.WAIT_COMPUTE).or(w -> w
                        .eq(SeriesPO::getMprStatus, ComputeStatus.IN_COMPUTE)
                        .lt(SeriesPO::getMprStartTime, DateUtil.offsetMinute(DbClock.now(), -8))))
                .orderByAsc(SeriesPO::getCreateTime));
        seriesPOList.forEach(e -> log.warn(e.getId()));
        seriesPOList.forEach(e -> mprService.lockedAsyncDoMprAnalyse(e.getId()));
    }
}
