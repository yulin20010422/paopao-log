package com.paopao.logger.messaging;

import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.pulsar.core.PulsarTemplate;

/**
 * @author xiaobai
 * @since 2023/12/20 14:30
 */
public class PulsarMQTemplate implements MessagingTemplate {

    private PulsarTemplate pulsarTemplate;
    private String topic;

    public PulsarMQTemplate(String topic, PulsarTemplate pulsarTemplate) {
        this.pulsarTemplate = pulsarTemplate;
        this.topic = topic;
    }

    @Override
    public void send(byte[] message) throws PulsarClientException {
        pulsarTemplate.send(topic, message);
    }
}
