package com.paopao.logger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaobai
 * @since 2023/12/20 14:25
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "log.messaging")
public class MessagingProperties {
    private String type = "pulsar"; // 默认值为 "pulsar"
    private String topic = "persistent://public/default/log-operation-topic"; // 默认topic
}
