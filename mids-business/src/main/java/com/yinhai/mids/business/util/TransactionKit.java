package com.yinhai.mids.business.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author zhuhs
 * @date 2024/9/11
 */
public class TransactionKit {

    /**
     * 在当前事务提交后执行任务。如果非事务环境会报错
     *
     * @param runnable 任务
     */
    public static void doAfterTxCommit(Runnable runnable) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }
}
