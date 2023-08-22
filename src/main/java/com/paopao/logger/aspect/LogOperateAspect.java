package com.paopao.logger.aspect;

import com.paopao.logger.annotation.LogOperate;
import com.paopao.logger.context.LogOperationContext;
import com.paopao.logger.pojo.LogOperation;
import com.paopao.logger.util.HttpUtil;
import com.paopao.logger.util.IPUtil;
import com.paopao.logger.util.PlatformUtil;
import com.paopao.logger.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.shade.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pulsar.shade.com.google.gson.JsonObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.pulsar.core.PulsarTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 这种方式应对简单的业务场景，比较好用，但是如果用户的操作涉及一些隐式的变化，是没有办法记录的，即便是用监听器，也是比较麻烦的。
 * 不妨就直接使用LogUtils，自己记录日志，这样可以更加灵活的记录日志。也可以灵活的记录一些隐式的变化。只是这种方式代码入侵性比较大，但是能够更加准确，更加全面。
 * <p>
 *
 * @author xiaobai
 * @version 1.0
 * </p>
 * @since 2023/8/18 15:14
 */
@Aspect
@Configuration
public class LogOperateAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogOperateAspect.class);

    private final JdbcTemplate jdbcTemplate = SpringUtil.getBean("jdbcTemplate", JdbcTemplate.class);

    private final PulsarTemplate pulsarTemplate = SpringUtil.getBean("pulsarTemplate", PulsarTemplate.class);


    @Pointcut("@annotation(com.paopao.logger.annotation.LogOperate)")
    public void logOperation() {
    }

    @Before("logOperation()")
    public void beforeLogOperation(JoinPoint joinPoint) {
        logger.info("beforeLogOperation");
        LogOperation logOperation = new LogOperation();
        LogOperate annotation = getLogOperateAnnotation(joinPoint);
        if (Objects.nonNull(annotation.objectId())) {
            String objectIdExpression = annotation.objectId();
            if (objectIdExpression.startsWith("#")) {
                String parameterName = objectIdExpression.substring(1);
                Object[] args = joinPoint.getArgs();
                for (Object arg : args) {
                    if (arg instanceof Long id && "id".equals(parameterName)) {
                        logOperation.setObjectId(String.valueOf(id));
                    }
                }
            }
        }
        //一些细节操作
        logOperation.setAction(annotation.action().getMessage());
        logOperation.setDescription(annotation.description());
        logOperation.setObject(annotation.object());
        logOperation.setModule(annotation.module());
        logOperation.setPlatform(annotation.platform());
        logOperation.setOperateUserId(annotation.operateUserId());
        logOperation.setTableName(annotation.tableName());
        logOperation.setTimestamp(LocalDateTime.now().toString());
        logOperation.setPlatform(PlatformUtil.getAllInfo());
        logOperation.setIp(IPUtil.getRealIp(HttpUtil.getHttpServletRequest()));
        if (Objects.nonNull(annotation.operateUserId())) {
            logOperation.setOperateUserId("游客");
        }
        if (StringUtils.length(logOperation.getObjectId()) > 0) {
            //查询这个对象操作前的信息
            String sql = "select * from " + logOperation.getTableName() + " where id = " + logOperation.getObjectId();
            jdbcTemplate.query(sql, rs -> {
                logOperation.setUpdateBefore(getObject(sql));
            });
        }
        LogOperationContext.setLogOperationGenericRecord(logOperation);
    }

    @AfterReturning(pointcut = "logOperation()", returning = "result")
    public void afterReturningLogOperation(JoinPoint joinPoint, Object result) throws PulsarClientException, JsonProcessingException {
        // Process after method execution...
        LogOperation logOperationGenericRecord = LogOperationContext.getLogOperationGenericRecord();
        LogOperate annotation = getLogOperateAnnotation(joinPoint);
        if (Objects.nonNull(annotation.objectId())) {
            String objectIdExpression = annotation.objectId();
            if (objectIdExpression.startsWith("#")) {
                String parameterName = objectIdExpression.substring(1);
                Object[] args = joinPoint.getArgs();
                for (Object arg : args) {
                    if (arg instanceof Long id && "id".equals(parameterName)) {
                        // Special handling for the 'id' parameter
                        // Do something with the id...
                        String sql = "select * from " + annotation.tableName() + " where id = " + id;
                        jdbcTemplate.query(sql, rs -> {
                            logOperationGenericRecord.setUpdateAfter(getObject(sql));
                        });
                    }
                }
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = objectMapper.writeValueAsBytes(logOperationGenericRecord);
        //将logOperationGenericRecord以avro的形式发送到pulsar
        pulsarTemplate.sendAsync("persistent://public/default/log-operation-topic", bytes);
    }

    private String getObject(String sql) {
        JsonObject jsonObject = new JsonObject();
        jdbcTemplate.query(sql, rs -> {
            int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                jsonObject.addProperty(rs.getMetaData().getColumnName(i), String.valueOf(rs.getObject(i)));
            }
        });
        return jsonObject.toString();
    }


    @AfterThrowing(pointcut = "logOperation()", throwing = "exception")
    public void afterThrowingLogOperation(JoinPoint joinPoint, Throwable exception) {
        // Process after throwing exception...
    }


    // Helper methods to extract annotations...

    private LogOperate getLogOperateAnnotation(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod().getAnnotation(LogOperate.class);
    }
}