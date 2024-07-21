/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : localhost:3306
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 14/07/2024 14:37:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for agent
-- ----------------------------
DROP TABLE IF EXISTS `agent`;
CREATE TABLE `agent` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `port` int DEFAULT NULL,
  `crt` BLOB  DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for application
-- ----------------------------
DROP TABLE IF EXISTS `application`;
CREATE TABLE `application` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `center_id` int NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `crt` BLOB DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uid`,`center_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for application_group
-- ----------------------------
DROP TABLE IF EXISTS `application_group`;
CREATE TABLE `application_group` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `agent_id` int NOT NULL,
  `center_id` int DEFAULT NULL,
  `application_id` int DEFAULT NULL,
  `group_id` int DEFAULT NULL,
  PRIMARY KEY (`uid`,`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for center
-- ----------------------------
DROP TABLE IF EXISTS `center`;
CREATE TABLE `center` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `port` int DEFAULT NULL,
  `crt` BLOB DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for file
-- ----------------------------
DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `agent_id` int NOT NULL,
  `folder_id` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT NULL,
  `last_update` timestamp NULL DEFAULT NULL,
  `tag` varchar(255) DEFAULT NULL,
  `size` int DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `expired_time` timestamp NULL DEFAULT NULL,
  `hash` varchar(255) DEFAULT NULL,
  `example` varchar(255) DEFAULT NULL,
  `type` enum('文件流','数据库') DEFAULT NULL,
  PRIMARY KEY (`uid`,`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for file_rule
-- ----------------------------
DROP TABLE IF EXISTS `file_rule`;
CREATE TABLE `file_rule` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `agent_id` int NOT NULL,
  `file_id` int DEFAULT NULL,
  `rule_id` int DEFAULT NULL,
  PRIMARY KEY (`uid`,`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for folder
-- ----------------------------
DROP TABLE IF EXISTS `folder`;
CREATE TABLE `folder` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `agent_id` int NOT NULL,
  `parent_id` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT NULL,
  `last_update` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`uid`,`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for folder_visibility
-- ----------------------------
DROP TABLE IF EXISTS `folder_visibility`;
CREATE TABLE `folder_visibility` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `agent_id` int NOT NULL,
  `folder_id` int DEFAULT NULL,
  `group_id` int DEFAULT NULL,
  PRIMARY KEY (`uid`,`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for group
-- ----------------------------
DROP TABLE IF EXISTS `group`;
CREATE TABLE `group` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `agent_id` int NOT NULL,
  `center_id` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uid`,`agent_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpc
-- ----------------------------
DROP TABLE IF EXISTS `mpc`;
CREATE TABLE `mpc` (
  `uid` int NOT NULL,
  `center_id` int DEFAULT NULL,
  `path` int DEFAULT NULL,
  `status` int DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpcTask
-- ----------------------------
DROP TABLE IF EXISTS `mpcTask`;
CREATE TABLE `mpcTask` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `application_id` int DEFAULT NULL,
  `center_id` int DEFAULT NULL,
  `mpc_id` int DEFAULT NULL,
  `parameter` varchar(255) DEFAULT NULL,
  `pn` int DEFAULT NULL,
  `host` varchar(255) DEFAULT NULL,
  `port` int DEFAULT NULL,
  `data` int DEFAULT NULL,
  `protocol` enum('mpl','psi') DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpcTask_agent
-- ----------------------------
DROP TABLE IF EXISTS `mpcTask_agent`;
CREATE TABLE `mpcTask_agent` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `mpcTask_id` int DEFAULT NULL,
  `agent_id` int DEFAULT NULL,
  `part` int DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for rule
-- ----------------------------
DROP TABLE IF EXISTS `rule`;
CREATE TABLE `rule` (
  `uid` int NOT NULL AUTO_INCREMENT,
  `agent_id` int NOT NULL,
  `group_id` int DEFAULT NULL,
  `allowed_method` enum('psi','pir','direct','mpc') DEFAULT NULL,
  PRIMARY KEY (`uid`,`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
