package com.paopao.logger.annotation;

import com.paopao.logger.aspect.LogOperateAspect;
import com.paopao.logger.config.LogOperationConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author xiaobai
 * @since 2023/8/15 15:13
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({LogOperationConfig.class})
public @interface EnableLogOperation {
    boolean enable() default false;
}

