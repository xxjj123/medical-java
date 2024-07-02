package com.yinhai.mids.common.module.storage.mys3;

import com.yinhai.ta404.module.storage.ta.all.properties.S3StorageProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhuhs
 * @date 2024/6/28 10:52
 */
@Component
@ConfigurationProperties(
        prefix = "ta404.modules.storage.mys3"
)
public class MyS3StorageProperties extends S3StorageProperties {
}
