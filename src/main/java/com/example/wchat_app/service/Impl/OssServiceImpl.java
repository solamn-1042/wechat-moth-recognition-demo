package com.example.wchat_app.service.Impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.example.wchat_app.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云OSS服务实现类
 */
@Service
@Slf4j
public class OssServiceImpl implements OssService {
    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.urlPrefix}")
    private String urlPrefix;

    @Value("${aliyun.oss.avatarDir}")
    private String avatarDir;

    @Override
    public String uploadFile(MultipartFile file, String fileDir) {
        if (file == null || file.isEmpty()) {
            log.warn("上传的文件为空");
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;
        String objectName = fileDir + fileName;

        try (InputStream inputStream = file.getInputStream()) {
            // 设置文件元信息
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(fileExtension));
            metadata.setContentLength(file.getSize());

            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream, metadata);
            ossClient.putObject(putObjectRequest);

            log.info("文件上传成功，路径：{}", objectName);
            return urlPrefix + objectName;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return null;
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        return uploadFile(file, avatarDir);
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            if (objectName != null && objectName.startsWith(urlPrefix)) {
                objectName = objectName.substring(urlPrefix.length());
            }

            if (objectName != null && !objectName.isEmpty()) {
                ossClient.deleteObject(bucketName, objectName);
                log.info("文件删除成功，路径：{}", objectName);
            } else {
                log.warn("删除文件的路径为空");
            }
        } catch (Exception e) {
            log.error("文件删除失败", e);
        }
    }

    /**
     * 根据文件扩展名获取内容类型
     * @param fileExtension 文件扩展名
     * @return 内容类型
     */
    private String getContentType(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            return "application/octet-stream";
        }

        fileExtension = fileExtension.toLowerCase();
        if (fileExtension.equals(".jpg") || fileExtension.equals(".jpeg")) {
            return "image/jpeg";
        } else if (fileExtension.equals(".png")) {
            return "image/png";
        } else if (fileExtension.equals(".gif")) {
            return "image/gif";
        } else if (fileExtension.equals(".bmp")) {
            return "image/bmp";
        } else if (fileExtension.equals(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
        }
    }
}