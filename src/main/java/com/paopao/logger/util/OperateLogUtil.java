package com.paopao.logger.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiaobai
 * @since 2023/8/13 23:49
 */
public class OperateLogUtil {
    private static final Logger logger = LoggerFactory.getLogger(OperateLogUtil.class);


    public static void log(String userId, String module, String action, String updateBefore, String updateAfter, String result, String description, Exception exception) {
        logger.info("userId: {}, " +
                        "module: {}, " +
                        "action: {}, " +
                        "updateBefore: {}, " +
                        "updateAfter: {}, " +
                        "result: {}, " +
                        "platform: {}, " +
                        "ip: {}," +
                        "description: {}, " +
                        "exception: {}",
                userId,
                module,
                action,
                updateBefore,
                updateAfter,
                result,
                PlatformUtil.getAllInfo(),
                IPUtil.getRealIp(HttpUtil.getHttpServletRequest()),
                description,
                exception.getMessage());
    }

}
