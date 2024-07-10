package com.yinhai.mids.common.module.async;

import cn.hutool.core.util.ArrayUtil;
import com.yinhai.ta404.core.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author zhuhs
 * @date 2022/08/04 14:09
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig extends AsyncConfigurerSupport {

    @Resource(name = "asyncJobExecutorService")
    private ScheduledExecutorService asyncJobExecutorService;

    @Override
    public Executor getAsyncExecutor() {
        return asyncJobExecutorService;
    }

    /**
     * 异步执行异常处理
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            log.error(throwable.getMessage(), throwable);
            StringBuilder sb = new StringBuilder();
            sb.append("Exception message - ")
                    .append(throwable.getMessage())
                    .append(", Method name - ")
                    .append(method.getName());
            if (ArrayUtil.isNotEmpty(objects)) {
                sb.append(", Parameter value - ").append(Arrays.toString(objects));
            }
            log.error(sb.toString());
            throw new AppException(sb.toString());
        };
    }
}
