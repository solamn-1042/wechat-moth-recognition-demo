package com.example.wchat_app.controller;

import com.example.wchat_app.common.Result;
import com.example.wchat_app.dto.LoginRequestDto;
import com.example.wchat_app.dto.LoginResponseDto;
import com.example.wchat_app.dto.UserInfoDto;
import com.example.wchat_app.dto.UserRegisterDTO;
import com.example.wchat_app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService; // 注入的是接口，而不是实现类

    /**
     * 用户注册接口
     *
     * @param registerDTO 注册信息传输对象
     * @return Result<Void> 注册结果
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterDTO registerDTO) {
        try {
            // 参数校验
            if (registerDTO.getUsername() == null || registerDTO.getUsername().length() < 3) {
                return Result.error(400, "用户名不能少于3个字符");
            }

            if (registerDTO.getPassword() == null || registerDTO.getPassword().length() < 6) {
                return Result.error(400, "密码不能少于6个字符");
            }

            userService.register(registerDTO);
            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("用户注册失败", e);
            if (e.getMessage().contains("用户名已存在")) {
                return Result.error(400, "用户名已存在");
            }
            return Result.error(500, "注册失败，请稍后重试");
        }
    }

    /**
     * 用户登录接口
     *
     * @param loginRequest 登录请求传输对象
     * @return Result<LoginResponseDto> 登录结果
     */
    @PostMapping("/login")
    public Result<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        LoginResponseDto loginResponse = userService.login(loginRequest);

        if (loginResponse != null) {
            return Result.success("登录成功", loginResponse);
        } else {
            return Result.error(400, "用户名或密码错误");
        }
    }

    @GetMapping("/{userId}")
    public Result<UserInfoDto> getUserInfo(@PathVariable Integer userId) {
        UserInfoDto userInfo = userService.getUserInfo(userId);

        if (userInfo != null) {
            return Result.success("获取用户信息成功", userInfo);
        } else {
            return Result.error(404, "用户不存在");
        }
    }

    @PostMapping("/updateProfile")
    public Result<Object> updateProfile(@RequestBody UserInfoDto userInfo) {
        boolean success = userService.updateProfile(userInfo);

        if (success) {
            return Result.success("个人资料更新成功");
        } else {
            return Result.error(400, "更新失败，用户不存在");
        }
    }

    @PostMapping("/changePassword")
    public Result<Object> changePassword(@RequestBody UserInfoDto userInfo) {
        boolean success = userService.changePassword(
                userInfo.getId(),
                userInfo.getOldPassword(),
                userInfo.getNewPassword());

        if (success) {
            return Result.success("密码修改成功");
        } else {
            return Result.error(400, "修改失败，请检查原密码是否正确");
        }
    }

    @PostMapping("/avatar")
    public Result<Object> uploadAvatar(
            @RequestParam("userId") Integer userId,
            @RequestParam("avatar") MultipartFile avatar) {

        String avatarUrl = userService.uploadAvatar(userId, avatar);

        if (avatarUrl != null) {
            return Result.success("头像上传成功", new UserInfoDto(){{
                setAvatarUrl(avatarUrl);
            }});
        } else {
            return Result.error(400, "头像上传失败");
        }
    }


}