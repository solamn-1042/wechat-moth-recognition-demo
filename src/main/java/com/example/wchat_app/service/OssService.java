package com.example.wchat_app.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云OSS服务接口
 */
public interface OssService {

    /**
     * 上传文件到OSS
     * @param file 文件
     * @param fileDir 文件目录
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String fileDir);

    /**
     * 上传头像到OSS
     * @param file 头像文件
     * @return 头像访问URL
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 删除OSS中的文件
     * @param objectName 对象名称（完整的URL或对象路径）
     */
    void deleteFile(String objectName);
}