package com.paopao.logger.core.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

/**
 * @author xiaobai
 * @since 2023/12/22 17:51
 */
@Service
public class RedisMessageSubscriber implements MessageListener {

    @Override
    public void onMessage(final Message message, final byte[] pattern) {
        System.out.println("Message received: " + message.toString());
    }
}