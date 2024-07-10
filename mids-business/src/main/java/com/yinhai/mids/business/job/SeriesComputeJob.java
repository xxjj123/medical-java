package com.yinhai.mids.business.job;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yinhai.mids.business.analysis.AnalyseEngine;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.SeriesPO;
import com.yinhai.mids.business.mapper.SeriesMapper;
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
    private AnalyseEngine analyseEngine;

    @XxlJob("register")
    public void register() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<SeriesPO> seriesPOList = seriesMapper.selectList(Wrappers.<SeriesPO>lambdaQuery()
                .eq(SeriesPO::getComputeStatus, ComputeStatus.WAIT_COMPUTE).orderByAsc(SeriesPO::getCreateTime));
        if (CollUtil.isEmpty(seriesPOList)) {
            return;
        }
        for (SeriesPO seriesPO : seriesPOList) {
            analyseEngine.register(seriesPO);
        }
    }

    @XxlJob("result")
    public void result() {
        // 控制每次发起的数量
        PageKit.startPage(PageRequest.of(1, 2));
        List<SeriesPO> seriesPOList = seriesMapper.selectList(Wrappers.<SeriesPO>lambdaQuery()
                .eq(SeriesPO::getComputeStatus, ComputeStatus.IN_COMPUTE).orderByAsc(SeriesPO::getCreateTime));
        if (CollUtil.isEmpty(seriesPOList)) {
            return;
        }
        for (SeriesPO seriesPO : seriesPOList) {
            analyseEngine.result(seriesPO);
        }
    }

}
