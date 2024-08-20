package com.yinhai.mids.business.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
import com.yinhai.mids.business.event.EventConstants;
import com.yinhai.mids.business.event.TxEventPublisher;
import com.yinhai.mids.business.mapper.ComputeSeriesMapper;
import com.yinhai.mids.business.service.ComputeSeriesService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

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

    @Resource
    private TxEventPublisher eventPublisher;

    @Override
    public void reCompute(String computeSeriesId) {
        AppAssert.notBlank(computeSeriesId, "计算序列ID不能为空");
        AppAssert.notNull(computeSeriesMapper.selectById(computeSeriesId), "计算序列不存在！");
        computeSeriesMapper.updateById(new ComputeSeriesPO().setId(computeSeriesId)
                .setComputeStatus(ComputeStatus.WAIT_COMPUTE)
                .setErrorMessage(null)
                .setComputeResponse(null));
        eventPublisher.publish(computeSeriesId, EventConstants.COMPUTE_EVENT);
    }

    @Override
    public void onComputePush(Map<String, Object> pushParamMap) {
        String code = (String) pushParamMap.get("code");
        AppAssert.notBlank(code, "code为空");
        String applyId = (String) pushParamMap.get("applyId");
        AppAssert.notBlank(applyId, "applyId为空");
        ComputeSeriesPO computeSeriesPO = computeSeriesMapper.selectOne(
                Wrappers.<ComputeSeriesPO>lambdaQuery().eq(ComputeSeriesPO::getApplyId, applyId));
        if (computeSeriesPO == null) {
            log.error("applyId {} 对应计算序列不存在", applyId);
            return;
        }
        if (StrUtil.equals(code, "1")) {
            eventPublisher.publish(applyId, EventConstants.COMPUTE_FINISH_EVENT);
        }
        if (StrUtil.equalsAny(code, "2", "3")) {
            computeSeriesMapper.updateById(new ComputeSeriesPO().setId(computeSeriesPO.getId())
                    .setComputeStatus(ComputeStatus.COMPUTE_FAILED)
                    .setErrorMessage((String) pushParamMap.get("message"))
                    .setComputeResponse(JsonKit.toJsonString(pushParamMap)));
        }
    }
}
