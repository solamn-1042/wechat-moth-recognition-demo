package com.example.wchat_app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user")
public class Hello {

    @GetMapping("/hello")
    public String hello() {

        log.info("hello start");
        return "Hello World";
    }


}
