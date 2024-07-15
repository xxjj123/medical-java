package com.yinhai.mids.business.job;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.SeriesComputePO;
import com.yinhai.mids.business.mapper.SeriesComputeMapper;
import com.yinhai.mids.business.mapper.SeriesMapper;
import com.yinhai.mids.business.service.AnalysisService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.PageKit;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/10 11:29
 */
@Component
public class SeriesComputeJob {

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private SeriesComputeMapper seriesComputeMapper;

    @Resource
    private AnalysisService analysisService;

    @XxlJob("register")
    public void register() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<SeriesComputePO> seriesComputePOList = seriesComputeMapper.selectList(
                Wrappers.<SeriesComputePO>lambdaQuery()
                        .eq(SeriesComputePO::getComputeStatus, ComputeStatus.WAIT_COMPUTE)
                        .orderByAsc(SeriesComputePO::getCreateTime)
        );
        if (CollUtil.isEmpty(seriesComputePOList)) {
            return;
        }
        for (SeriesComputePO seriesComputePO : seriesComputePOList) {
            analysisService.register(seriesComputePO.getId());
        }
    }

    @XxlJob("result")
    public void result() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<SeriesComputePO> seriesComputePOList = seriesComputeMapper.selectList(
                Wrappers.<SeriesComputePO>lambdaQuery()
                        .eq(SeriesComputePO::getComputeStatus, ComputeStatus.IN_COMPUTE)
                        .orderByAsc(SeriesComputePO::getComputeStartTime)
        );
        if (CollUtil.isEmpty(seriesComputePOList)) {
            return;
        }
        for (SeriesComputePO seriesComputePO : seriesComputePOList) {
            analysisService.result(seriesComputePO.getApplyId());
        }
    }

}
