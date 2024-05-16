package com.paopao.logger.factory;

import com.paopao.logger.client.LogServiceClient;
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


    @Resource
    private MessagePublisher messagePublisher;


    public MessagingClientFactory(MessagingProperties properties) {
        this.properties = properties;
    }

    public MessagingTemplate createClient() {
        String lowerCase = properties.getType().toLowerCase();
        return switch (lowerCase) {
            case "kafka" -> throw new UnsupportedOperationException("暂不支持kafka");
            case "rabbitmq" -> new RabbitMQTemplate(SpringUtil.getBean(RabbitTemplate.class), properties.getTopic());
            case "redis" -> new RedisMQTemplate(messagePublisher, properties.getTopic());
            case "pulasr" ->
                    new PulsarMQTemplate(properties.getTopic(), SpringUtil.getBean("pulsarTemplate", org.springframework.pulsar.core.PulsarTemplate.class));
            default -> SpringUtil.getBean(LogServiceClient.class);
        };
    }
}
