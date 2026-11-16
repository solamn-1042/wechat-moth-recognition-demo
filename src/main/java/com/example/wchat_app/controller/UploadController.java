package com.example.wchat_app.controller;

import com.example.wchat_app.service.DetectionRecordService;
import com.example.wchat_app.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.json.JSONObject;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("/user")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private DetectionRecordService detectionRecordService;

    @PostMapping("/request")
    public ResponseEntity<String> upload(@RequestParam("img") MultipartFile file,
                                         @RequestParam(value = "userId",required = false) Integer userId) {
        try {
            String retJsonStr = uploadService.handleImageUpload(file);
            JSONObject retJson = new JSONObject(retJsonStr);

            // 从上传服务返回的JSON中获取保存的图片路径
            String resultImagePath = retJson.getString("result_image_path"); // "/result/xxxx.jpg"
            String originalName = retJson.getString("original_name"); // 获取原始图片名称
            String originalImagePath = "/img/" + originalName;  // 构建原始图片路径
            log.info("获取到的结果图片路径为：{}", resultImagePath);

            // 如果提供了用户ID且有识别结果，保存记录到数据库
            if (userId != null && retJson.has("main_detection") && !retJson.isNull("main_detection")) {
                JSONObject mainDetection = retJson.getJSONObject("main_detection");
                String mothType = mainDetection.getString("class");
                double confidence = mainDetection.getDouble("confidence");

                // 保存识别记录到数据库
                detectionRecordService.saveRecord(
                        userId,
                        originalImagePath,
                        resultImagePath,
                        mothType,
                        confidence
                );
                log.info("成功保存识别记录，用户ID: {}, 飞蛾类型: {}, 置信度: {}",
                        userId, mothType, confidence);
            } else if (userId == null) {
                log.warn("未提供用户ID，跳过保存记录");
            } else {
                log.warn("未检测到主要识别结果，跳过保存记录");
            }

            // 转成项目根路径对应的文件系统绝对路径，假设你的静态资源根目录是项目根目录
            // 你可能需要根据实际情况修改路径前缀
            File fullPathFile = new File(System.getProperty("user.dir"), "wchat_app" + resultImagePath);
            String fullPath = fullPathFile.getAbsolutePath();
            log.info("获取到的绝对路径为：{}", fullPath);

            // 赋值给 ResultController 静态变量，后续 /user/get 接口用来返回图片
            ResultController.setLastOutputPath(fullPath);

            return ResponseEntity.ok(retJsonStr);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"message\":\"fail\"}");
        }
    }
}
