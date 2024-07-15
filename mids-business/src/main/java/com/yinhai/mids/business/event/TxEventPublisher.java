package com.yinhai.mids.business.event;

import com.yinhai.ta404.core.event.EventPublish;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;

/**
 * @author zhuhs
 * @date 2024/7/15 16:02
 */
@Component
public class TxEventPublisher {

    @Resource
    private EventPublish eventPublish;


    /**
     * 在当前所在事务提交后执行事件发布
     */
    public void publish(Object object, String eventId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublish.publish(object, eventId);
            }
        });
    }

}
