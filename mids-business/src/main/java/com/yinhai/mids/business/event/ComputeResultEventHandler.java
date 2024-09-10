package com.yinhai.mids.business.event;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.service.ComputeService;
import com.yinhai.mids.business.service.TaskLockService;
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
@Consumer(value = EventConstants.COMPUTE_RESULT_EVENT, async = true)
@Component
public class ComputeResultEventHandler implements AbstractEventHandler {

    private static final Log log = LogFactory.get();

    @Resource
    private ComputeService computeService;

    @Resource
    private TaskLockService taskLockService;

    @Override
    public void onEvent(IEventDisruptor eventDisruptor, long sequence, boolean endOfBatch) throws Exception {
        IEventMessage eventMessage = eventDisruptor.getEventMessage();
        String applyId = (String) eventMessage.getEventSource();
        boolean locked = taskLockService.tryLock(TaskType.COMPUTE_RESULT, applyId, 2 * 60);
        if (locked) {
            computeService.queryComputeResult(applyId);
            taskLockService.unlock(TaskType.COMPUTE_RESULT, applyId);
        }
    }
}
