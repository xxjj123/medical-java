package com.yinhai.mids.business.job;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.service.TaskLockService;
import com.yinhai.ta404.core.utils.ServiceLocator;

/**
 * @author zhuhs
 * @date 2024/10/16
 */
public class TaskLockManager {

    private static final Log log = LogFactory.get();

    private static final String TASK_ID = "0";

    public static void lock(TaskType taskType, String itemId, int expireSeconds, Runnable runnable) {
        TaskLockService taskLockService = ServiceLocator.getService(TaskLockService.class);
        boolean locked = taskLockService.tryLock(taskType, itemId, expireSeconds);
        if (!locked) {
            return;
        }
        try {
            runnable.run();
        } finally {
            taskLockService.unlock(taskType, itemId);
        }
    }

    public static void lock(TaskType taskType, int expireSeconds, Runnable runnable) {
        lock(taskType, TASK_ID, expireSeconds, runnable);
    }
}
