package com.paopao.logger.selector;

import com.paopao.logger.annotation.EnableLogOperation;
import com.paopao.logger.aspect.LogOperateAspect;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author xiaobai
 * @since 2023/12/25 18:23
 */
public class LogOperationImportSelector implements ImportSelector {

    public static Boolean hasEnableLogOperation = false;

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableLogOperation.class.getName(), false));
        if (attributes != null) {
            hasEnableLogOperation = true;
            return new String[]{LogOperateAspect.class.getName()};
        }
        return new String[0];
    }
}
