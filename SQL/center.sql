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
    `password` varchar(255),
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
    `name` varchar(255) DEFAULT NULL,
    `ip` varchar(255) DEFAULT NULL,
    `port` int DEFAULT NULL,
    `password` varchar(255),
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
    `application_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `agent_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `file_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `folder_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `dest_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 83 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for download_task
-- ----------------------------
DROP TABLE IF EXISTS `download_task`;

CREATE TABLE `download_task` (
    `uid` int NOT NULL AUTO_INCREMENT,
    `application_id` varchar(255) DEFAULT NULL,
    `output_id` int DEFAULT NULL,
    `download_time` timestamp NULL DEFAULT NULL,
    `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 77 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for file
-- ----------------------------
DROP TABLE IF EXISTS `file`;

CREATE TABLE `file` (
    `uid` varchar(255) NOT NULL,
    `agent_id` varchar(255) NOT NULL,
    `folder_id` varchar(255) DEFAULT NULL,
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
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for folder
-- ----------------------------
DROP TABLE IF EXISTS `folder`;

CREATE TABLE `folder` (
    `uid` varchar(255) NOT NULL,
    `agent_id` varchar(255) NOT NULL,
    `parent_id` varchar(255) DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    `create_date` timestamp NULL DEFAULT NULL,
    `last_update` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for input
-- ----------------------------
DROP TABLE IF EXISTS `input`;

CREATE TABLE `input` (
    `uid` varchar(255) NOT NULL,
    `application_id` varchar(255) NOT NULL,
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
    `application_id` varchar(255) DEFAULT NULL,
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
    `file_id` varchar(255) DEFAULT NULL,
    `agent_id` varchar(255) DEFAULT NULL,
    `application_id` varchar(255) DEFAULT NULL,
    `attribute` JSON DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for outside_database
-- ----------------------------
DROP TABLE IF EXISTS `outside_database`;

CREATE TABLE `outside_database` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `agent_id` varchar(255) DEFAULT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `type` enum('mysql') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `connection` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `username` varchar(255) DEFAULT NULL,
    `password` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

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
    `application_id` varchar(255) DEFAULT NULL,
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
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;

CREATE TABLE `task` (
    `uid` int NOT NULL AUTO_INCREMENT,
    `file_id` varchar(255) DEFAULT NULL,
    `agent_id` varchar(255) DEFAULT NULL,
    `application_id` varchar(255) DEFAULT NULL,
    `output_id` varchar(255) DEFAULT NULL,
    `download_time` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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

-- ----------------------------
-- Table structure for fl_output
-- ----------------------------
DROP TABLE IF EXISTS `fl_output`;

CREATE TABLE `fl_output` (
    `uid` bigint NOT NULL AUTO_INCREMENT,
    `hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `upload_date` timestamp NULL DEFAULT NULL,
    `application_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `expired_time` timestamp NULL DEFAULT NULL,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 83 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `notification`;

CREATE TABLE `notification` (
    `uid` BIGINT NOT NULL AUTO_INCREMENT,
    `appID` varchar(255) DEFAULT NULL,
    `title` varchar(255) DEFAULT NULL,
    `content` TEXT DEFAULT NULL,
    `time` TIMESTAMP DEFAULT NULL,
    `hasRead` BOOLEAN DEFAULT NULL,
    `taskID` varchar(255) DEFAULT NULL,
    `code` int NULL DEFAULT NULL,
    `type` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for keycloak
-- ----------------------------
DROP TABLE IF EXISTS `keycloak`;
CREATE TABLE `keycloak` (
                            `authentication_id` varchar(255) NOT NULL,
                            `server_url` varchar(255) DEFAULT NULL,
                            `realm` varchar(255) DEFAULT NULL,
                            `client_id` varchar(255) DEFAULT NULL,
                            `client_secret` varchar(255) DEFAULT NULL,
                            PRIMARY KEY (`authentication_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
-- ----------------------------
-- Table structure for keycloak_credentials
-- ----------------------------
DROP TABLE IF EXISTS `keycloak_credentials`;
CREATE TABLE `keycloak_credentials` (
                                        `target_id` varchar(255) NOT NULL,
                                        `public_key` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                                        `expired_time` timestamp NULL DEFAULT NULL,
                                        PRIMARY KEY (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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

CREATE TABLE `data_meta` (
                             `resource_uri` varchar(512) NOT NULL COMMENT '数据资源标识符',
                             `owner_party_id` varchar(64) NOT NULL COMMENT '数据持有者 Id',
                             `parents` varchar(1024) COMMENT '祖先 resource uris',
                             `signature` text NOT NULL COMMENT '以上从字段 resource_uri 开始到 parents 直接拼接一起后进行签名',
                             `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (`resource_uri`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `data_key` (
                            `resource_uri` varchar(512) NOT NULL COMMENT '资源标识符',
                            `encrypted_data_key` varchar(128) NOT NULL COMMENT '加密后的数据密钥',
                            `iv` varchar(128) NOT NULL COMMENT 'initialization vector',
                            `tag` varchar(128) NOT NULL COMMENT 'tag',
                            `aad` varchar(512) NOT NULL COMMENT 'additional authenticated data',
                            `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            PRIMARY KEY (`resource_uri`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `rules` (
                         `rule_id` varchar(64) NOT NULL COMMENT '授权规则id',
                         `resource_uri` varchar(512) NOT NULL COMMENT '资源标识符',
                         `scope` varchar(64) NOT NULL COMMENT '授权范围',
                         `grantee_party_ids` varchar(1024) NOT NULL COMMENT '被授权机构ID列表，用英文逗号分割，例如：xx1,xx2',
                         `columns` text NOT NULL COMMENT '被授权的特征列，用英文逗号分割，例如：feature1,feature2',
                         `op_constrants` text COMMENT '算子授权描述列表的 json 形式',
                         `global_constrants` text COMMENT '全局授权描述列表的 json 形式',
                         `signature` text NOT NULL COMMENT '以上从字段 rule_id 开始到 global_constrants 直接拼接一起后进行签名',
                         `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `gmt_delete` datetime DEFAULT NULL COMMENT '删除时间',
                         `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否被删除',
                         PRIMARY KEY (`rule_id`),
                         FOREIGN KEY (`resource_uri`) REFERENCES data_meta(`resource_uri`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for jwt_metadata
-- ----------------------------
DROP TABLE IF EXISTS `jwt_metadata`;
CREATE TABLE `jwt_metadata` (
                                `uid` varchar(255) NOT NULL,
                                `agent_uid` varchar(255) DEFAULT NULL,
                                `issued_time` datetime DEFAULT NULL,
                                `expires_time` datetime DEFAULT NULL,
                                `revoked` tinyint(1) DEFAULT NULL,
                                PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;