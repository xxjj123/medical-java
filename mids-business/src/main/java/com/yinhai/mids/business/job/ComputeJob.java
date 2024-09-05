package com.yinhai.mids.business.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
import com.yinhai.mids.business.mapper.ComputeSeriesMapper;
import com.yinhai.mids.business.mapper.SeriesMapper;
import com.yinhai.mids.business.service.ComputeService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.mids.common.util.PageKit;
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
    private SeriesMapper seriesMapper;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private ComputeService computeService;

    public void register() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<ComputeSeriesPO> computeSeriesPOList = computeSeriesMapper.selectList(
                Wrappers.<ComputeSeriesPO>lambdaQuery()
                        .eq(ComputeSeriesPO::getComputeStatus, ComputeStatus.WAIT_COMPUTE)
                        .orderByAsc(ComputeSeriesPO::getCreateTime)
        );
        if (CollUtil.isEmpty(computeSeriesPOList)) {
            return;
        }
        for (ComputeSeriesPO computeSeriesPO : computeSeriesPOList) {
            computeService.register(computeSeriesPO.getId());
        }
    }

    public void result() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<ComputeSeriesPO> computeSeriesPOList = computeSeriesMapper.selectList(
                Wrappers.<ComputeSeriesPO>lambdaQuery()
                        .eq(ComputeSeriesPO::getComputeStatus, ComputeStatus.IN_COMPUTE)
                        .lt(ComputeSeriesPO::getComputeStartTime, DateUtil.offsetMinute(MapperKit.executeForDate(), -5))
                        .orderByAsc(ComputeSeriesPO::getComputeStartTime)
        );
        if (CollUtil.isEmpty(computeSeriesPOList)) {
            return;
        }
        for (ComputeSeriesPO computeSeriesPO : computeSeriesPOList) {
            computeService.result(computeSeriesPO.getApplyId());
        }
    }

}
