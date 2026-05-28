-- ============================================================
-- ACG Community (漫化) Database Schema
-- MySQL 8.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS `acg_community`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `acg_community`;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE `t_user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `username`   VARCHAR(50)  NOT NULL,
    `password`   VARCHAR(255) NOT NULL,
    `nickname`   VARCHAR(50)  DEFAULT NULL,
    `phone`      VARCHAR(20)  DEFAULT NULL,
    `avatar_url` VARCHAR(500) DEFAULT NULL,
    `role`       TINYINT      DEFAULT 0 COMMENT '0=user,1=makeup_artist,2=merchant,3=admin,4=super_admin',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ============================================================
-- 2. 商品分类表
-- ============================================================
CREATE TABLE `t_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(50)  NOT NULL,
    `description` VARCHAR(200) DEFAULT NULL,
    `sort_order`  INT          DEFAULT 0,
    `is_active`   TINYINT      DEFAULT 1,
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- ============================================================
-- 3. 商品表
-- ============================================================
CREATE TABLE `t_product` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(200)  NOT NULL,
    `description` TEXT          DEFAULT NULL,
    `price`       DECIMAL(10,2) NOT NULL,
    `stock`       INT           DEFAULT 0,
    `images`      JSON          DEFAULT NULL,
    `category_id` BIGINT        DEFAULT NULL,
    `merchant_id` BIGINT        DEFAULT NULL,
    `status`      TINYINT       DEFAULT 0 COMMENT '0=active,1=inactive',
    `created_at`  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT       DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `t_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';

-- ============================================================
-- 4. 订单表
-- ============================================================
CREATE TABLE `t_order` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT        NOT NULL,
    `total_amount`    DECIMAL(10,2) NOT NULL,
    `status`          TINYINT       DEFAULT 0 COMMENT '0=pending,1=paid,2=shipped,3=completed,4=cancelled',
    `receiver_name`   VARCHAR(50)   DEFAULT NULL,
    `receiver_phone`  VARCHAR(20)   DEFAULT NULL,
    `receiver_address` VARCHAR(500) DEFAULT NULL,
    `created_at`      DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT       DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- ============================================================
-- 5. 订单明细表
-- ============================================================
CREATE TABLE `t_order_item` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `order_id`     BIGINT        NOT NULL,
    `product_id`   BIGINT        NOT NULL,
    `product_name` VARCHAR(200)  DEFAULT NULL,
    `product_image` VARCHAR(500) DEFAULT NULL,
    `quantity`     INT           NOT NULL,
    `price`        DECIMAL(10,2) NOT NULL,
    `subtotal`     DECIMAL(10,2) NOT NULL,
    `created_at`   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT       DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    CONSTRAINT `fk_order_item_order`   FOREIGN KEY (`order_id`)   REFERENCES `t_order`   (`id`),
    CONSTRAINT `fk_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `t_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单明细表';

-- ============================================================
-- 6. 化妆服务表
-- ============================================================
CREATE TABLE `t_makeup_service` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `artist_id`   BIGINT        NOT NULL,
    `name`        VARCHAR(200)  NOT NULL,
    `description` TEXT          DEFAULT NULL,
    `price`       DECIMAL(10,2) NOT NULL,
    `duration`    INT           DEFAULT 60 COMMENT '分钟',
    `images`      JSON          DEFAULT NULL,
    `status`      TINYINT       DEFAULT 0,
    `created_at`  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT       DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_artist_id` (`artist_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='化妆服务表';

-- ============================================================
-- 7. 化妆师可用时间段表
-- ============================================================
CREATE TABLE `t_artist_available_slot` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `artist_id`  BIGINT   NOT NULL,
    `service_id` BIGINT   NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time`   DATETIME NOT NULL,
    `booked`     TINYINT  DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT  DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_booked` (`booked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='化妆师可用时间段表';

-- ============================================================
-- 8. 化妆预约表
-- ============================================================
CREATE TABLE `t_makeup_booking` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT   NOT NULL,
    `service_id`   BIGINT   NOT NULL,
    `slot_id`      BIGINT   DEFAULT NULL,
    `status`       TINYINT  DEFAULT 0 COMMENT '0=pending,1=confirmed,2=completed,3=cancelled',
    `notes`        TEXT     DEFAULT NULL,
    `contact_name` VARCHAR(50)  DEFAULT NULL,
    `contact_phone` VARCHAR(20) DEFAULT NULL,
    `created_at`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT  DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id`    (`user_id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_slot_id`    (`slot_id`),
    CONSTRAINT `fk_booking_service` FOREIGN KEY (`service_id`) REFERENCES `t_makeup_service` (`id`),
    CONSTRAINT `fk_booking_slot`    FOREIGN KEY (`slot_id`)    REFERENCES `t_artist_available_slot` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='化妆预约表';

-- ============================================================
-- 8. 化妆师认证申请表
-- ============================================================
CREATE TABLE `t_makeup_artist_application` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT       NOT NULL,
    `real_name`        VARCHAR(50)  NOT NULL,
    `id_card`          VARCHAR(100) NOT NULL,
    `experience_years` INT          DEFAULT 0,
    `portfolio_images` JSON         DEFAULT NULL,
    `certificates`     JSON         DEFAULT NULL,
    `self_intro`       TEXT         DEFAULT NULL,
    `status`           TINYINT      DEFAULT 0 COMMENT '0=pending,1=approved,2=rejected',
    `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`          TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='化妆师认证申请表';

-- ============================================================
-- 9. 商家认证申请表
-- ============================================================
CREATE TABLE `t_merchant_application` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`        BIGINT       NOT NULL,
    `business_name`  VARCHAR(100) NOT NULL,
    `business_license` VARCHAR(500) DEFAULT NULL,
    `contact_info`   JSON         DEFAULT NULL,
    `status`         TINYINT      DEFAULT 0,
    `created_at`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商家认证申请表';

-- ============================================================
-- 10. 聊天室表
-- ============================================================
CREATE TABLE `t_chat_room` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(200) DEFAULT NULL,
    `participants_json` JSON       NOT NULL,
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='聊天室表';

-- ============================================================
-- 11. 聊天消息表
-- ============================================================
CREATE TABLE `t_message` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `room_id`      BIGINT       NOT NULL,
    `sender_id`    BIGINT       NOT NULL,
    `content`      TEXT         NOT NULL,
    `message_type` VARCHAR(20)  DEFAULT 'text',
    `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='聊天消息表';

-- ============================================================
-- 12. 用户收藏表
-- ============================================================
CREATE TABLE `t_favorite` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL,
    `item_type`  TINYINT  NOT NULL COMMENT '0=product,1=makeup_artist,2=makeup_service',
    `item_id`    BIGINT   NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT  DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_type_item` (`user_id`, `item_type`, `item_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户收藏表';

-- ============================================================
-- 初始数据
-- ============================================================

-- 默认管理员  密码: admin123 (BCrypt)
INSERT INTO `t_user` (`username`, `password`, `nickname`, `role`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 4);

-- 商品分类
INSERT INTO `t_category` (`name`, `description`, `sort_order`) VALUES
    ('Cosplay服饰', 'Cosplay服装、道具服饰等',     1),
    ('二次元手办', '动漫、游戏角色手办模型',         2),
    ('动漫周边',   '徽章、立牌、挂件等正版周边',     3),
    ('假发道具',   'Cosplay专用假发及配套道具',      4),
    ('化妆工具',   'Cosplay化妆、特效妆所需工具材料', 5);

-- 示例商品
INSERT INTO `t_product` (`name`, `description`, `price`, `stock`, `images`, `category_id`, `merchant_id`, `status`) VALUES
    ('初音未来Cosplay套装', '包含上衣、裙子、领带、袖套，均码可调', 299.00, 50,
     '["https://example.com/miku_cos_1.jpg","https://example.com/miku_cos_2.jpg"]',
     1, NULL, 0),
    ('蕾姆手办 Re:Zero', '1/7比例 约230mm PVC涂装完成品', 458.00, 30,
     '["https://example.com/rem_figure_1.jpg"]',
     2, NULL, 0);

-- 示例化妆服务
INSERT INTO `t_makeup_service` (`artist_id`, `name`, `description`, `price`, `duration`, `images`, `status`) VALUES
    (1, 'Cosplay全套特效妆', '含底妆、眼妆、假睫毛、面部彩绘，适合舞台及漫展', 388.00, 120,
     '["https://example.com/makeup_service_1.jpg"]', 0);
