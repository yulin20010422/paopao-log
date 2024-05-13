package com.paopao.logger.factory;

import com.paopao.logger.config.MessagingProperties;
import com.paopao.logger.core.redis.MessagePublisher;
import com.paopao.logger.messaging.MessagingTemplate;
import com.paopao.logger.messaging.PulsarMQTemplate;
import com.paopao.logger.messaging.RabbitMQTemplate;
import com.paopao.logger.messaging.RedisMQTemplate;
import com.paopao.logger.util.SpringUtil;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Component;

/**
 * @author xiaobai
 * @since 2023/12/20 14:28
 */
@Component
public class MessagingClientFactory {

    private final MessagingProperties properties;

    private final PulsarTemplate pulsarTemplate = SpringUtil.getBean("pulsarTemplate", org.springframework.pulsar.core.PulsarTemplate.class);

    private final RabbitTemplate rabbitTemplate = SpringUtil.getBean(RabbitTemplate.class);

    @Resource
    private MessagePublisher messagePublisher;


    public MessagingClientFactory(MessagingProperties properties) {
        this.properties = properties;
    }

    public MessagingTemplate createClient() {
        return switch (properties.getType().toLowerCase()) {
            case "kafka" -> throw new UnsupportedOperationException("暂不支持kafka");
            case "rabbitmq" -> new RabbitMQTemplate(rabbitTemplate, properties.getTopic());
            case "redis" -> new RedisMQTemplate(messagePublisher, properties.getTopic());
            default -> new PulsarMQTemplate(properties.getTopic(), pulsarTemplate);
        };
    }
}
