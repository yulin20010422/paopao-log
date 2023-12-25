package com.paopao.logger.core.redis;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author xiaobai
 * @since 2023/12/22 17:50
 */
@Service
public class MessagePublisher {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }
}
