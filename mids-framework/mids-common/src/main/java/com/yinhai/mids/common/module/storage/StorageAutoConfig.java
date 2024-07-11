package com.yinhai.mids.common.module.storage;

import cn.hutool.core.util.RuntimeUtil;
import com.yinhai.mids.common.module.storage.mys3.MyS3FSBucketStoreFactory;
import com.yinhai.mids.common.module.storage.mys3.MyS3FSObjectStoreFactory;
import com.yinhai.mids.common.module.storage.mys3.MyS3StorageProperties;
import com.yinhai.ta404.module.storage.ta.all.bucket.AbstractFSBucketStoreFactory;
import com.yinhai.ta404.module.storage.ta.all.object.AbstractFSObjectStoreFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author zhuhs
 * @date 2024/6/28 9:27
 */
@Configuration
public class StorageAutoConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "ta404.modules.storage.mys3",
            name = {"accessKeyId", "access-key-id"}
    )
    public AbstractFSObjectStoreFactory s3FSObjectStoreFactory(MyS3StorageProperties s3StorageProperties) {
        return new MyS3FSObjectStoreFactory(s3StorageProperties);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "ta404.modules.storage.mys3",
            name = {"accessKeyId", "access-key-id"},
            matchIfMissing = false
    )
    public AbstractFSBucketStoreFactory s3FSBucketStoreFactory(MyS3StorageProperties s3StorageProperties) {
        return new MyS3FSBucketStoreFactory(s3StorageProperties);
    }

    @Bean("uploadThreadPool")
    public ThreadPoolTaskExecutor uploadThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心池大小
        executor.setCorePoolSize(RuntimeUtil.getProcessorCount());
        // 最大线程数
        executor.setMaxPoolSize(RuntimeUtil.getProcessorCount() * 2);
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        // 队列长度
        executor.setQueueCapacity(10000);
        // 线程名称前缀
        executor.setThreadNamePrefix("upload-thread");
        // 拒绝策略（这里选择由调用线程执行）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
