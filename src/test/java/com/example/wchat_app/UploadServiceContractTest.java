package com.example.wchat_app;

import com.example.wchat_app.service.Impl.UploadServiceImpl;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.json.JSONObject;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class UploadServiceContractTest {
    HttpServer server;
    UploadServiceImpl service;
    String base, resultName="contract-test.jpg", resultOrigin;
    @BeforeEach void setup() throws Exception {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        base="http://127.0.0.1:"+server.getAddress().getPort();resultOrigin=base;
        server.createContext("/request",exchange->{
            exchange.getRequestBody().readAllBytes();
            byte[] bytes=("{\"result_url\":\""+resultOrigin+"/result/"+resultName+"\",\"image_name\":\""+resultName+"\",\"detections\":[],\"main_detection\":null}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.createContext("/result/",exchange->{byte[] bytes={1,2,3};exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();});
        server.start();service=new UploadServiceImpl();ReflectionTestUtils.setField(service,"inferenceBaseUrl",base);
    }
    @AfterEach void cleanup(){server.stop(0);}
    MockMultipartFile image(){return new MockMultipartFile("img","sample.jpg","image/jpeg",new byte[]{1,2,3});}
    @Test void emptyDetectionStillReturnsPerRequestImage() throws Exception {
        JSONObject response=new JSONObject(service.handleImageUpload(image()));
        assertTrue(response.isNull("main_detection"));
        assertEquals("/result/contract-test.jpg",response.getString("result_image_path"));
    }
    @Test void unexpectedImageOriginIsRejected(){resultOrigin="http://example.invalid";assertThrows(java.io.IOException.class,()->service.handleImageUpload(image()));}
    @Test void traversalImageNameIsRejected(){resultName="../escape.jpg";assertThrows(java.io.IOException.class,()->service.handleImageUpload(image()));}
}
