package com.paopao.logger.aspect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.paopao.logger.annotation.LogOperate;
import com.paopao.logger.context.LogOperationContext;
import com.paopao.logger.enums.ActionEnum;
import com.paopao.logger.enums.ResultEnum;
import com.paopao.logger.pojo.LogOperation;
import com.paopao.logger.util.IPUtil;
import com.paopao.logger.util.PlatformUtil;
import com.paopao.logger.util.SpringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.shade.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pulsar.shade.com.google.gson.JsonObject;
import org.apache.pulsar.shade.com.google.gson.JsonParser;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.paopao.logger.common.Const.AUTHORIZATION;
import static com.paopao.logger.common.Const.COLON;

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

    Cache<String, String> objectMapSnapshotsCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(3, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    @Pointcut("@annotation(com.paopao.logger.annotation.LogOperate)")
    public void logOperation() {
    }

    @Before("logOperation()")
    public void beforeLogOperation(JoinPoint joinPoint) {
        logger.info("beforeLogOperation");
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        LogOperation logOperation = new LogOperation();
        LogOperate annotation = getLogOperateAnnotation(joinPoint);
        if (Objects.nonNull(annotation.objectId())) {
            String objectIdExpression = annotation.objectId();
            //如果objectId是一个表达式，那么就从参数中获取,这里只支持一个参数#id,
            if (objectIdExpression.startsWith("#")) {
                String parameterName = objectIdExpression.substring(1);
                Object[] args = joinPoint.getArgs();
                for (Object arg : args) {
                    if (arg instanceof Long id && "id".equals(parameterName)) {
                        logOperation.setObjectId(String.valueOf(id));
                    }
                }
            }
            //如果el表达式 是#xxx.id的形式，那么就从参数中获取
            if (objectIdExpression.startsWith("#") && objectIdExpression.contains(".")) {
                String[] split = objectIdExpression.split("\\.");
                String parameterName = split[0].substring(1);
                String fieldName = split[1];
                Object[] args = joinPoint.getArgs();
                for (Object arg : args) {
                    if (arg instanceof Long id && "id".equals(parameterName)) {
                        logOperation.setObjectId(String.valueOf(id));
                    }
                    if (arg instanceof JsonObject jsonObject && "id".equals(parameterName)) {
                        logOperation.setObjectId(jsonObject.get(fieldName).getAsString());
                    }
                }
            }
        }
        //一些细节操作
        logOperation.setAction(annotation.action().getMessage());
        logOperation.setDescription(annotation.description());
        logOperation.setObject(annotation.object());
        logOperation.setObjectId(annotation.objectId());
        logOperation.setModule(annotation.module());
        logOperation.setPlatform(annotation.platform());
        logOperation.setTableName(annotation.tableName());
        logOperation.setTimestamp(LocalDateTime.now().toString());
        logOperation.setPlatform(PlatformUtil.getAllInfo());
        logOperation.setIp(IPUtil.getRealIp(request));
        //如果用户id不为空，那么就从参数中获取
        String authingId = request.getHeader(AUTHORIZATION);
        if (Objects.nonNull(authingId)) {
            logOperation.setOperateUserId(authingId);
        } else {
            logOperation.setOperateUserId("游客");
        }
        if (org.springframework.util.StringUtils.hasLength(logOperation.getObjectId())) {
            String snapshotsCacheIfPresent = objectMapSnapshotsCache.getIfPresent(logOperation.getTableName() + COLON + logOperation.getObjectId());
            if (StringUtils.hasLength(snapshotsCacheIfPresent)) {
                //如果缓存中有，就直接从缓存中获取
                logOperation.setUpdateBefore(snapshotsCacheIfPresent);
            } else {
                //查询这个对象操作前的信息
                String sql = "select * from " + logOperation.getTableName() + " where id = " + logOperation.getObjectId();
                jdbcTemplate.query(sql, rs -> {
                    String object = getObject(sql);
                    logOperation.setUpdateBefore(object);
                });
            }

        }
        LogOperationContext.setLogOperationGenericRecord(logOperation);
    }

    @AfterReturning(pointcut = "logOperation()", returning = "result")
    public void afterReturningLogOperation(JoinPoint joinPoint, Object result) throws PulsarClientException, JsonProcessingException {
        // Process after method execution...
        LogOperation logOperation = LogOperationContext.getLogOperationGenericRecord();
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
                        //存取这个对象的快照,便于下次查询可不用查询数据库
                        jdbcTemplate.query(sql, rs -> {
                            String object = getObject(sql);
                            logOperation.setUpdateAfter(object);
                            objectMapSnapshotsCache.put(annotation.tableName() + COLON + id, object);
                        });
                    }
                }
            }
        }
        if (annotation.action().equals(ActionEnum.ADD)) {
            //根据tableName获取最新的一条数据
            String sql = "select * from " + annotation.tableName() + " order by create_time desc limit 1";
            jdbcTemplate.query(sql, rs -> {
                String object = getObject(sql);
                logOperation.setUpdateAfter(object);
                logOperation.setObjectId(getId(object).replace("\"", ""));
            });
        }
        String applicationName = SpringUtil.getApplicationName();
        logOperation.setResult(ResultEnum.SUCCESS.getMessage());
        logOperation.setApplicationName(applicationName);
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = objectMapper.writeValueAsBytes(logOperation);
        //将logOperationGenericRecord以avro的形式发送到pulsar
        //自行替换消息队列类型 kafka/rocketmq/rabbitmq
        pulsarTemplate.sendAsync("persistent://public/default/log-operation-topic", bytes);
        LogOperationContext.clear();
    }

    private String getId(String object) {
        JsonObject jsonObject = JsonParser.parseString(object).getAsJsonObject();
        return String.valueOf(jsonObject.get("id"));
    }

    @AfterThrowing(pointcut = "logOperation()", throwing = "exception")
    public void afterThrowingLogOperation(JoinPoint joinPoint, Throwable exception) throws JsonProcessingException, PulsarClientException {
        // Process after throwing exception...
        LogOperation logOperation = LogOperationContext.getLogOperationGenericRecord();
        //获取spring.application.name
        String applicationName = SpringUtil.getApplicationName();
        logOperation.setResult(ResultEnum.FAIL.getMessage());
        logOperation.setException(exception.getMessage());
        logOperation.setApplicationName(applicationName);
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = objectMapper.writeValueAsBytes(logOperation);
        //自行替换消息队列类型 kafka/rocketmq/rabbitmq
        pulsarTemplate.sendAsync("persistent://public/default/log-operation-topic", bytes);
        LogOperationContext.clear();
    }

    private String getObject(String sql) {
//        Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(sql);
        JsonObject jsonObject = new JsonObject();
        jdbcTemplate.query(sql, rs -> {
            int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                jsonObject.addProperty(rs.getMetaData().getColumnName(i), String.valueOf(rs.getObject(i)));
            }
        });
        return jsonObject.toString();
    }

    // Helper methods to extract annotations...

    private LogOperate getLogOperateAnnotation(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod().getAnnotation(LogOperate.class);
    }
}