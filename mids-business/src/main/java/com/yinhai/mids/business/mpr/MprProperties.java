package com.yinhai.mids.business.mpr;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhuhs
 * @date 2024/7/15 14:46
 */
@Data
@ConfigurationProperties(prefix = "mpr")
@Component
public class MprProperties {

    private String registerUrl;

    private String pushCallbackUrl;

}
