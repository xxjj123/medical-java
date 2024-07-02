package com.yinhai.mids.common.module.storage.mys3;

import com.yinhai.ta404.module.storage.ta.all.object.AbstractFSObjectStore;
import com.yinhai.ta404.module.storage.ta.all.object.impl.S3FSObjectStoreFactory;
import com.yinhai.ta404.module.storage.ta.all.properties.FSBusinessTypeAndBucket;
import com.yinhai.ta404.module.storage.ta.all.properties.S3StorageProperties;

/**
 * @author zhuhs
 * @date 2024/6/28 9:29
 */
public class MyS3FSObjectStoreFactory extends S3FSObjectStoreFactory {

    public MyS3FSObjectStoreFactory(S3StorageProperties properties) {
        super(properties);
    }

    @Override
    public String getFsType() {
        return "mys3";
    }

    @Override
    public AbstractFSObjectStore create(FSBusinessTypeAndBucket fsBusinessTypeAndBucket) {
        return new MyS3FSObjectStore(getProperties(), fsBusinessTypeAndBucket);
    }
}
