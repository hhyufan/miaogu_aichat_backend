CREATE DATABASE IF NOT EXISTS `miaogu_aichat` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `miaogu_aichat`;

-- 创建好友表（如果不存在）
CREATE TABLE IF NOT EXISTS user (
                                      id INT AUTO_INCREMENT  NOT NULL PRIMARY KEY,
                                      username VARCHAR(50) NOT NULL,
                                      password VARCHAR(100) NOT NULL,
                                      email VARCHAR(50) NOT NULL
);

-- 创建好友表（如果不存在）
CREATE TABLE IF NOT EXISTS friend (
                                      id VARCHAR(20) NOT NULL PRIMARY KEY,
                                      name VARCHAR(50) NOT NULL,
                                      detail VARCHAR(100)
);

-- 创建ChatGPT 3.5消息表
CREATE TABLE IF NOT EXISTS chat3_5_message (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               time DATETIME NOT NULL,
                                               content TEXT NOT NULL,
                                               role VARCHAR(20) NOT NULL,
                                               username VARCHAR(50) NOT NULL,
                                               delete_version INT DEFAULT 0 NOT NULL

);

-- 创建ChatGPT 4消息表
CREATE TABLE IF NOT EXISTS chat4_message (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             time DATETIME NOT NULL,
                                             content TEXT NOT NULL,
                                             role VARCHAR(20) NOT NULL,
                                             username VARCHAR(50) NOT NULL,
                                             delete_version INT DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS deepSeek_message (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             time DATETIME NOT NULL,
                                             content TEXT NOT NULL,
                                             role VARCHAR(20) NOT NULL,
                                             username VARCHAR(50) NOT NULL,
                                             delete_version INT DEFAULT 0 NOT NULL
);

-- 插入初始好友数据
INSERT IGNORE INTO friend (id, name, detail) VALUES
                                                    ('1001', 'baka幼犬酱', 'ChatGPT User'),
                                                    ('1002', '日富美', 'ChatGPT3.5'),
                                                    ('1003', '白州梓', 'ChatGPT4o-mini'),
                                                    ('1004', 'kafuu', 'DeepSeek V3');

-- ALTER TABLE chat3_5_message
--     ADD COLUMN delete_version INT DEFAULT 0 NOT NULL COMMENT '删除版本号（0=未删除）';
-- ALTER TABLE chat4_message
--     ADD COLUMN delete_version INT DEFAULT 0 NOT NULL COMMENT '删除版本号（0=未删除）';
