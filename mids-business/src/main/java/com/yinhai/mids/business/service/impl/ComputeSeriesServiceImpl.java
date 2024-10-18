package com.yinhai.mids.business.service.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.ComputeType;
import com.yinhai.mids.business.entity.dto.LungTaskInfo;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
import com.yinhai.mids.business.mapper.ComputeSeriesMapper;
import com.yinhai.mids.business.service.ComputeSeriesService;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhuhs
 * @date 2024/7/8 15:25
 */
@Service
@TaTransactional
public class ComputeSeriesServiceImpl implements ComputeSeriesService {

    private static final Log log = LogFactory.get();

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Override
    public void refreshComputeStatus(String computeSeriesId) {
        ComputeSeriesPO computeSeries = computeSeriesMapper.selectById(computeSeriesId);
        if (computeSeries == null) {
            return;
        }

        String computeStatus = ComputeStatus.IN_COMPUTE;
        Integer computeType = computeSeries.getComputeType();
        if (ComputeType.LUNG == computeType) {
            LungTaskInfo lungTaskInfo = computeSeriesMapper.queryLungTaskInfo(computeSeriesId);
            if (lungTaskInfo == null) {
                return;
            }
            Integer applyTaskStatus = lungTaskInfo.getApplyTaskStatus();
            Integer applyResult = lungTaskInfo.getApplyResult();
            Integer pushResult = lungTaskInfo.getPushResult();
            Integer queryTaskStatus = lungTaskInfo.getQueryTaskStatus();
            Integer queryResult = lungTaskInfo.getQueryResult();
            Integer mprTaskStatus = lungTaskInfo.getMprTaskStatus();
            Integer mprResult = lungTaskInfo.getMprResult();

            if (applyTaskStatus == 0 && mprTaskStatus == 0) {
                computeStatus = ComputeStatus.WAIT_COMPUTE;
            }
            if (applyResult == 0 || pushResult == 0 || mprResult == 0 || queryResult == 0) {
                computeStatus = ComputeStatus.COMPUTE_FAILED;
            }
            if (queryTaskStatus == 1 && mprTaskStatus == 2 && queryResult == 1 && mprResult == 1) {
                computeStatus = ComputeStatus.COMPUTE_SUCCESS;
            }
            if (applyTaskStatus == -1 || queryTaskStatus == -1 || mprTaskStatus == -1) {
                computeStatus = ComputeStatus.COMPUTE_ERROR;
            }
        }
        computeSeriesMapper.update(new ComputeSeriesPO(), Wrappers.<ComputeSeriesPO>lambdaUpdate()
                .eq(ComputeSeriesPO::getComputeSeriesId, computeSeriesId)
                .set(ComputeSeriesPO::getComputeStatus, Integer.valueOf(computeStatus))
        );
    }
}
