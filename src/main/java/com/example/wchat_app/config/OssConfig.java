package com.example.wchat_app.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * 阿里云OSS配置类
 */
@Configuration
@Slf4j
public class OssConfig {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    private OSS ossClient;

    @Bean
    public OSS ossClient() {
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        return ossClient;
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            try {
                log.info("正在关闭OSS客户端...");
                ossClient.shutdown();
                log.info("OSS客户端已关闭");
            } catch (Exception e) {
                log.error("关闭OSS客户端时发生错误", e);
            }
        }
    }
}