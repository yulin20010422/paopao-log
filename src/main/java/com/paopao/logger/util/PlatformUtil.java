package com.paopao.logger.util;

/**
 * @author xiaobai
 * @since 2023/8/17 15:11
 */
public class PlatformUtil {


    public static String getAllInfo() {
        return "os:" + getOsName() + "; version:" + getOsVersion() + "; arch:" + getOsArch();
    }


    /**
     * 获取平台信息
     *
     * @return
     */
    private static String getOsName() {
        return System.getProperty("os.name");
    }

    /**
     * 获取平台版本
     *
     * @return
     */
    private static String getOsVersion() {
        return System.getProperty("os.version");
    }

    /**
     * 获取平台架构
     *
     * @return
     */
    private static String getOsArch() {
        return System.getProperty("os.arch");
    }
}
