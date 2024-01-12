package com.paopao.logger.pojo;


import lombok.Getter;
import lombok.Setter;

/**
 * @author xiaobai
 * @since 2023/8/11 15:26
 */
@Getter
@Setter
public class LogOperation{

    private String operateUserId;
    private String tableName;
    private String object;
    private String objectId;
    private String module;
    private String timestamp;
    private String action;
    private String updateBefore;
    private String updateAfter;
    private String result;
    private String platform;
    private String ip;
    private String description;
    private String where;

    private String exception;
    private String applicationName;
}
