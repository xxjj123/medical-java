package com.yinhai.mids.business.spine;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "spine")
@Component
public class SpineProperties {

    private String queryUrl;
}
