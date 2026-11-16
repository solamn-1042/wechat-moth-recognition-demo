package com.example.wchat_app.mapper;

import com.example.wchat_app.entity.DetectionRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Options;
import java.util.List;

@Mapper
public interface DetectionRecordMapper {

    @Insert("INSERT INTO detection_record(user_id, original_img_path, result_img_path, moth_type, confidence) " +
            "VALUES(#{userId}, #{originalImgPath}, #{resultImgPath}, #{mothType}, #{confidence})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DetectionRecord record);

    @Select("SELECT * FROM detection_record WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<DetectionRecord> findByUserId(Integer userId, int offset, int limit);

    @Select("SELECT COUNT(*) FROM detection_record WHERE user_id = #{userId}")
    int countByUserId(Integer userId);

    @Select("SELECT * FROM detection_record WHERE id = #{recordId}")
    DetectionRecord getRecordById(Integer recordId);
}