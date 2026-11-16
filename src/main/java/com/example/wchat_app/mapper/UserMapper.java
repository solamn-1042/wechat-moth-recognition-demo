package com.example.wchat_app.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import com.example.wchat_app.entity.User;

@Mapper
public interface UserMapper {

    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    int checkUsernameExists(@Param("username") String username);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Insert("INSERT INTO user (username, password, nickname, avatar_url) " +
            "VALUES (#{username}, #{password}, #{nickname}, #{avatarUrl})")
    int insertUser(User user);

    @Update("UPDATE user SET update_time = now() WHERE id = #{id}")
    int updateLoginTime(@Param("id") Integer id);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Integer id);

    @Update("UPDATE user SET nickname = #{nickname}, update_time = now() WHERE id = #{id}")
    int updateNickname(@Param("id") Integer id, @Param("nickname") String nickname);

    @Update("UPDATE user SET password = #{password}, update_time = now() WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    @Update("UPDATE user SET avatar_url = #{avatarUrl}, update_time = now() WHERE id = #{id}")
    int updateAvatarUrl(@Param("id") Integer id, @Param("avatarUrl") String avatarUrl);
}