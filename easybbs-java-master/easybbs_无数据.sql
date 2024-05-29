/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50734
Source Host           : localhost:3306
Source Database       : easybbs

Target Server Type    : MYSQL
Target Server Version : 50734
File Encoding         : 65001

Date: 2023-02-15 09:16:19
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for email_code
-- ----------------------------
DROP TABLE IF EXISTS `email_code`;
CREATE TABLE `email_code` (
  `email` varchar(150) NOT NULL COMMENT '邮箱',
  `code` varchar(5) NOT NULL COMMENT '编号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `status` tinyint(1) DEFAULT NULL COMMENT '0:未使用  1:已使用',
  PRIMARY KEY (`email`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮箱验证码';

-- ----------------------------
-- Table structure for forum_article
-- ----------------------------
DROP TABLE IF EXISTS `forum_article`;
CREATE TABLE `forum_article` (
  `article_id` varchar(15) NOT NULL COMMENT '文章ID',
  `board_id` int(11) DEFAULT NULL COMMENT '板块ID',
  `board_name` varchar(50) DEFAULT NULL COMMENT '板块名称',
  `p_board_id` int(11) DEFAULT NULL COMMENT '父级板块ID',
  `p_board_name` varchar(50) DEFAULT NULL COMMENT '父板块名称',
  `user_id` varchar(15) NOT NULL COMMENT '用户ID',
  `nick_name` varchar(20) NOT NULL COMMENT '昵称',
  `user_ip_address` varchar(100) DEFAULT NULL COMMENT '最后登录ip地址',
  `title` varchar(150) NOT NULL COMMENT '标题',
  `cover` varchar(100) DEFAULT NULL COMMENT '封面',
  `content` text COMMENT '内容',
  `markdown_content` text COMMENT 'markdown内容',
  `editor_type` tinyint(4) NOT NULL COMMENT '0:富文本编辑器 1:markdown编辑器',
  `summary` varchar(200) DEFAULT NULL COMMENT '摘要',
  `post_time` datetime NOT NULL COMMENT '发布时间',
  `last_update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `read_count` int(11) DEFAULT '0' COMMENT '阅读数量',
  `good_count` int(11) DEFAULT '0' COMMENT '点赞数',
  `comment_count` int(11) DEFAULT '0' COMMENT '评论数',
  `top_type` tinyint(4) DEFAULT '0' COMMENT '0未置顶  1:已置顶',
  `attachment_type` tinyint(4) DEFAULT NULL COMMENT '0:没有附件  1:有附件',
  `status` tinyint(4) DEFAULT NULL COMMENT '-1已删除 0:待审核  1:已审核 ',
  PRIMARY KEY (`article_id`),
  KEY `idx_board_id` (`board_id`),
  KEY `idx_pboard_id` (`p_board_id`),
  KEY `idx_post_time` (`post_time`),
  KEY `idx_top_type` (`top_type`),
  KEY `idx_title` (`title`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章信息';

-- ----------------------------
-- Table structure for forum_article_attachment
-- ----------------------------
DROP TABLE IF EXISTS `forum_article_attachment`;
CREATE TABLE `forum_article_attachment` (
  `file_id` varchar(15) NOT NULL COMMENT '文件ID',
  `article_id` varchar(15) NOT NULL COMMENT '文章ID',
  `user_id` varchar(15) DEFAULT NULL COMMENT '用户id',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小',
  `file_name` varchar(200) DEFAULT NULL COMMENT '文件名称',
  `download_count` int(11) DEFAULT NULL COMMENT '下载次数',
  `file_path` varchar(100) DEFAULT NULL COMMENT '文件路径',
  `file_type` tinyint(4) DEFAULT NULL COMMENT '文件类型',
  `integral` int(11) DEFAULT NULL COMMENT '下载所需积分',
  PRIMARY KEY (`file_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息';

-- ----------------------------
-- Table structure for forum_article_attachment_download
-- ----------------------------
DROP TABLE IF EXISTS `forum_article_attachment_download`;
CREATE TABLE `forum_article_attachment_download` (
  `file_id` varchar(15) NOT NULL COMMENT '文件ID',
  `user_id` varchar(15) NOT NULL COMMENT '用户id',
  `article_id` varchar(15) NOT NULL COMMENT '文章ID',
  `download_count` int(11) DEFAULT '1' COMMENT '文件下载次数',
  PRIMARY KEY (`file_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户附件下载';

-- ----------------------------
-- Table structure for forum_board
-- ----------------------------
DROP TABLE IF EXISTS `forum_board`;
CREATE TABLE `forum_board` (
  `board_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '板块ID',
  `p_board_id` int(11) DEFAULT NULL COMMENT '父级板块ID',
  `board_name` varchar(50) DEFAULT NULL COMMENT '板块名',
  `cover` varchar(50) DEFAULT NULL COMMENT '封面',
  `board_desc` varchar(150) DEFAULT NULL COMMENT '描述',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `post_type` tinyint(1) DEFAULT '1' COMMENT '0:只允许管理员发帖 1:任何人可以发帖',
  PRIMARY KEY (`board_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10008 DEFAULT CHARSET=utf8mb4 COMMENT='文章板块信息';

-- ----------------------------
-- Table structure for forum_comment
-- ----------------------------
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment` (
  `comment_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `p_comment_id` int(11) DEFAULT NULL COMMENT '父级评论ID',
  `article_id` varchar(15) NOT NULL COMMENT '文章ID',
  `content` varchar(800) DEFAULT NULL COMMENT '回复内容',
  `img_path` varchar(150) DEFAULT NULL COMMENT '图片',
  `user_id` varchar(15) NOT NULL COMMENT '用户ID',
  `nick_name` varchar(20) DEFAULT NULL COMMENT '昵称',
  `user_ip_address` varchar(100) DEFAULT NULL COMMENT '用户ip地址',
  `reply_user_id` varchar(15) DEFAULT NULL COMMENT '回复人ID',
  `reply_nick_name` varchar(20) DEFAULT NULL COMMENT '回复人昵称',
  `top_type` tinyint(4) DEFAULT '0' COMMENT '0:未置顶  1:置顶',
  `post_time` datetime DEFAULT NULL COMMENT '发布时间',
  `good_count` int(11) DEFAULT '0' COMMENT 'good数量',
  `status` tinyint(4) DEFAULT NULL COMMENT '0:待审核  1:已审核',
  PRIMARY KEY (`comment_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_post_time` (`post_time`),
  KEY `idx_top` (`top_type`),
  KEY `idx_p_id` (`p_comment_id`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10011 DEFAULT CHARSET=utf8mb4 COMMENT='评论';

-- ----------------------------
-- Table structure for like_record
-- ----------------------------
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record` (
  `op_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `op_type` tinyint(4) DEFAULT NULL COMMENT '操作类型0:文章点赞 1:评论点赞',
  `object_id` varchar(15) NOT NULL COMMENT '主体ID',
  `user_id` varchar(15) NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT NULL COMMENT '发布时间',
  `author_user_id` varchar(15) DEFAULT NULL COMMENT '主体作者ID',
  PRIMARY KEY (`op_id`),
  UNIQUE KEY `idx_key` (`object_id`,`user_id`,`op_type`) USING BTREE,
  KEY `idx_user_id` (`user_id`,`op_type`)
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录';

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting` (
  `code` varchar(10) NOT NULL COMMENT '编号',
  `json_content` varchar(500) DEFAULT NULL COMMENT '设置信息',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置信息';

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `user_id` varchar(15) NOT NULL COMMENT '用户ID',
  `nick_name` varchar(20) DEFAULT NULL COMMENT '昵称',
  `email` varchar(150) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(50) DEFAULT NULL COMMENT '密码',
  `sex` tinyint(1) DEFAULT NULL COMMENT '0:女 1:男',
  `person_description` varchar(200) DEFAULT NULL COMMENT '个人描述',
  `join_time` datetime DEFAULT NULL COMMENT '加入时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(15) DEFAULT NULL COMMENT '最后登录IP',
  `last_login_ip_address` varchar(100) DEFAULT NULL COMMENT '最后登录ip地址',
  `total_integral` int(11) DEFAULT NULL COMMENT '积分',
  `current_integral` int(11) DEFAULT NULL COMMENT '当前积分',
  `status` tinyint(4) DEFAULT NULL COMMENT '0:禁用 1:正常',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `key_email` (`email`),
  UNIQUE KEY `key_nick_name` (`nick_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息';

-- ----------------------------
-- Table structure for user_integral_record
-- ----------------------------
DROP TABLE IF EXISTS `user_integral_record`;
CREATE TABLE `user_integral_record` (
  `record_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` varchar(15) DEFAULT NULL COMMENT '用户ID',
  `oper_type` tinyint(4) DEFAULT NULL COMMENT '操作类型',
  `integral` int(11) DEFAULT NULL COMMENT '积分',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10045 DEFAULT CHARSET=utf8mb4 COMMENT='用户积分记录表';

-- ----------------------------
-- Table structure for user_message
-- ----------------------------
DROP TABLE IF EXISTS `user_message`;
CREATE TABLE `user_message` (
  `message_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `received_user_id` varchar(15) DEFAULT NULL COMMENT '接收人用户ID',
  `article_id` varchar(15) DEFAULT NULL COMMENT '文章ID',
  `article_title` varchar(150) DEFAULT NULL COMMENT '文章标题',
  `comment_id` int(11) DEFAULT NULL COMMENT '评论ID',
  `send_user_id` varchar(15) DEFAULT NULL COMMENT '发送人用户ID',
  `send_nick_name` varchar(20) DEFAULT NULL COMMENT '发送人昵称',
  `message_type` tinyint(4) DEFAULT NULL COMMENT '0:系统消息 1:评论 2:文章点赞  3:评论点赞 4:附件下载',
  `message_content` varchar(1000) DEFAULT NULL COMMENT '消息内容',
  `status` tinyint(4) DEFAULT NULL COMMENT '1:未读 2:已读',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`message_id`),
  UNIQUE KEY `idx_key` (`article_id`,`comment_id`,`send_user_id`,`message_type`) USING BTREE,
  KEY `idx_received_user_id` (`received_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`message_type`)
) ENGINE=InnoDB AUTO_INCREMENT=10019 DEFAULT CHARSET=utf8mb4 COMMENT='用户消息';

INSERT INTO `sys_setting` VALUES ('audit', '{\"commentAudit\":false,\"postAudit\":false}');
INSERT INTO `sys_setting` VALUES ('comment', '{\"commentDayCountThreshold\":50,\"commentIntegral\":1,\"commentOpen\":true}');
INSERT INTO `sys_setting` VALUES ('email', '{\"emailContent\":\"你好，您的邮箱验证码是：%s，15分钟有效\",\"emailTitle\":\"邮箱验证码--EasyBBS\"}');
INSERT INTO `sys_setting` VALUES ('like', '{\"likeDayCountThreshold\":50}');
INSERT INTO `sys_setting` VALUES ('post', '{\"attachmentSize\":1,\"dayImageUploadCount\":50,\"postDayCountThreshold\":50,\"postIntegral\":1}');
INSERT INTO `sys_setting` VALUES ('register', '{\"registerWelcomInfo\":\"社区欢迎你，以后的日子里，有老罗陪伴你一起学编程\"}');
