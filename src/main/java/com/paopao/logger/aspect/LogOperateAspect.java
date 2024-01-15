package com.paopao.logger.aspect;

import com.paopao.logger.annotation.LogOperate;
import com.paopao.logger.condition.LogOperationCondition;
import com.paopao.logger.context.LogOperationContext;
import com.paopao.logger.enums.ActionEnum;
import com.paopao.logger.enums.ResultEnum;
import com.paopao.logger.factory.MessagingClientFactory;
import com.paopao.logger.messaging.MessagingTemplate;
import com.paopao.logger.pojo.LogOperation;
import com.paopao.logger.util.IPUtil;
import com.paopao.logger.util.PlatformUtil;
import com.paopao.logger.util.SpringUtil;
import jakarta.annotation.Resource;
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
import org.springframework.context.annotation.Conditional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.paopao.logger.common.Const.AUTHORIZATION;
import static com.paopao.logger.enums.ActionEnum.isUnnecessaryAfter;
import static com.paopao.logger.enums.ActionEnum.isUnnecessaryBefore;

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
@Component
@Conditional(LogOperationCondition.class)
public class LogOperateAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogOperateAspect.class);

    private final JdbcTemplate jdbcTemplate = SpringUtil.getBean("jdbcTemplate", JdbcTemplate.class);

    /**
     * 查询结果分隔符
     */
    private final String SPLIT_QUERY_RESULT = "这是一个分割符号";

    @Resource
    private MessagingClientFactory clientFactory;

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
                //没有.的形式、#开头的
                if (!objectIdExpression.contains(".")) {
                    logOperation.setObjectId(String.valueOf(args[0]));
                }
            }
            //如果el表达式 是#xxx.id的形式，那么就从参数中获取
            if (objectIdExpression.startsWith("#") && objectIdExpression.contains(".")) {
                String[] split = objectIdExpression.split("\\.");
                String parameterName = split[0].substring(1);
                String fieldName = split[1];
                //获取.后面
                String parameterNameAfter = split[1];

                Object[] args = joinPoint.getArgs();
                for (Object arg : args) {
                    //单个
                    String whereField = annotation.where();
                    Object whereFiled = getWhereFiled(arg, whereField, parameterNameAfter);
                    if (Objects.nonNull(whereFiled)) {
                        if (whereFiled instanceof List) {
                            logOperation.setObjectIds((List<String>) whereFiled);
                        } else {
                            logOperation.setObjectId(String.valueOf(whereFiled));
                        }
                        logOperation.setObjectId(String.valueOf(whereFiled));
                    }
                }
            }
        }
        logOperation.setWhere(annotation.where());
        //一些细节操作
        logOperation.setAction(annotation.action().getMessage());
        logOperation.setDescription(annotation.description());
        logOperation.setObject(annotation.object());
        logOperation.setModule(annotation.module());
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
        if (StringUtils.hasLength(logOperation.getObjectId()) && !isUnnecessaryBefore(annotation.action()) ||
                !CollectionUtils.isEmpty(logOperation.getObjectIds()) && !isUnnecessaryBefore(annotation.action())) {
            //查询这个对象操作前的信息
            String whereField = annotation.where();
            if (CollectionUtils.isEmpty(logOperation.getObjectIds())) {
                String sql = "select * from " + logOperation.getTableName() + " where " + whereField + " = " + logOperation.getObjectId();
                logOperation.setUpdateBefore(getEffectString(sql));
            } else {
                String sql = "select * from " + logOperation.getTableName() + " where " + whereField + " in (" + String.join(",", logOperation.getObjectIds()) + ")";
                logOperation.setUpdateBefore(getEffectStrings(sql));
            }
        }
        LogOperationContext.setLogOperationGenericRecord(logOperation);
    }

    @AfterReturning(pointcut = "logOperation()", returning = "result")
    public void afterReturningLogOperation(JoinPoint joinPoint, Object result) throws PulsarClientException, JsonProcessingException {
        LogOperation logOperation = LogOperationContext.getLogOperationGenericRecord();
        LogOperate annotation = getLogOperateAnnotation(joinPoint);
        if (Objects.nonNull(annotation.objectId())) {
            if (logOperation.getObjectId() != null && !isUnnecessaryAfter(annotation.action()) ||
                    !CollectionUtils.isEmpty(logOperation.getObjectIds()) && !isUnnecessaryAfter(annotation.action())) {
                if (CollectionUtils.isEmpty(logOperation.getObjectIds())) {
                    String sql = "select * from " + logOperation.getTableName() + " where " + logOperation.getWhere() + " = " + logOperation.getObjectId();
                    logOperation.setUpdateAfter(getEffectString(sql));
                } else {
                    String sql = "select * from " + logOperation.getTableName() + " where " + logOperation.getWhere() + " in (" + String.join(",", logOperation.getObjectIds()) + ")";
                    logOperation.setUpdateAfter(getEffectStrings(sql));
                }
            }
        }
        if (annotation.action().equals(ActionEnum.ADD)) {
            //根据tableName获取最新的一条数据
            String sql = "select * from " + annotation.tableName() + " order by create_at desc limit 1";
            String effectString = getEffectString(sql);
            logOperation.setUpdateAfter(effectString);
            logOperation.setObjectId(getId(effectString).replace("\"", ""));
        }
        String applicationName = SpringUtil.getApplicationName();
        logOperation.setResult(ResultEnum.SUCCESS.getMessage());
        logOperation.setApplicationName(applicationName);
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = objectMapper.writeValueAsBytes(logOperation);
        //将logOperationGenericRecord以avro的形式发送到pulsar
        //自行替换消息队列类型 kafka/rocketmq/rabbitmq
        getMessagingTemplate().send(bytes);
        LogOperationContext.clear();
    }


    @AfterThrowing(pointcut = "logOperation()", throwing = "exception")
    public void afterThrowingLogOperation(JoinPoint joinPoint, Throwable exception) throws JsonProcessingException, PulsarClientException {
        LogOperation logOperation = LogOperationContext.getLogOperationGenericRecord();
        //获取spring.application.name
        String applicationName = SpringUtil.getApplicationName();
        logOperation.setResult(ResultEnum.FAIL.getMessage());
        logOperation.setException(exception.getMessage());
        logOperation.setApplicationName(applicationName);
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = objectMapper.writeValueAsBytes(logOperation);
        //自行替换消息队列类型 kafka/rocketmq/rabbitmq
        getMessagingTemplate().send(bytes);
        LogOperationContext.clear();
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

    private LogOperate getLogOperateAnnotation(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod().getAnnotation(LogOperate.class);
    }

    private MessagingTemplate getMessagingTemplate() {
        return clientFactory.createClient();
    }

    private String getId(String object) {
        JsonObject jsonObject = JsonParser.parseString(object).getAsJsonObject();
        return String.valueOf(jsonObject.get("id"));
    }

    /**
     * 反射获取某个object类型的id和name
     */
    private Object getWhereFiled(Object arg, String fieldName, String afterArg) {
        // 使用反射获取id和name属性值
        try {
            Class<?> clazz = arg.getClass();
            //获取arg下的afterArg属性
            Field afterArgField = clazz.getDeclaredField(afterArg);
            //判断afterArgField是否是一个集合
            if (afterArgField.getType().isAssignableFrom(List.class)) {
                //获取集合中的第一个元素
                List<?> list = (List<?>) afterArgField.get(arg);
                if (!list.isEmpty()) {
                    return list.stream().map(item -> {
                        try {
                            Field idField = item.getClass().getDeclaredField(fieldName);
                            idField.setAccessible(true);
                            return idField.get(item).toString();
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            logger.error("获取id和name属性值失败:{}", e.getMessage());
                        }
                        return null;
                    }).toList();
                }
            }
            // 获取id属性值
            Field idField = clazz.getDeclaredField(fieldName);
            idField.setAccessible(true);
            return idField.get(arg);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("获取id和name属性值失败:{}", e.getMessage());
        }
        return null;
    }

    private String getEffectString(String sql) {
        try {
            jdbcTemplate.query(sql, rs -> {
                return getObject(sql);
            });
        } catch (DataAccessException e) {
            logger.error("查询数据库失败:{}", e.getMessage());
        }
        return null;
    }

    private String getEffectStrings(String sql) {
        AtomicReference<String> effectString = new AtomicReference<>("");
        List<Map<String, Object>> maps;
        try {
            maps = jdbcTemplate.queryForList(sql);
            maps.forEach(item -> {
                JsonObject jsonObject = new JsonObject();
                item.forEach((key, value) -> jsonObject.addProperty(key, String.valueOf(value)));
                String s1 = effectString.get();
                if (StringUtils.hasLength(s1)) {
                    effectString.set(s1 + SPLIT_QUERY_RESULT + jsonObject.toString());
                } else {
                    effectString.set(jsonObject.toString());
                }
            });
        } catch (DataAccessException e) {
            logger.error("查询数据库失败:{}", e.getMessage());
        }
        return effectString.get();
    }
}