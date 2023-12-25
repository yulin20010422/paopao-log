package com.paopao.logger.factory;

import com.paopao.logger.config.MessagingProperties;
import com.paopao.logger.core.redis.MessagePublisher;
import com.paopao.logger.messaging.MessagingTemplate;
import com.paopao.logger.messaging.PulsarMQTemplate;
import com.paopao.logger.messaging.RedisMQTemplate;
import com.paopao.logger.util.SpringUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author xiaobai
 * @since 2023/12/20 14:28
 */
@Component
public class MessagingClientFactory {

    private final MessagingProperties properties;

    private final org.springframework.pulsar.core.PulsarTemplate pulsarTemplate = SpringUtil.getBean("pulsarTemplate", org.springframework.pulsar.core.PulsarTemplate.class);

    @Resource
    private MessagePublisher messagePublisher;


    public MessagingClientFactory(MessagingProperties properties) {
        this.properties = properties;
    }

    public MessagingTemplate createClient() {
        switch (properties.getType().toLowerCase()) {
            case "kafka":
                throw new UnsupportedOperationException("暂不支持kafka");
            case "mq":
                throw new UnsupportedOperationException("暂不支持mq");
            case "redis":
                return new RedisMQTemplate(messagePublisher,properties.getTopic());
            default:
                return new PulsarMQTemplate(properties.getTopic(), pulsarTemplate);
        }
    }
}
