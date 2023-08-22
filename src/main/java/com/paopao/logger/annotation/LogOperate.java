package com.paopao.logger.annotation;


import com.paopao.logger.enums.ActionEnum;
import com.paopao.logger.enums.ResultEnum;

import java.lang.annotation.*;

/**
 * @author xiaobai
 * @since 2023/8/15 15:22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LogOperate {

    String module() default "";

    String tableName() default "";

    String platform() default "";

    String timestamp() default "";

    String operateUserId() default "";

    String ip() default "";

    ActionEnum action();

    String object() default "";

    String objectId() default "";

    String updateBefore() default "";

    String updateAfter() default "";

    ResultEnum result() default ResultEnum.SUCCESS;

    String description() default "";

    String exception() default "";

}
