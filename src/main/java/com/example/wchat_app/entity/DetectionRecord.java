package com.example.wchat_app.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DetectionRecord {
    private Integer id;
    private Integer userId;
    private String originalImgPath;
    private String resultImgPath;
    private String mothType;
    private BigDecimal confidence;
    private LocalDateTime createTime;
}