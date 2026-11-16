package com.example.wchat_app.service;

import com.example.wchat_app.dto.LoginRequestDto;
import com.example.wchat_app.dto.LoginResponseDto;
import com.example.wchat_app.dto.UserInfoDto;
import com.example.wchat_app.dto.UserRegisterDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    /**
     * 用户注册
     * @param registerDTO 注册信息传输对象
     */
    void register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     * @param loginRequest 登录请求传输对象
     * @return 登录响应传输对象，包含用户信息和token
     */
    LoginResponseDto login(LoginRequestDto loginRequest);


    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoDto getUserInfo(Integer userId);

    /**
     * 更新用户资料
     * @param userInfo 更新的资料信息
     * @return 是否更新成功
     */
    boolean updateProfile(UserInfoDto userInfo);

    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Integer userId, String oldPassword, String newPassword);

    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param avatarFile 头像文件
     * @return 头像URL
     */
    String uploadAvatar(Integer userId, MultipartFile avatarFile);
}