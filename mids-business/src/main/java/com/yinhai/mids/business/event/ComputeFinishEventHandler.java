package com.yinhai.mids.business.event;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.service.ComputeService;
import com.yinhai.ta404.core.event.async.AbstractEventHandler;
import com.yinhai.ta404.core.event.async.annotation.Consumer;
import com.yinhai.ta404.core.event.async.disruptor.IEventDisruptor;
import com.yinhai.ta404.core.event.async.message.IEventMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zhuhs
 * @date 2024/7/15 14:31
 */
@Consumer(value = EventConstants.COMPUTE_FINISH_EVENT, async = true)
@Component
public class ComputeFinishEventHandler implements AbstractEventHandler {

    private static final Log log = LogFactory.get();

    @Resource
    private ComputeService computeService;

    @Override
    public void onEvent(IEventDisruptor eventDisruptor, long sequence, boolean endOfBatch) throws Exception {
        IEventMessage eventMessage = eventDisruptor.getEventMessage();
        String applyId = (String) eventMessage.getEventSource();
        computeService.result(applyId);
    }
}
