package com.yinhai.mids.business.job;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.SeriesPO;
import com.yinhai.mids.business.event.EventConstants;
import com.yinhai.mids.business.mapper.SeriesMapper;
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
 * @date 2024/9/6
 */
@Component
public class MprAnalyseJob {

    @Resource
    private EventPublish eventPublish;

    @Resource
    private SeriesMapper seriesMapper;

    @Scheduled(fixedRate = 30000)
    @SuppressWarnings("unchecked")
    public void mpr() {
        PageKit.startPage(PageRequest.of(1, 3));
        List<SeriesPO> seriesPOList = seriesMapper.selectList(Wrappers.<SeriesPO>lambdaQuery().select(SeriesPO::getId)
                .and(q -> q.eq(SeriesPO::getMprStatus, ComputeStatus.WAIT_COMPUTE).or(w -> w
                        .eq(SeriesPO::getMprStatus, ComputeStatus.IN_COMPUTE)
                        .lt(SeriesPO::getMprStartTime, DateUtil.offsetMinute(DbKit.now(), -5))))
                .orderByAsc(SeriesPO::getCreateTime));
        seriesPOList.forEach(e -> eventPublish.publish(e.getId(), EventConstants.MPR_EVENT));
    }
}
