package com.example.wchat_app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/user")
public class ResultController {

    // 假设 lastOutputPath 由其他服务设置，建议使用更安全的方式（如数据库或会话存储）
    private static String lastOutputPath;

    public static void setLastOutputPath(String path) {
        lastOutputPath = path;
    }

    @GetMapping("/get")
    public ResponseEntity<Resource> getResultImage() {
        // 检查 lastOutputPath 是否有效
        if (lastOutputPath == null || lastOutputPath.trim().isEmpty()) {
            log.warn("lastOutputPath is null or empty");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        // 构造文件路径
        Path filePath = Paths.get(lastOutputPath).normalize();
        File file = filePath.toFile();

        // 检查文件是否存在
        if (!file.exists() || !file.isFile()) {
            log.warn("File does not exist or is not a file: {}", lastOutputPath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        try {
            // 确定文件 MIME 类型
            String mimeType = Files.probeContentType(filePath);
            MediaType mediaType = mimeType != null ? MediaType.parseMediaType(mimeType) : MediaType.APPLICATION_OCTET_STREAM;

            // 构造 Resource 对象
            Resource resource = new FileSystemResource(file);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(file.length());
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");

            log.info("Serving file: {}", lastOutputPath);
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error reading file: {}", lastOutputPath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


}