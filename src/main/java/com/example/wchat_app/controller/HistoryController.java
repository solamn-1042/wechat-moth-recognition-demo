package com.example.wchat_app.controller;

import com.example.wchat_app.entity.DetectionRecord;
import com.example.wchat_app.service.DetectionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class HistoryController {
    @org.springframework.beans.factory.annotation.Value("${app.public-base-url}")
    private String publicBaseUrl;

    @Autowired
    private DetectionRecordService detectionRecordService;

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistoryRecords(
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        if (userId == null) {
            log.warn("请求历史记录时未提供userId或格式不正确");
            Map<String, Object> error = new HashMap<>();
            error.put("message", "请提供有效的用户ID");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            List<DetectionRecord> records = detectionRecordService.getUserRecords(userId, page, size);

            // 处理图片路径，添加服务器基础URL
            String baseUrl = publicBaseUrl; // 修改为您服务器的实际地址和端口
            // 处理记录中的图片路径
            for (DetectionRecord record : records) {
                if (record.getOriginalImgPath() != null && !record.getOriginalImgPath().startsWith("http")) {
                    record.setOriginalImgPath(baseUrl + record.getOriginalImgPath());
                }
                if (record.getResultImgPath() != null && !record.getResultImgPath().startsWith("http")) {
                    record.setResultImgPath(baseUrl + record.getResultImgPath());
                }
            }

            int total = detectionRecordService.getUserRecordCount(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("records", records);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询历史记录失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "查询历史记录失败");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/history/image")
    public ResponseEntity<Object> getHistoryImage(@RequestParam("recordId") Integer recordId) {
        try {
            if (recordId == null) {
                log.warn("请求记录详情时未提供recordId");
                Map<String, Object> error = new HashMap<>();
                error.put("message", "请提供有效的记录ID");
                return ResponseEntity.badRequest().body(error);
            }

            DetectionRecord record = detectionRecordService.getRecordById(recordId);

            if (record == null) {
                log.warn("请求的记录不存在: {}", recordId);
                return ResponseEntity.notFound().build();
            }

            // 处理图片路径
            String baseUrl = publicBaseUrl; // 使用正确的端口

            if (record.getOriginalImgPath() != null && !record.getOriginalImgPath().startsWith("http")) {
                record.setOriginalImgPath(baseUrl + record.getOriginalImgPath());
            }

            if (record.getResultImgPath() != null && !record.getResultImgPath().startsWith("http")) {
                record.setResultImgPath(baseUrl + record.getResultImgPath());
            }

            return ResponseEntity.ok(record);
        } catch (Exception e) {
            log.error("获取记录详情失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "获取记录详情失败");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}