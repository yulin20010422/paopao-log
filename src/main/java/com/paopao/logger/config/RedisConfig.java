package com.paopao.logger.config;

import com.paopao.logger.core.redis.RedisMessageSubscriber;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.Objects;

/**
 * @author xiaobai
 * @since 2023/12/22 17:51
 */
@Configuration
public class RedisConfig {

    @Resource
    private MessagingProperties properties;

    @Bean
    StringRedisTemplate template(LettuceConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }

    @Bean
    RedisMessageListenerContainer container(MessageListenerAdapter listenerAdapter,LettuceConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(listenerAdapter, topic());
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic(properties.getTopic());
    }
}

