CREATE DATABASE IF NOT EXISTS `agent`;

USE `agent`;

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for agent
-- ----------------------------
DROP TABLE IF EXISTS `agent`;

CREATE TABLE `agent` (
    `uid` varchar(255) NOT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `port` int DEFAULT NULL,
    `password` varchar(255),
    `last_updated` timestamp NULL DEFAULT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for application
-- ----------------------------
DROP TABLE IF EXISTS `application`;

CREATE TABLE `application` (
    `uid` varchar(255) NOT NULL,
    `center_id` varchar(255) NOT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `password` varchar(255),
    `last_updated` timestamp NULL DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    `attribute` JSON DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- ----------------------------
-- Table structure for center
-- ----------------------------
DROP TABLE IF EXISTS `center`;

CREATE TABLE `center` (
    `uid` varchar(255) NOT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `port` bigint DEFAULT NULL,
    `password` varchar(255),
    `last_updated` timestamp NULL DEFAULT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for file
-- ----------------------------
DROP TABLE IF EXISTS `file`;

CREATE TABLE `file` (
    `uid` varchar(255) NOT NULL,
    `agent_id` varchar(255) NOT NULL,
    `folder_id` varchar(255) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `create_date` timestamp NULL DEFAULT NULL,
    `last_update` timestamp NULL DEFAULT NULL,
    `attribute` JSON DEFAULT NULL,
    `size` bigint DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `hash` varchar(255) DEFAULT NULL,
    `example` varchar(255) DEFAULT NULL,
    `type` varchar(255) DEFAULT NULL,
    `judge_time` timestamp NULL DEFAULT NULL,
    `judge_type` varchar(255) NULL DEFAULT NULL,
    `judge_district` varchar(255) NULL DEFAULT NULL,
    `judge_cause` varchar(255) NULL DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for folder
-- parent_id作为外健指向自己的uid，表示父文件夹id
-- ----------------------------
DROP TABLE IF EXISTS `folder`;

CREATE TABLE `folder` (
    `uid` varchar(255) NOT NULL,
    `agent_id` varchar(255) NOT NULL,
    `parent_id` varchar(255) NOT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `create_date` timestamp NULL DEFAULT NULL,
    `last_update` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpc
-- ----------------------------
DROP TABLE IF EXISTS `mpc`;

CREATE TABLE `mpc` (
    `uid` varchar(32) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `compile_parameters` json DEFAULT NULL,
    `runtime_parameters` json DEFAULT NULL,
    `center_id` varchar(255) DEFAULT NULL,
    `path` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for mpcTask
-- ----------------------------
DROP TABLE IF EXISTS `mpcTask`;

CREATE TABLE `mpcTask` (
    `uid` varchar(32) NOT NULL,
    `application_id` varchar(255) DEFAULT NULL,
    `center_id` varchar(255) DEFAULT NULL,
    `mpc_id` varchar(32) DEFAULT NULL,
    `compile_parameters` json DEFAULT NULL,
    `runtime_parameters` json DEFAULT NULL,
    `N` int DEFAULT NULL,
    `part` int DEFAULT NULL,
    `host` varchar(255) DEFAULT NULL,
    `port` int DEFAULT NULL,
    `data_id` varchar(255) DEFAULT NULL,
    `mpc_name` varchar(255) DEFAULT NULL,
    `task_type` enum(
        'GARNET_PSI',
        'GARNET_MPC',
        'GARNET_INFERENCE'
    ) DEFAULT NULL,
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
    `center_id` varchar(255) DEFAULT NULL,
    `agent_id` varchar(255) DEFAULT NULL,
    `part` int DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB AUTO_INCREMENT = 75 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for outside_database
-- ----------------------------
DROP TABLE IF EXISTS `outside_database`;

CREATE TABLE `outside_database` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` varchar(255) DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    `type` enum('mysql') DEFAULT NULL,
    `connection` varchar(255) DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    `username` varchar(255) DEFAULT NULL,
    `password` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB AUTO_INCREMENT = 29 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for outside_database_table
-- ----------------------------
DROP TABLE IF EXISTS `outside_database_table`;

CREATE TABLE `outside_database_table` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` varchar(255) DEFAULT NULL,
    `outside_database_id` bigint DEFAULT NULL,
    `name` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `schema_example` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `example` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`)
) ENGINE = InnoDB AUTO_INCREMENT = 48 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for rule
-- ----------------------------
DROP TABLE IF EXISTS `rule`;

CREATE TABLE `rule` (
    `uid` varchar(255) NOT NULL,
    `expression` varchar(1023) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for file_rule
-- ----------------------------
DROP TABLE IF EXISTS `file_rule`;

CREATE TABLE `file_rule` (
    `uid` varchar(255) NOT NULL,
    `file_id` varchar(255) NOT NULL,
    `rule_id` varchar(255) NOT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for visibility
-- ----------------------------
DROP TABLE IF EXISTS `visibility`;

CREATE TABLE `visibility` (
    `uid` varchar(255) NOT NULL,
    `expression` varchar(1023) NOT NULL,
    `description` varchar(255) NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for folder_visibility
-- ----------------------------
DROP TABLE IF EXISTS `folder_visibility`;

CREATE TABLE `folder_visibility` (
    `uid` varchar(255) NOT NULL,
    `folder_id` varchar(255) NOT NULL,
    `visibility_id` varchar(255) NOT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

SELECT r.expression
FROM file_rule fr
    JOIN rule r ON fr.rule_id = r.uid
WHERE
    fr.file_id = 'DAVEX-C1-GXX2-F1'

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for keys_storage
-- ----------------------------
DROP TABLE IF EXISTS `keys_storage`;
CREATE TABLE keys_storage (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              owner_name VARCHAR(255) NOT NULL, -- 唯一ID
                              public_key TEXT NOT NULL       -- 公钥
);