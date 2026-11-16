CREATE DATABASE IF NOT EXISTS wechat_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wechat_app;
CREATE TABLE IF NOT EXISTS user (
 id INT AUTO_INCREMENT PRIMARY KEY,
 username VARCHAR(100) NOT NULL UNIQUE,
 password VARCHAR(255) NOT NULL,
 nickname VARCHAR(100), avatar_url VARCHAR(1024),
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
 update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS detection_record (
 id INT AUTO_INCREMENT PRIMARY KEY,
 user_id INT NOT NULL,
 original_img_path VARCHAR(1024), result_img_path VARCHAR(1024),
 moth_type VARCHAR(100), confidence DECIMAL(9,8),
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_user_created(user_id,create_time),
 CONSTRAINT fk_detection_user FOREIGN KEY (user_id) REFERENCES user(id)
);
