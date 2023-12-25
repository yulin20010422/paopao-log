package com.paopao.logger.messaging;

import com.paopao.logger.core.redis.MessagePublisher;
import lombok.AllArgsConstructor;
import org.apache.pulsar.client.api.PulsarClientException;

/**
 * @author xiaobai
 * @since 2023/12/22 17:52
 */
@AllArgsConstructor
public class RedisMQTemplate implements MessagingTemplate {

    private MessagePublisher messagePublisher;
    private String topic;

    @Override
    public void send(byte[] message) {
        messagePublisher.publish(topic, new String(message));
    }
}
