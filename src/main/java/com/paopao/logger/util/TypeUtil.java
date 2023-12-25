package com.paopao.logger.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.pulsar.shade.org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.Map;

/**
 * @author xiaobai
 * @since 2023/12/22 15:40
 */
public class TypeUtil {
    public TypeUtil() {
    }

    public static Class<?> getClass(Type type) {
        if (null != type) {
            if (type instanceof Class) {
                return (Class)type;
            }

            if (type instanceof ParameterizedType) {
                return (Class)((ParameterizedType)type).getRawType();
            }

            if (type instanceof TypeVariable) {
                return (Class)((TypeVariable)type).getBounds()[0];
            }

            if (type instanceof WildcardType) {
                Type[] upperBounds = ((WildcardType)type).getUpperBounds();
                if (upperBounds.length == 1) {
                    return getClass(upperBounds[0]);
                }
            }
        }

        return null;
    }
    public static Type getParamType(Method method, int index) {
        Type[] types = getParamTypes(method);
        return null != types && types.length > index ? types[index] : null;
    }

    public static Class<?> getParamClass(Method method, int index) {
        Class<?>[] classes = getParamClasses(method);
        return null != classes && classes.length > index ? classes[index] : null;
    }

    public static Type[] getParamTypes(Method method) {
        return null == method ? null : method.getGenericParameterTypes();
    }

    public static Class<?>[] getParamClasses(Method method) {
        return null == method ? null : method.getParameterTypes();
    }

    public static Type getReturnType(Method method) {
        return null == method ? null : method.getGenericReturnType();
    }

    public static Class<?> getReturnClass(Method method) {
        return null == method ? null : method.getReturnType();
    }

    public static Type getTypeArgument(Type type) {
        return getTypeArgument(type, 0);
    }

    public static Type getTypeArgument(Type type, int index) {
        Type[] typeArguments = getTypeArguments(type);
        return null != typeArguments && typeArguments.length > index ? typeArguments[index] : null;
    }

    public static Type[] getTypeArguments(Type type) {
        if (null == type) {
            return null;
        } else {
            ParameterizedType parameterizedType = toParameterizedType(type);
            return null == parameterizedType ? null : parameterizedType.getActualTypeArguments();
        }
    }

    public static ParameterizedType toParameterizedType(Type type) {
        ParameterizedType result = null;
        if (type instanceof ParameterizedType) {
            result = (ParameterizedType)type;
        } else if (type instanceof Class) {
            Class<?> clazz = (Class)type;
            Type genericSuper = clazz.getGenericSuperclass();
            if (null == genericSuper || Object.class.equals(genericSuper)) {
                Type[] genericInterfaces = clazz.getGenericInterfaces();
                if (ArrayUtils.isNotEmpty(genericInterfaces)) {
                    genericSuper = genericInterfaces[0];
                }
            }

            result = toParameterizedType(genericSuper);
        }

        return result;
    }
}

