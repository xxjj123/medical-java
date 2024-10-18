package com.yinhai.mids.business.job;

import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.dto.KeyaQueryToDoTask;
import com.yinhai.mids.business.mapper.KeyaQueryTaskMapper;
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
public class KeyaQueryJob {

    @Resource
    private KeyaQueryTaskMapper queryTaskMapper;

    @Resource
    private KeyaService keyaService;

    @Scheduled(fixedRate = 30, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void execute() {
        // 加锁的目的是保证定时任务同时只有一个节点执行
        TaskLockManager.lock(TaskType.KEYA_QUERY, 30, () -> {
            PageKit.startPage(PageRequest.of(1, 5));
            List<KeyaQueryToDoTask> toDoTaskList = queryTaskMapper.queryTodoTasks();
            toDoTaskList.forEach(task -> keyaService.lockedAsyncQuery(task));
        });
    }
}
