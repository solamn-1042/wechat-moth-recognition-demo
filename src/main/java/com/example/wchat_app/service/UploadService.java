package com.example.wchat_app.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String handleImageUpload(MultipartFile file) throws Exception;

}