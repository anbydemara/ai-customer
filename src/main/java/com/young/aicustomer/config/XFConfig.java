package com.young.aicustomer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "xunfei")
public class XFConfig {
    private String url;
    private String appId;
    private String apiSecret;
    public String apiKey;
    Integer maxResponseTime;
}
