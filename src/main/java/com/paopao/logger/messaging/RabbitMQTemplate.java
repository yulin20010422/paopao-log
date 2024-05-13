package com.paopao.logger.messaging;

import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @author white
 * @since 2024/5/13 11:40
 */
public class RabbitMQTemplate implements MessagingTemplate {

    private final RabbitTemplate rabbitTemplate;
    //mq中，topic等同于exchange
    private final String topic;

    public RabbitMQTemplate(RabbitTemplate rabbitTemplate, String topic) {
        this.rabbitTemplate = rabbitTemplate;
        this.topic = topic;
    }

    @Override
    public void send(byte[] message) {
        this.rabbitTemplate.convertAndSend(topic, message);
    }
}
