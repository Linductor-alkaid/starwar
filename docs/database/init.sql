-- ========================================
-- 星际防卫战 - 数据库初始化脚本
-- 数据库名称：star_war_db
-- 字符集：utf8mb4
-- 创建时间：2025年
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `star_war_db` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `star_war_db`;

-- ========================================
-- 删除表（按照依赖关系的逆序删除，先删除有外键约束的表）
-- ========================================
-- 先删除回复表（依赖 post 和 user）
DROP TABLE IF EXISTS `reply`;
-- 再删除帖子表（依赖 user）
DROP TABLE IF EXISTS `post`;
-- 再删除游戏记录表（依赖 user）
DROP TABLE IF EXISTS `game_record`;
-- 最后删除用户表（被其他表引用）
DROP TABLE IF EXISTS `user`;

-- ========================================
-- 1. 用户表（user）
-- ========================================
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 2. 游戏记录表（game_record）
-- ========================================
CREATE TABLE `game_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `score` INT NOT NULL COMMENT '得分',
  `level` INT NOT NULL COMMENT '等级',
  `play_time` INT NOT NULL COMMENT '游戏时长（秒）',
  `record_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_score` (`score`),
  KEY `idx_record_date` (`record_date`),
  CONSTRAINT `fk_game_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏记录表';

-- ========================================
-- 3. 帖子表（post）
-- ========================================
CREATE TABLE `post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '发布者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '帖子标题',
  `content` TEXT NOT NULL COMMENT '帖子内容',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `reply_count` INT NOT NULL DEFAULT 0 COMMENT '回复次数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1:正常 0:删除）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_status` (`status`),
  KEY `idx_view_count` (`view_count`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- ========================================
-- 4. 回复表（reply）
-- ========================================
CREATE TABLE `reply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '回复者ID',
  `content` TEXT NOT NULL COMMENT '回复内容',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父回复ID（用于嵌套回复）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1:正常 0:删除）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_reply_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reply_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回复表';

-- ========================================
-- 插入测试数据
-- ========================================

-- 插入测试用户（密码为123456加密后的值，实际使用时需要替换）
INSERT INTO `user` (`username`, `password`, `email`, `nickname`, `avatar`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iyKvjh.VW', 'admin@example.com', '管理员', NULL),
('testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iyKvjh.VW', 'user1@example.com', '测试用户1', NULL),
('testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iyKvjh.VW', 'user2@example.com', '测试用户2', NULL);

-- 插入测试游戏记录
INSERT INTO `game_record` (`user_id`, `score`, `level`, `play_time`) VALUES
(1, 5000, 10, 300),
(2, 3500, 7, 210),
(3, 2800, 5, 180),
(1, 4200, 8, 250),
(2, 3000, 6, 195);

-- 插入测试帖子
INSERT INTO `post` (`user_id`, `title`, `content`, `view_count`, `reply_count`) VALUES
(1, '欢迎来到星际防卫战！', '这是一个回复歼击类游戏，欢迎各位玩家交流心得。', 156, 12),
(2, '游戏攻略分享', '我来分享一下我的游戏攻略，希望对新手有帮助。首先要注意...', 89, 5),
(3, '高分技巧', '如何获得高分？我来教你几个技巧...', 67, 3);

-- 插入测试回复
INSERT INTO `reply` (`post_id`, `user_id`, `content`, `parent_id`) VALUES
(1, 2, '感谢分享，学到了很多！', NULL),
(1, 3, '我也来分享一个技巧...', NULL),
(2, 1, '很有用的攻略，谢谢！', NULL),
(2, 3, '我试了一下，真的很有效！', NULL),
(3, 1, '学到了，马上试试', NULL),
(3, 2, '确实有效，我的分数提高了！', NULL);

-- ========================================
-- 初始化完成
-- ========================================
SELECT '数据库初始化完成！' AS message;

