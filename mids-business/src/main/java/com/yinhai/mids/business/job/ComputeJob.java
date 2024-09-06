package com.yinhai.mids.business.job;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
import com.yinhai.mids.business.event.EventConstants;
import com.yinhai.mids.business.mapper.ComputeSeriesMapper;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.DbKit;
import com.yinhai.mids.common.util.PageKit;
import com.yinhai.ta404.core.event.EventPublish;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/10 11:29
 */
@Component
public class ComputeJob {

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private EventPublish eventPublish;

    @Scheduled(fixedRate = 30000)
    @SuppressWarnings("unchecked")
    public void compute() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<ComputeSeriesPO> seriesList = computeSeriesMapper.selectList(Wrappers.<ComputeSeriesPO>lambdaQuery()
                        .select(ComputeSeriesPO::getId).eq(ComputeSeriesPO::getComputeStatus, ComputeStatus.WAIT_COMPUTE)
                        .orderByAsc(ComputeSeriesPO::getCreateTime));
        seriesList.forEach(e -> eventPublish.publish(e.getId(), EventConstants.COMPUTE_EVENT));
    }

    @Scheduled(fixedRate = 30000)
    @SuppressWarnings("unchecked")
    public void computeResult() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<ComputeSeriesPO> seriesList = computeSeriesMapper.selectList(Wrappers.<ComputeSeriesPO>lambdaQuery()
                        .select(ComputeSeriesPO::getApplyId).eq(ComputeSeriesPO::getComputeStatus, ComputeStatus.IN_COMPUTE)
                        .lt(ComputeSeriesPO::getComputeStartTime, DateUtil.offsetMinute(DbKit.now(), -5))
                        .orderByAsc(ComputeSeriesPO::getComputeStartTime));
        seriesList.forEach(e -> eventPublish.publish(e.getApplyId(), EventConstants.COMPUTE_RESULT_EVENT));
    }
}
