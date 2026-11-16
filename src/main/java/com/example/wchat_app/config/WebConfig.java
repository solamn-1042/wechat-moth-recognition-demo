package com.example.wchat_app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

//    @Value("${upload.avatar.dir:./uploads/avatars}")
//    private String uploadDir;

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        String absolutePath = new File(uploadDir).getAbsolutePath();
//
//        // 注册资源处理器，使上传的文件可通过URL访问
//        registry.addResourceHandler("/api/avatars/**")
//                .addResourceLocations("file:" + absolutePath + File.separator);
//    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用绝对路径映射，直接指向实际存储位置
        String projectPath = System.getProperty("user.dir");

        registry.addResourceHandler("/result/**")
                .addResourceLocations("file:" + projectPath + "/wchat_app/result/");

        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:" + projectPath + "/wchat_app/img/");

        // 添加日志输出帮助调试
        log.info("静态资源映射配置: /result/** -> {}", projectPath + "/wchat_app/result/");
        log.info("静态资源映射配置: /img/** -> {}", projectPath + "/wchat_app/img/");
    }
}