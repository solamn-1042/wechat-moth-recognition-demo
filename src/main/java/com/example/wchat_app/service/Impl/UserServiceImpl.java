package com.example.wchat_app.service.Impl;

import com.example.wchat_app.dto.LoginRequestDto;
import com.example.wchat_app.dto.LoginResponseDto;
import com.example.wchat_app.dto.UserInfoDto;
import com.example.wchat_app.dto.UserRegisterDTO;
import com.example.wchat_app.entity.User;
import com.example.wchat_app.mapper.UserMapper;
import com.example.wchat_app.service.OssService;
import com.example.wchat_app.service.UserService;
import com.example.wchat_app.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private OssService ossService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void register(UserRegisterDTO registerDTO) {
        // 检查用户名是否存在
        if (userMapper.checkUsernameExists(registerDTO.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 加密密码
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        // 设置昵称，如果为空则使用用户名
        String nickname = registerDTO.getNickname();
        if (!StringUtils.hasText(nickname)) {
            nickname = registerDTO.getUsername();
        }
        user.setNickname(nickname);

        // 保存用户
        userMapper.insertUser(user);
        log.info("用户注册成功，用户名：{}", user.getUsername());
    }

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        // 查找用户
        User user = userMapper.findByUsername(loginRequest.getUsername());
//        log.info("从数据库获取的用户信息: {}", user);
        // 用户不存在或密码不匹配
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return null;
        }

        // 更新最后登录时间
        userMapper.updateLoginTime(user.getId());


        // 生成token
        String token = tokenUtils.generateToken(user.getId(), user.getUsername());

        // 构建响应
        LoginResponseDto responseDto = new LoginResponseDto();
        responseDto.setId(user.getId());
        responseDto.setUsername(user.getUsername());
        responseDto.setNickname(user.getNickname());
        responseDto.setAvatarUrl(user.getAvatarUrl());
        responseDto.setToken(token);

        log.info("用户登录成功，用户id：{}，用户名：{}，头像URL：{}", user.getId(),user.getUsername(), user.getAvatarUrl());
        return responseDto;
    }

    @Override
    public UserInfoDto getUserInfo(Integer userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return null;
        }

        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setId(user.getId());
        userInfoDto.setUsername(user.getUsername());
        userInfoDto.setNickname(user.getNickname());
        userInfoDto.setAvatarUrl(user.getAvatarUrl());

        return userInfoDto;
    }

    @Override
    @Transactional
    public boolean updateProfile(UserInfoDto userInfo) {
        // 确保用户存在
        User user = userMapper.findById(userInfo.getId());
        if (user == null) {
            return false;
        }

        // 更新昵称
        int result = userMapper.updateNickname(userInfo.getId(), userInfo.getNickname());

        return result > 0;
    }

    @Override
    @Transactional
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        if (userId == null) {
            log.warn("User ID is null. Cannot change password.");
            return false;
        }

        // 找到用户
        User user = userMapper.findById(userId);
        if (user == null) {
            log.warn("User not found. ID: {}", userId);
            return false;
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        // 设置新密码
        String encryptedPassword = passwordEncoder.encode(newPassword);
        int result = userMapper.updatePassword(userId, encryptedPassword);

        return result > 0;
    }

    @Override
    @Transactional
    public String uploadAvatar(Integer userId, MultipartFile avatarFile) {
        // 确保用户存在
        User user = userMapper.findById(userId);
        if (user == null) {
            log.warn("用户不存在，ID: {}", userId);
            return null;
        }

        if (avatarFile == null || avatarFile.isEmpty()) {
            log.warn("上传的头像文件为空");
            return null;
        }

        // 删除旧头像（如果需要）
        if (user.getAvatarUrl() != null && user.getAvatarUrl().contains("aliyuncs.com")) {
            ossService.deleteFile(user.getAvatarUrl());
        }

        // 上传新头像到OSS
        String avatarUrl = ossService.uploadAvatar(avatarFile);
        if (avatarUrl == null) {
            log.error("头像上传到OSS失败");
            return null;
        }

        // 更新用户头像URL
        int updated = userMapper.updateAvatarUrl(userId, avatarUrl);
        if (updated <= 0) {
            log.warn("更新用户头像URL失败");
            return null;
        }

        log.info("用户头像更新成功，URL: {}", avatarUrl);
        return avatarUrl;
    }
}