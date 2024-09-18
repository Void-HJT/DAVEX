CREATE DATABASE IF NOT EXISTS `center`;

USE `center`;

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for agent
-- ----------------------------
DROP TABLE IF EXISTS `agent`;

CREATE TABLE `agent` (
    `uid` varchar(255) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `ip` varchar(255) DEFAULT NULL,
    `port` int DEFAULT NULL,
    `crt` blob,
    `last_updated` timestamp NULL DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for application
-- ----------------------------
DROP TABLE IF EXISTS `application`;

CREATE TABLE `application` (
    `uid` varchar(255) NOT NULL,
    `center_id` varchar(255) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `crt` blob,
    `last_updated` timestamp NULL DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    `attrubute` JSON DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for application_group
-- ----------------------------
DROP TABLE IF EXISTS `application_group`;

CREATE TABLE `application_group` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` varchar(255) NOT NULL,
    `center_id` varchar(255) DEFAULT NULL,
    `application_id` varchar(255) DEFAULT NULL,
    `group_id` bigint DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for center
-- ----------------------------
DROP TABLE IF EXISTS `center`;

CREATE TABLE `center` (
    `uid` varchar(255) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `ip` varchar(255) DEFAULT NULL,
    `port` int DEFAULT NULL,
    `crt` blob,
    `last_updated` timestamp NULL DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for comparison_output
-- ----------------------------
DROP TABLE IF EXISTS `comparison_output`;

CREATE TABLE `comparison_output` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `upload_date` timestamp NULL DEFAULT NULL,
    `application_id` int DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `agent_id` int DEFAULT NULL,
    `file_id` int DEFAULT NULL,
    `folder_id` int DEFAULT NULL,
    `dest_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 83 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for download_task
-- ----------------------------
DROP TABLE IF EXISTS `download_task`;

CREATE TABLE `download_task` (
    `uid` int NOT NULL AUTO_INCREMENT,
    `application_id` int DEFAULT NULL,
    `output_id` int DEFAULT NULL,
    `download_time` timestamp NULL DEFAULT NULL,
    `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 77 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for file
-- ----------------------------
DROP TABLE IF EXISTS `file`;

CREATE TABLE `file` (
    `uid` varchar(255) NOT NULL,
    `agent_id` varchar(255) NOT NULL,
    `folder_id` bigint DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    `create_date` timestamp NULL DEFAULT NULL,
    `last_update` timestamp NULL DEFAULT NULL,
    `attrubute` JSON DEFAULT NULL,
    `size` bigint DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `hash` varchar(255) DEFAULT NULL,
    `example` varchar(255) DEFAULT NULL,
    `type` enum('文件流', '数据库') DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for file_rule
-- ----------------------------
DROP TABLE IF EXISTS `file_rule`;

CREATE TABLE `file_rule` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` varchar(255) NOT NULL,
    `file_id` varchar(255) DEFAULT NULL,
    `rule_id` bigint DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for folder
-- ----------------------------
DROP TABLE IF EXISTS `folder`;

CREATE TABLE `folder` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` varchar(255) NOT NULL,
    `parent_id` bigint DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    `create_date` timestamp NULL DEFAULT NULL,
    `last_update` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for folder_visibility
-- ----------------------------
DROP TABLE IF EXISTS `folder_visibility`;

CREATE TABLE `folder_visibility` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` varchar(255) NOT NULL,
    `folder_id` varchar(255) DEFAULT NULL,
    `group_id` bigint DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for group
-- ----------------------------
DROP TABLE IF EXISTS `group`;

CREATE TABLE `group` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` varchar(255) NOT NULL,
    `center_id` varchar(255) DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for input
-- ----------------------------
DROP TABLE IF EXISTS `input`;

CREATE TABLE `input` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `application_id` bigint NOT NULL,
    `path` varchar(255) NOT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB AUTO_INCREMENT = 52 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpc
-- ----------------------------
DROP TABLE IF EXISTS `mpc`;

CREATE TABLE `mpc` (
    `uid` varchar(32) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `compile_parameters` json DEFAULT NULL,
    `runtime_parameters` json DEFAULT NULL,
    `center_id` bigint DEFAULT NULL,
    `path` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpcTask
-- ----------------------------
DROP TABLE IF EXISTS `mpcTask`;

CREATE TABLE `mpcTask` (
    `uid` varchar(32) NOT NULL,
    `application_id` bigint DEFAULT NULL,
    `center_id` bigint DEFAULT NULL,
    `mpc_id` varchar(32) DEFAULT NULL,
    `compile_parameters` json DEFAULT NULL,
    `runtime_parameters` json DEFAULT NULL,
    `N` int DEFAULT NULL,
    `part` int DEFAULT NULL,
    `host` varchar(255) DEFAULT NULL,
    `port` int DEFAULT NULL,
    `data_id` bigint DEFAULT NULL,
    `mpc_name` varchar(255) DEFAULT NULL,
    `task_type` enum('GARNET_PSI', 'GARNET_MPC') DEFAULT NULL,
    `status` varchar(255) DEFAULT NULL,
    `message` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpcTask_agent
-- ----------------------------
DROP TABLE IF EXISTS `mpcTask_agent`;

CREATE TABLE `mpcTask_agent` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `mpcTask_id` varchar(255) DEFAULT NULL,
    `center_id` bigint DEFAULT NULL,
    `agent_id` bigint DEFAULT NULL,
    `part` int DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB AUTO_INCREMENT = 42 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpcTaskOutput
-- ----------------------------
DROP TABLE IF EXISTS `mpcTaskOutput`;

CREATE TABLE `mpcTaskOutput` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `upload_date` timestamp NULL DEFAULT NULL,
    `application_id` int DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for output
-- ----------------------------
DROP TABLE IF EXISTS `output`;

CREATE TABLE `output` (
    `uid` int NOT NULL AUTO_INCREMENT,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `upload_date` timestamp NULL DEFAULT NULL,
    `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `size` int DEFAULT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `file_id` int DEFAULT NULL,
    `agent_id` int DEFAULT NULL,
    `application_id` int DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for outside_database
-- ----------------------------
DROP TABLE IF EXISTS `outside_database`;

CREATE TABLE `outside_database` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` bigint DEFAULT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `type` enum('mysql') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `connection` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for outside_database_table
-- ----------------------------
DROP TABLE IF EXISTS `outside_database_table`;

CREATE TABLE `outside_database_table` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` bigint DEFAULT NULL,
    `outside_database_id` bigint DEFAULT NULL,
    `name` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `schema_example` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `example` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for query_output
-- ----------------------------
DROP TABLE IF EXISTS `query_output`;

CREATE TABLE `query_output` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `upload_date` timestamp NULL DEFAULT NULL,
    `application_id` int DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for rabbitmq_connection
-- ----------------------------
DROP TABLE IF EXISTS `rabbitmq_connection`;

CREATE TABLE `rabbitmq_connection` (
    `center_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `port` int DEFAULT NULL,
    `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `virtual_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`center_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for rule
-- ----------------------------
DROP TABLE IF EXISTS `rule`;

CREATE TABLE `rule` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` varchar(255) NOT NULL,
    `group_id` bigint DEFAULT NULL,
    `allowed_method` enum('psi', 'pir', 'direct', 'mpc') DEFAULT NULL,
    PRIMARY KEY (`uid`, `agent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;

CREATE TABLE `task` (
    `uid` int NOT NULL AUTO_INCREMENT,
    `file_id` int DEFAULT NULL,
    `agent_id` int DEFAULT NULL,
    `application_id` int DEFAULT NULL,
    `output_id` int DEFAULT NULL,
    `download_time` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;