package com.yinhai.mids.common.module.async;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.yinhai.mids.common.util.ThreadKit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

/**
 * @author zhuhs
 * @date 2022/08/04 13:54
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 核心线程数 = cpu 核心数 + 1
     */
    private final int corePoolSize = RuntimeUtil.getProcessorCount() + 1;

    @Bean(name = "asyncJobExecutorService")
    public ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(
                corePoolSize,
                ThreadUtil.newNamedThreadFactory("async-pool-", true),
                new CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                ThreadKit.printException(r, t);
            }
        };
    }
}
