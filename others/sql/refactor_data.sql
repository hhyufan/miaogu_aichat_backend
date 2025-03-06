USE `ry-vue`;

DROP DATABASE IF EXISTS `ry-vue`;
CREATE DATABASE `ry-vue` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS friend;
DROP TABLE IF EXISTS chat3_5_message;
DROP TABLE IF EXISTS chat4_message;
DROP TABLE IF EXISTS user;
CREATE TABLE IF NOT EXISTS user (
                                    id INT AUTO_INCREMENT  NOT NULL PRIMARY KEY,
                                    username VARCHAR(50) NOT NULL UNIQUE ,
                                    password VARCHAR(100) NOT NULL,
                                    email VARCHAR(50) NOT NULL UNIQUE
);


-- 创建好友表
CREATE TABLE friend (
                                      id VARCHAR(20) NOT NULL PRIMARY KEY,
                                      name VARCHAR(50) NOT NULL,
                                      detail VARCHAR(100)
);

-- 创建ChatGPT 3.5消息表
CREATE TABLE  chat3_5_message (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               time DATETIME NOT NULL,
                                               content TEXT NOT NULL,
                                               role VARCHAR(20) NOT NULL,
                                               username VARCHAR(50) NOT NULL,
                                               delete_version INT DEFAULT 0 NOT NULL
);

-- 创建ChatGPT 4消息表
CREATE TABLE chat4_message (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             time DATETIME NOT NULL,
                                             content TEXT NOT NULL,
                                             role VARCHAR(20) NOT NULL,
                                             username VARCHAR(50) NOT NULL,
                                             delete_version INT DEFAULT 0 NOT NULL
);

-- 插入初始好友数据
INSERT INTO friend (id, name, detail) VALUES
                                                    ('1001', 'baka幼犬酱', 'ChatGPT User'),
                                                    ('1002', '日富美', 'ChatGPT3.5'),
                                                    ('1003', '白州梓', 'ChatGPT4o-mini');

-- 插入初始聊天记录（ChatGPT 3.5）
INSERT INTO chat3_5_message (time, content, role, username) VALUES
                                                 ('2023-01-01 09:12:00', '在吗？', '1001', 'newUser'),
                                                 ('2023-01-01 09:12:00', '怎么了？', '1002', 'newUser');

-- 插入初始聊天记录（ChatGPT 4）
INSERT INTO chat4_message (time, content, role, username) VALUES
                                               ('2023-01-01 09:12:00', '在干嘛呢', '1001', 'newUser'),
                                               ('2023-01-01 09:12:00', '吃饭', '1002', 'newUser');
