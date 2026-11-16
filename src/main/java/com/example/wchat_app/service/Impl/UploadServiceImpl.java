package com.example.wchat_app.service.Impl;

import com.example.wchat_app.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {
    @org.springframework.beans.factory.annotation.Value("${inference.base-url}")
    private String inferenceBaseUrl;

    @Override
    public String handleImageUpload(MultipartFile file) throws Exception {
        String uniqueImgName = UUID.randomUUID().toString() + ".jpg";
        File imgDir = new File("wchat_app/img");
        if (!imgDir.exists()) imgDir.mkdirs();
        File localImg = new File(imgDir, uniqueImgName);

        try (OutputStream os = new FileOutputStream(localImg)) {
            os.write(file.getBytes());
        }

        // 调用 Flask
        URL url = new URL(inferenceBaseUrl + "/request");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(60000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        String boundary = UUID.randomUUID().toString();
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = conn.getOutputStream()) {
            outputStream.write(("--" + boundary + "\r\n").getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"img\"; filename=\"" + uniqueImgName + "\"\r\n").getBytes());
            outputStream.write("Content-Type: image/jpeg\r\n\r\n".getBytes());
            Files.copy(localImg.toPath(), outputStream);
            outputStream.write("\r\n--".getBytes());
            outputStream.write((boundary + "--\r\n").getBytes());
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (InputStream is = conn.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                String flaskResponse = responseBuilder.toString();

                // 解析 JSON
                JSONObject jsonObject = new JSONObject(flaskResponse);
                String resultUrl = jsonObject.getString("result_url");
                String imageName = jsonObject.getString("image_name");
                if (!imageName.matches("[a-zA-Z0-9-]+\\.jpg")) throw new IOException("Invalid inference image name");

                // 下载识别结果图片
                URL imageUrl = new URL(resultUrl);
                URL trusted = new URL(inferenceBaseUrl);
                if (!imageUrl.getProtocol().equals(trusted.getProtocol()) || !imageUrl.getHost().equals(trusted.getHost()) || imageUrl.getPort() != trusted.getPort()) throw new IOException("Unexpected inference image origin");
                File resultDir = new File("wchat_app/result");
                if (!resultDir.exists()) resultDir.mkdirs();
                File resultImage = new File(resultDir, imageName);

                java.net.URLConnection imageConnection = imageUrl.openConnection();
                imageConnection.setConnectTimeout(5000);
                imageConnection.setReadTimeout(60000);
                try (InputStream imgInput = imageConnection.getInputStream();
                     OutputStream imgOut = new FileOutputStream(resultImage)) {
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = imgInput.read(buffer)) != -1) {
                        imgOut.write(buffer, 0, n);
                    }
                }
                log.info("图片下载成功");

                // 创建返回JSON，保留Flask原始响应中的所有字段
                JSONObject retJson = new JSONObject();
                retJson.put("message", "success");
                retJson.put("original_name", uniqueImgName);
                retJson.put("result_image_path", "/result/" + imageName);
                retJson.put("result_image_url", resultUrl);

                // 重点：从Flask返回的jsonObject中获取检测结果
                if (jsonObject.has("detections")) {
                    JSONArray detections = jsonObject.getJSONArray("detections");
                    retJson.put("detections", detections);
                    log.info("添加检测结果数组到返回JSON: {}", detections);
                }

                if (jsonObject.has("main_detection") && !jsonObject.isNull("main_detection")) {
                    JSONObject mainDetection = jsonObject.getJSONObject("main_detection");
                    retJson.put("main_detection", mainDetection);
                    log.info("添加主要检测结果到返回JSON: {}", mainDetection);
                } else {
                    retJson.put("main_detection", JSONObject.NULL);
                }

                String finalResponse = retJson.toString();
                log.info("最终返回给前端的JSON: {}", finalResponse);
                return finalResponse;
            }
        } else {
            throw new RuntimeException("Flask prediction failed, code: " + responseCode);
        }
    }
}
