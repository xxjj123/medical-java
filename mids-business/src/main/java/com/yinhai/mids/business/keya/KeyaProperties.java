package com.yinhai.mids.business.keya;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhuhs
 * @date 2024/7/15 14:46
 */
@Data
@ConfigurationProperties(prefix = "keya")
@Component
public class KeyaProperties {

    private String registerUrl;

    private String resultUrl;

    private String pushCallbackUrl;

}
