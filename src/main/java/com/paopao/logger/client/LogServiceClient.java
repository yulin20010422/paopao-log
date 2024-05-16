package com.paopao.logger.client;

import com.paopao.logger.messaging.MessagingTemplate;
import com.paopao.logger.util.SpringUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author white
 * @since 2024/5/16 12:25
 */
public class LogServiceClient implements MessagingTemplate {
    private static final Logger logger = LoggerFactory.getLogger(LogServiceClient.class);
    private final RestTemplate restTemplate;

    @Value("${log.service.base-url:http://localhost:8080}")
    private String logServerUrl;

    @PostConstruct
    public void init() {
        if (!isServiceAvailable()) {
            logger.warn("Warning: Log service is not available at {}", logServerUrl, new IllegalStateException("Log service unavailable"));
        } else {
            logger.info("Log service is available at {}", logServerUrl);
        }
    }


    public LogServiceClient() {
        this.restTemplate = SpringUtil.getBean(RestTemplate.class);
    }


    public LogServiceClient(RestTemplate restTemplate, String logServerUrl) {
        this.restTemplate = restTemplate;
        this.logServerUrl = logServerUrl;
    }

    // 设置基础URL的方法，允许在使用时动态设置日志服务的基础URL
    public void setBaseUrl(String baseUrl) {
        this.logServerUrl = baseUrl;
    }

    private boolean isServiceAvailable() {
        String healthCheckUrl = this.logServerUrl + "/actuator/health"; // 假设日志服务有健康检查端点
        try {
            restTemplate.getForEntity(healthCheckUrl, Void.class);
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }

    @Override
    public void send(byte[] message) {

    }
}
