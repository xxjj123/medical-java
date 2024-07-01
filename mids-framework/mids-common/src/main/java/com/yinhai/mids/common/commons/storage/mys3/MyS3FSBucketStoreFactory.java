package com.yinhai.mids.common.commons.storage.mys3;

import com.yinhai.ta404.module.storage.ta.all.bucket.impl.S3FSBucketStoreFactory;
import com.yinhai.ta404.module.storage.ta.all.properties.S3StorageProperties;

/**
 * @author zhuhs
 * @date 2024/6/28 10:49
 */
public class MyS3FSBucketStoreFactory extends S3FSBucketStoreFactory {

    public MyS3FSBucketStoreFactory(S3StorageProperties properties) {
        super(properties);
    }

    @Override
    public String getFsType() {
        return "mys3";
    }
}
