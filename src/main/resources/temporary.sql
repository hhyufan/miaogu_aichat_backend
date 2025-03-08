-- 创建新数据库
CREATE DATABASE `miaogu_aichat`;

-- 将旧数据库中的所有表复制到新数据库
RENAME TABLE `ry-vue`.chat3_5_message TO `miaogu_aichat`.chat3_5_message;
RENAME TABLE `ry-vue`.chat4_message TO `miaogu_aichat`.chat4_message;
RENAME TABLE `ry-vue`.deepseek_message TO `miaogu_aichat`.deepseek_message;
RENAME TABLE `ry-vue`.friend TO `miaogu_aichat`.friend;
RENAME TABLE `ry-vue`.user TO `miaogu_aichat`.user;

-- 删除旧数据库（可选）
DROP DATABASE `ry-vue`;
