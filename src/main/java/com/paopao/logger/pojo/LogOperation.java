package com.paopao.logger.pojo;


/**
 * @author xiaobai
 * @since 2023/8/11 15:26
 */
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

    public String getOperateUserId() {
        return operateUserId;
    }

    public void setOperateUserId(String operateUserId) {
        this.operateUserId = operateUserId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUpdateBefore() {
        return updateBefore;
    }

    public void setUpdateBefore(String updateBefore) {
        this.updateBefore = updateBefore;
    }

    public String getUpdateAfter() {
        return updateAfter;
    }

    public void setUpdateAfter(String updateAfter) {
        this.updateAfter = updateAfter;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
