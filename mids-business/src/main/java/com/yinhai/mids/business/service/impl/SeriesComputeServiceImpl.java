package com.yinhai.mids.business.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.SeriesComputePO;
import com.yinhai.mids.business.event.EventConstants;
import com.yinhai.mids.business.event.TxEventPublisher;
import com.yinhai.mids.business.mapper.SeriesComputeMapper;
import com.yinhai.mids.business.service.SeriesComputeService;
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
public class SeriesComputeServiceImpl implements SeriesComputeService {

    private static final Log log = LogFactory.get();

    @Resource
    private SeriesComputeMapper seriesComputeMapper;

    @Resource
    private TxEventPublisher eventPublisher;

    @Override
    public void reAnalyse(String seriesComputeId) {
        AppAssert.notBlank(seriesComputeId, "计算序列ID不能为空");
        AppAssert.notNull(seriesComputeMapper.selectById(seriesComputeId), "计算序列不存在！");
        seriesComputeMapper.updateById(new SeriesComputePO().setId(seriesComputeId)
                .setComputeStatus(ComputeStatus.WAIT_COMPUTE)
                .setErrorMessage(null)
                .setComputeResponse(null));
        eventPublisher.publish(seriesComputeId, EventConstants.COMPUTE_EVENT);
    }

    @Override
    public void onAnalysePush(Map<String, Object> pushParamMap) {
        String code = (String) pushParamMap.get("code");
        AppAssert.notBlank(code, "code为空");
        String applyId = (String) pushParamMap.get("applyId");
        AppAssert.notBlank(applyId, "applyId为空");
        if (StrUtil.equals(code, "1")) {
            eventPublisher.publish(applyId, EventConstants.COMPUTE_FINISH_EVENT);
        }
        if (StrUtil.equalsAny(code, "1", "2")) {
            seriesComputeMapper.update(new SeriesComputePO().setComputeStatus(ComputeStatus.COMPUTE_FAILED)
                            .setErrorMessage((String) pushParamMap.get("message"))
                            .setComputeResponse(JsonKit.toJsonString(pushParamMap)),
                    Wrappers.<SeriesComputePO>lambdaQuery().eq(SeriesComputePO::getApplyId, applyId));
        }
    }
}
