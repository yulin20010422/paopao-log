package com.paopao.logger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaobai
 * @since 2023/12/20 14:25
 */
@Configuration
@ConfigurationProperties(prefix = "log.messaging")
public class MessagingProperties {
    private String type = "pulsar"; // 默认值为 "pulsar"
    private String topic = "persistent://public/default/log-operation-topic"; // 默认topic

    // getters 和 setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
