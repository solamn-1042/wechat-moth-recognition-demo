package com.example.wchat_app.service;

import com.example.wchat_app.entity.DetectionRecord;

import java.util.List;

public interface DetectionRecordService {
    DetectionRecord saveRecord(Integer userId, String originalImgPath, String resultImgPath,
                               String mothType, Double confidence);

    List<DetectionRecord> getUserRecords(Integer userId, int page, int size);

    int getUserRecordCount(Integer userId);

    DetectionRecord getRecordById(Integer recordId);
}