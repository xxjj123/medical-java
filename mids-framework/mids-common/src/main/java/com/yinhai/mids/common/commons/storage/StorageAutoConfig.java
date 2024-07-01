package com.yinhai.mids.common.commons.storage;

import com.yinhai.mids.common.commons.storage.mys3.MyS3FSBucketStoreFactory;
import com.yinhai.mids.common.commons.storage.mys3.MyS3FSObjectStoreFactory;
import com.yinhai.mids.common.commons.storage.mys3.MyS3StorageProperties;
import com.yinhai.ta404.module.storage.ta.all.bucket.AbstractFSBucketStoreFactory;
import com.yinhai.ta404.module.storage.ta.all.object.AbstractFSObjectStoreFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author zhuhs
 * @date 2024/6/28 9:27
 */
@Configuration
public class StorageAutoConfig {

    @Bean
    @Primary
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
}
