package com.example.wchat_app.dto;

import lombok.Data;

@Data
public class LoginResponseDto {
    private Integer id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String token;
}