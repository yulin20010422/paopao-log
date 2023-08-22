package com.paopao.logger.context;

import com.paopao.logger.pojo.LogOperation;

/**
 * @author xiaobai
 * @since 2023/8/17 16:20
 */
public class LogOperationContext {
    private static final ThreadLocal<LogOperation> LOG_OPERATION_GENERIC_RECORD = new ThreadLocal<>();

    public LogOperationContext() {
    }

    public static LogOperation getLogOperationGenericRecord() {
        return LOG_OPERATION_GENERIC_RECORD.get();
    }
    public static void setLogOperationGenericRecord(LogOperation logOperation) {
        LOG_OPERATION_GENERIC_RECORD.set(logOperation);
    }

    public static void clear() {
        LOG_OPERATION_GENERIC_RECORD.remove();
    }
}
