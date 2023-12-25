package com.paopao.logger.condition;

import com.paopao.logger.annotation.EnableLogOperation;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * @author xiaobai
 * @since 2023/12/20 11:23
 */
public class LogOperationCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 获取启动类上的注解
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableLogOperation.class.getName());
        if (attributes != null) {
            // 检查注解的enable属性值
            return (Boolean) attributes.get("enable");
        }
        return false;
    }
}
