CREATE DATABASE IF NOT EXISTS `center`;

USE `center`;

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for agent
-- ----------------------------
DROP TABLE IF EXISTS `agent`;

CREATE TABLE `agent` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `name` varchar(255) DEFAULT NULL,
    `ip` varchar(255) DEFAULT NULL,
    `port` int DEFAULT NULL,
    `crt` BLOB DEFAULT NULL,
    `last_updated` timestamp NULL DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`),
    UNIQUE KEY `name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for application
-- ----------------------------
DROP TABLE IF EXISTS `application`;

CREATE TABLE `application` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `center_id` BIGINT NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `crt` BLOB DEFAULT NULL,
    `last_updated` timestamp NULL DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`, `center_id`),
    UNIQUE KEY `name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for application_group
-- ----------------------------
DROP TABLE IF EXISTS `application_group`;

CREATE TABLE `application_group` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `agent_id` BIGINT NOT NULL,
    `center_id` BIGINT DEFAULT NULL,
    `application_id` BIGINT DEFAULT NULL,
    `group_id` BIGINT DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for center
-- ----------------------------
DROP TABLE IF EXISTS `center`;

CREATE TABLE `center` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `name` varchar(255) DEFAULT NULL,
    `ip` varchar(255) DEFAULT NULL,
    `port` int DEFAULT NULL,
    `crt` BLOB DEFAULT NULL,
    `last_updated` timestamp NULL DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`),
    UNIQUE KEY `name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for file
-- ----------------------------
DROP TABLE IF EXISTS `file`;

CREATE TABLE `file` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `agent_id` BIGINT NOT NULL,
    `folder_id` BIGINT DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    `create_date` timestamp NULL DEFAULT NULL,
    `last_update` timestamp NULL DEFAULT NULL,
    `tag` varchar(255) DEFAULT NULL,
    `size` BIGINT DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `hash` varchar(255) DEFAULT NULL,
    `example` varchar(255) DEFAULT NULL,
    `type` enum('文件流', '数据库') DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for file_rule
-- ----------------------------
DROP TABLE IF EXISTS `file_rule`;

CREATE TABLE `file_rule` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `agent_id` BIGINT NOT NULL,
    `file_id` BIGINT DEFAULT NULL,
    `rule_id` BIGINT DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for folder
-- ----------------------------
DROP TABLE IF EXISTS `folder`;

CREATE TABLE `folder` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `agent_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    `create_date` timestamp NULL DEFAULT NULL,
    `last_update` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for folder_visibility
-- ----------------------------
DROP TABLE IF EXISTS `folder_visibility`;

CREATE TABLE `folder_visibility` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `agent_id` BIGINT NOT NULL,
    `folder_id` BIGINT DEFAULT NULL,
    `group_id` BIGINT DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for group
-- ----------------------------
DROP TABLE IF EXISTS `group`;

CREATE TABLE `group` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `agent_id` BIGINT NOT NULL,
    `center_id` BIGINT DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`),
    UNIQUE KEY `name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpc
-- ----------------------------
DROP TABLE IF EXISTS `mpc`;

CREATE TABLE `mpc` (
    `uid` VARCHAR(32) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `compile_parameters` JSON DEFAULT NULL,
    `runtime_parameters` JSON DEFAULT NULL,
    `center_id` BIGINT DEFAULT NULL,
    `path` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpcTask
-- ----------------------------
DROP TABLE IF EXISTS `mpcTask`;

CREATE TABLE `mpcTask` (
    `uid` VARCHAR(32) NOT NULL,
    `application_id` BIGINT DEFAULT NULL,
    `center_id` BIGINT DEFAULT NULL,
    `mpc_id` VARCHAR(32) DEFAULT NULL,
    `compile_parameters` JSON DEFAULT NULL,
    `runtime_parameters` JSON DEFAULT NULL,
    `N` int DEFAULT NULL,
    `part` int DEFAULT NULL,
    `host` varchar(255) DEFAULT NULL,
    `port` int DEFAULT NULL,
    `data_id` BIGINT DEFAULT NULL,
    `mpc_name` varchar(255) DEFAULT NULL,
    `task_type` ENUM('GARNET_PSI', 'GARNET_MPC') NULL,
    `status` varchar(255) DEFAULT NULL,
    `message` VARCHAR(255) NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpcTask_agent
-- ----------------------------
DROP TABLE IF EXISTS `mpcTask_agent`;

CREATE TABLE `mpcTask_agent` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `mpcTask_id` varchar(255) NULL,
    `center_id` BIGINT DEFAULT NULL,
    `agent_id` BIGINT DEFAULT NULL,
    `part` int DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for rule
-- ----------------------------
DROP TABLE IF EXISTS `rule`;

CREATE TABLE `rule` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `agent_id` BIGINT NOT NULL,
    `group_id` BIGINT DEFAULT NULL,
    `allowed_method` enum('psi', 'pir', 'direct', 'mpc') DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for input
-- ----------------------------
DROP TABLE IF EXISTS `input`;

CREATE TABLE `input` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `application_id` BIGINT NOT NULL,
    `path` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `mpcTaskOutput`;

CREATE TABLE `mpcTaskOutput` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `upload_date` timestamp NULL DEFAULT NULL,
    `application_id` int NULL DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `output`;

CREATE TABLE `output` (
    `uid` int NOT NULL AUTO_INCREMENT,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `upload_date` timestamp NULL DEFAULT NULL,
    `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `size` int NULL DEFAULT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `file_id` int NULL DEFAULT NULL,
    `agent_id` int NULL DEFAULT NULL,
    `application_id` int NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `comparison_output`;

CREATE TABLE `comparison_output` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `upload_date` timestamp NULL DEFAULT NULL,
    `application_id` int NULL DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `download_task`;

CREATE TABLE `download_task` (
    `uid` int NOT NULL AUTO_INCREMENT,
    `application_id` int NULL DEFAULT NULL,
    `output_id` int NULL DEFAULT NULL,
    `download_time` timestamp NULL DEFAULT NULL,
    `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `query_output`;

CREATE TABLE `query_output` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `upload_date` timestamp NULL DEFAULT NULL,
    `application_id` int NULL DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;