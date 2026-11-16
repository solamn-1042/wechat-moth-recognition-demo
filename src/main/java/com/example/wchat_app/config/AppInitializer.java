package com.example.wchat_app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class AppInitializer implements CommandLineRunner {

    @Value("${upload.avatar.dir:./uploads/avatars}")
    private String uploadDir;

    @Override
    public void run(String... args) throws Exception {
        // 确保上传目录存在
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                log.info("Created avatar upload directory: {}", path.toAbsolutePath());
            } catch (Exception e) {
                log.error("Failed to create avatar upload directory: {}", path.toAbsolutePath(), e);
            }
        } else {
            log.info("Avatar upload directory exists: {}", path.toAbsolutePath());
        }
    }
}