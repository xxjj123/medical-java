package com.yinhai.mids.common.module.async;

import com.yinhai.mids.common.util.ThreadKit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author zhuhs
 * @date 2022/08/04 14:08
 */
@Slf4j
@Component
public class ShutdownManager {

    @Resource(name = "asyncJobExecutorService")
    private ScheduledExecutorService asyncJobExecutorService;

    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    /**
     * 停止异步执行任务
     */
    private void shutdownAsyncManager() {
        try {
            log.info("====关闭后台任务任务线程池====");
            ThreadKit.shutdownAndAwaitTermination(asyncJobExecutorService);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
