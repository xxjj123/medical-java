package com.yinhai.mids.business.service;

import com.yinhai.mids.business.constant.TaskType;

/**
 * @author zhuhs
 * @date 2024/9/4
 */
public interface TaskLockService {

    /**
     * 尝试锁住指定任务
     *
     * @param taskType      任务类型
     * @param itemId        任务对象ID
     * @param expireSeconds 过期时间，超过该时间则锁定失效。如果传0或负值则不会过期
     * @return boolean
     */
    boolean tryLock(TaskType taskType, String itemId, int expireSeconds);
}
