package com.paopao.logger.messaging;

import org.apache.pulsar.client.api.PulsarClientException;

/**
 * @author xiaobai
 * @since 2023/12/20 14:30
 */
public interface MessagingTemplate {

    void send(byte[] message) throws PulsarClientException;
}
