package com.paopao.logger.config;

import com.paopao.logger.aspect.LogOperateAspect;
import com.paopao.logger.condition.LogOperationCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaobai
 * @since 2023/12/20 11:13
 */
@Configuration
public class LogOperationConfig {

    @Bean
    @Conditional(LogOperationCondition.class)
    public LogOperateAspect logOperateAspect() {
        return new LogOperateAspect();
    }
}

