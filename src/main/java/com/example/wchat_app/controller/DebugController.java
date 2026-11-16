package com.example.wchat_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    //http://localhost:8080/debug/checkFile?filename=08e53254-4f58-4c02-9cb2-c4f9fe9a7af4.jpg
    @GetMapping("/checkFile")
    public Map<String, Object> checkFile(@RequestParam String filename) {
        Map<String, Object> result = new HashMap<>();

        String projectPath = System.getProperty("user.dir");
        String path1 = projectPath + "/result/" + filename;
        String path2 = projectPath + "/wchat_app/result/" + filename;

        File file1 = new File(path1);
        File file2 = new File(path2);

        result.put("path1", path1);
        result.put("path1Exists", file1.exists());
        result.put("path2", path2);
        result.put("path2Exists", file2.exists());

        return result;
    }
}