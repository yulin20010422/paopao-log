package com.paopao.logger.annotation;

import com.paopao.logger.aspect.LogOperateAspect;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author xiaobai
 * @since 2023/8/15 15:13
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({LogOperateAspect.class})
public @interface EnableLogOperation {
}
