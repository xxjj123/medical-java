package com.yinhai.mids.business.event;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.service.AnalyseService;
import com.yinhai.ta404.core.event.async.AbstractEventHandler;
import com.yinhai.ta404.core.event.async.annotation.Consumer;
import com.yinhai.ta404.core.event.async.disruptor.IEventDisruptor;
import com.yinhai.ta404.core.event.async.message.IEventMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zhuhs
 * @date 2024/7/22 11:38
 */
@Consumer(value = EventConstants.ANALYSE_EVENT, async = true)
@Component
public class AnalyseEventHandler implements AbstractEventHandler {

    private static final Log log = LogFactory.get();

    @Resource
    private AnalyseService analyseService;


    @Override
    public void onEvent(IEventDisruptor eventDisruptor, long sequence, boolean endOfBatch) throws Exception {
        IEventMessage eventMessage = eventDisruptor.getEventMessage();
        String seriesId = (String) eventMessage.getEventSource();
        analyseService.doMprAnalyse(seriesId);
    }
}
