package com.yinhai.mids.business.job;

import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.dto.KeyaApplyToDoTask;
import com.yinhai.mids.business.mapper.KeyaApplyTaskMapper;
import com.yinhai.mids.business.service.KeyaService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.PageKit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuhs
 * @date 2024/10/16
 */
@Component
public class KeyaApplyJob {

    @Resource
    private KeyaApplyTaskMapper applyTaskMapper;

    @Resource
    private KeyaService keyaService;

    @Scheduled(fixedRate = 30, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void execute() {
        // 加锁的目的是保证定时任务同时只有一个节点执行
        TaskLockManager.lock(TaskType.KEYA_APPLY, 30, () -> {
            PageKit.startPage(PageRequest.of(1, 1));
            List<KeyaApplyToDoTask> toDoTaskList = applyTaskMapper.queryTodoTasks();
            toDoTaskList.forEach(task -> keyaService.lockedAsyncApply(task));
        });
    }
}
