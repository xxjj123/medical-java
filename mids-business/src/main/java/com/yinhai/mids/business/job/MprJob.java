package com.yinhai.mids.business.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.dto.MprToDoTask;
import com.yinhai.mids.business.mapper.MprTaskMapper;
import com.yinhai.mids.business.service.MprService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.PageKit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuhs
 * @date 2024/10/21
 */
@Component
public class MprJob {

    private static final Log log = LogFactory.get();

    @Resource
    private MprTaskMapper mprTaskMapper;

    @Resource
    private MprService mprService;

    @Scheduled(fixedRate = 30, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void execute() {
        // 加锁的目的是保证定时任务同时只有一个节点执行
        TaskLockManager.lock(TaskType.MPR, 30, () -> {
            PageKit.startPage(PageRequest.of(1, 5));
            List<MprToDoTask> toDoTaskList = mprTaskMapper.queryTodoTasks();
            if (CollUtil.isNotEmpty(toDoTaskList)) {
                log.debug("ToDoTasks: {}, {}", "MPR", toDoTaskList.size());
                toDoTaskList.forEach(task -> mprService.lockedAsyncMpr(task));
            }
        });
    }
}
