package com.paopao.logger.enums;

/**
 * @author xiaobai
 * @since 2023/8/22 11:27
 */
public enum ResultEnum {
    SUCCESS("成功"),
    FAIL("失败"),
    UNKNOWN("未知");

    public String getMessage() {
        return message;
    }

    private String message;

    ResultEnum(String message) {
        this.message = message;
    }
}
