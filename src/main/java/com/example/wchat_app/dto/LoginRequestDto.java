package com.example.wchat_app.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String username;
    private String password;
}