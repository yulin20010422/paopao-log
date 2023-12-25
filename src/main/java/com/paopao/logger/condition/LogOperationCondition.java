package com.paopao.logger.condition;

import com.paopao.logger.annotation.EnableLogOperation;
import com.paopao.logger.selector.LogOperationImportSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author xiaobai
 * @since 2023/12/20 11:23
 */
@Slf4j
public class LogOperationCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        log.info("LogOperationCondition.matches:{}",LogOperationImportSelector.hasEnableLogOperation);
        // 获取启动类上的注解 ,是否有EnableLogOperation注解
        return LogOperationImportSelector.hasEnableLogOperation;
    }
}
