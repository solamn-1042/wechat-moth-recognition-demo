package com.example.wchat_app.service.Impl;

import com.example.wchat_app.entity.DetectionRecord;
import com.example.wchat_app.mapper.DetectionRecordMapper;
import com.example.wchat_app.service.DetectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class DetectionRecordServiceImpl implements DetectionRecordService {

    @Autowired
    private DetectionRecordMapper detectionRecordMapper;

    @Override
    public DetectionRecord saveRecord(Integer userId, String originalImgPath, String resultImgPath,
                                      String mothType, Double confidence) {
        DetectionRecord record = new DetectionRecord();
        record.setUserId(userId);
        record.setOriginalImgPath(originalImgPath);
        record.setResultImgPath(resultImgPath);
        record.setMothType(mothType);

        // 如果有置信度，转换为BigDecimal
        if (confidence != null) {
            record.setConfidence(BigDecimal.valueOf(confidence));
        }

        detectionRecordMapper.insert(record);
        return record;
    }

    @Override
    public List<DetectionRecord> getUserRecords(Integer userId, int page, int size) {
        int offset = (page - 1) * size;
        return detectionRecordMapper.findByUserId(userId, offset, size);
    }

    @Override
    public int getUserRecordCount(Integer userId) {
        return detectionRecordMapper.countByUserId(userId);
    }

    @Override
    public DetectionRecord getRecordById(Integer recordId) {
        if (recordId == null) {
            return null;
        }
        return detectionRecordMapper.getRecordById(recordId);
    }
}