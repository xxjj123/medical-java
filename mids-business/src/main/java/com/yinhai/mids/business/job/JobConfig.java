package com.yinhai.mids.business.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.github.pagehelper.Page;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.dto.KeyaApplyToDoTask;
import com.yinhai.mids.business.entity.dto.KeyaQueryToDoTask;
import com.yinhai.mids.business.entity.dto.MprToDoTask;
import com.yinhai.mids.business.entity.dto.SpineRecogToDoTask;
import com.yinhai.mids.business.mapper.KeyaApplyTaskMapper;
import com.yinhai.mids.business.mapper.KeyaQueryTaskMapper;
import com.yinhai.mids.business.mapper.MprTaskMapper;
import com.yinhai.mids.business.mapper.SpineRecogTaskMapper;
import com.yinhai.mids.business.service.KeyaService;
import com.yinhai.mids.business.service.MprService;
import com.yinhai.mids.business.service.SpineService;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.mids.common.util.PageKit;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时任务配置类
 *
 * @author zhuhs
 * @date 2024/10/23
 */
@Configuration
public class JobConfig implements SchedulingConfigurer {

    private static final Log log = LogFactory.get();

    @Resource
    private KeyaApplyTaskMapper applyTaskMapper;

    @Resource
    private KeyaQueryTaskMapper queryTaskMapper;

    @Resource
    private MprTaskMapper mprTaskMapper;

    @Resource
    private SpineRecogTaskMapper spineRecogTaskMapper;

    @Resource
    private KeyaService keyaService;

    @Resource
    private MprService mprService;

    @Resource
    private SpineService spineService;

    @Override
    public void configureTasks(@NotNull ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("job-thread-");
        scheduler.initialize();
        taskRegistrar.setScheduler(scheduler);

        taskRegistrar.addFixedRateTask(createTask(TaskType.KEYA_APPLY, this::keyaApply, 30));
        taskRegistrar.addFixedRateTask(createTask(TaskType.KEYA_QUERY, this::keyaQuery, 10));
        taskRegistrar.addFixedRateTask(createTask(TaskType.MPR, this::mpr, 30));
        taskRegistrar.addFixedRateTask(createTask(TaskType.SPINE_RECOG, this::spineRecognize, 10));
    }

    public void keyaApply() {
        PageKit.startPage(PageRequest.of(1, 1));
        List<KeyaApplyToDoTask> toDoTaskList = applyTaskMapper.queryTodoTasks();
        if (CollUtil.isNotEmpty(toDoTaskList)) {
            logTaskTotal(TaskType.KEYA_APPLY, toDoTaskList);
            toDoTaskList.forEach(task -> keyaService.lockedAsyncApply(task));
        }
    }

    public void keyaQuery() {
        PageKit.startPage(PageRequest.of(1, 10));
        List<KeyaQueryToDoTask> toDoTaskList = queryTaskMapper.queryTodoTasks();
        if (CollUtil.isNotEmpty(toDoTaskList)) {
            logTaskTotal(TaskType.KEYA_QUERY, toDoTaskList);
            toDoTaskList.forEach(task -> keyaService.lockedAsyncQuery(task));
        }
    }

    public void mpr() {
        PageKit.startPage(PageRequest.of(1, 5));
        List<MprToDoTask> toDoTaskList = mprTaskMapper.queryTodoTasks();
        if (CollUtil.isNotEmpty(toDoTaskList)) {
            logTaskTotal(TaskType.MPR, toDoTaskList);
            toDoTaskList.forEach(task -> mprService.lockedAsyncMpr(task));
        }
    }

    public void spineRecognize() {
        PageKit.startPage(PageRequest.of(1, 5));
        List<SpineRecogToDoTask> toDoTaskList = spineRecogTaskMapper.queryTodoTasks();
        if (CollUtil.isNotEmpty(toDoTaskList)) {
            logTaskTotal(TaskType.SPINE_RECOG, toDoTaskList);
            toDoTaskList.forEach(task -> spineService.lockedAsyncApply(task));
        }
    }

    private <T> void logTaskTotal(TaskType taskType, List<T> list) {
        int total = list instanceof Page ? (int) ((Page<T>) list).getTotal() : CollUtil.size(list);
        String time = DateUtil.format(DbClock.now(), DatePattern.NORM_DATETIME_MS_PATTERN);
        log.debug("{}, {}, total = {}, todo = {}", time, taskType.toString(), total, CollUtil.size(list));
    }

    /**
     * 创建一个定时任务，该任务在多节点下同时只在一个随机节点执行。任务的延迟时间和间隔时间不会严格按照设置
     * <br>
     * 只适用于间隔较短的定时任务，5s以上1h以下
     *
     * @param taskType        任务类型
     * @param runnable        执行代码
     * @param intervalSeconds 间隔时间
     */
    private FixedRateTask createTask(TaskType taskType, Runnable runnable, int intervalSeconds) {
        // 使不同节点的相同定时任务，执行时机相同
        int initialDelaySeconds = 30;
        long delay = initialDelaySeconds * 1000L;
        long interval = intervalSeconds * 1000L;
        long finalDelay = delay + interval - (DbClock.now().getTime() + delay) % interval;
        return new FixedRateTask(() -> {
            // 微调加锁时间，减少锁竞争和增加随机性
            ThreadUtil.safeSleep(RandomUtil.randomLong(1000L));
            TaskLockManager.lock(taskType, intervalSeconds, () -> {
                // 至少执行2000毫秒，以消除各节点之间的时间差
                TimeInterval timer = DateUtil.timer();
                runnable.run();
                long cost = timer.interval();
                if (cost < 3000L) {
                    ThreadUtil.safeSleep(3000L - cost);
                }
            });
        }, interval, finalDelay);
    }
}
