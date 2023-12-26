package com.paopao.logger.enums;


import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * @author xiaobai
 * @since 2023/8/22 11:32
 */
public enum ActionEnum {
    /**
     * 新增
     */
    ADD("新增"),
    /**
     * 删除
     */
    DELETE("删除"),
    /**
     * 修改
     */
    UPDATE("修改"),
    /**
     * 查询
     */
    QUERY("查询"),
    /**
     * 登录
     */
    LOGIN("登录"),
    /**
     * 登出
     */
    LOGOUT("登出"),
    /**
     * 上传
     */
    UPLOAD("上传"),
    /**
     * 下载
     */
    DOWNLOAD("下载"),
    /**
     * 导入
     */
    IMPORT("导入"),
    /**
     * 导出
     */
    EXPORT("导出"),
    /**
     * 其他
     */
    OTHER("其他");

    public static List<ActionEnum> unnecessaryBefore() {
        List<ActionEnum> actionEnums= Lists.newArrayList();
        actionEnums.add(ActionEnum.DELETE);
        return actionEnums;
    }

    public static List<ActionEnum> unnecessaryAfter() {
        List<ActionEnum> actionEnums= Lists.newArrayList();
        actionEnums.add(ActionEnum.DELETE);
        return actionEnums;
    }

    public static boolean isUnnecessaryAfter(ActionEnum actionEnum) {
        return unnecessaryAfter().contains(actionEnum);
    }

    public static boolean isUnnecessaryBefore(ActionEnum actionEnum) {
        return unnecessaryBefore().contains(actionEnum);
    }

    private final String message;

    public String getMessage() {
        return message;
    }

    ActionEnum(String message) {
        this.message = message;
    }
}
