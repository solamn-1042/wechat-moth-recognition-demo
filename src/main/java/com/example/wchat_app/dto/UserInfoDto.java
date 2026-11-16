package com.example.wchat_app.dto;

import lombok.Data;

/**
 * 用户信息数据传输对象，用于用户注册、更新等操作
 */
@Data
public class UserInfoDto {
    private Integer id;
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String token;

    // 以下字段用于密码修改
    private String oldPassword;
    private String newPassword;
}