-- PDHD Focus 系统数据库表结构

-- 用户表
CREATE TABLE `user`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username`   VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password`   VARCHAR(100) NOT NULL COMMENT '密码',
    `is_delete`  TINYINT DEFAULT 0 COMMENT '逻辑删除(0:未删除,1:已删除)',
    `created_at` DATETIME COMMENT '创建时间',
    `updated_at` DATETIME COMMENT '更新时间'
) COMMENT '用户表';

-- 目标表
CREATE TABLE `goal`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '目标ID',
    `title`      VARCHAR(255) NOT NULL COMMENT '目标标题',
    `color`      VARCHAR(50) COMMENT '颜色标识',
    `status`     TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    `progress`   INT     DEFAULT 0 COMMENT '进度(0-100)',
    `user_id`    BIGINT       NOT NULL COMMENT '所属用户ID',
    `is_delete`  TINYINT DEFAULT 0 COMMENT '逻辑删除(0:未删除,1:已删除)',
    `created_at` DATETIME COMMENT '创建时间',
    `updated_at` DATETIME COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`)
) COMMENT '目标表';

-- 计划表
CREATE TABLE `schedule`
(
    `id`                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '计划ID',
    `title`              VARCHAR(255) NOT NULL COMMENT '计划标题',
    `content`            TEXT COMMENT '计划内容',
    `type`               VARCHAR(50) COMMENT '类型(WORK,INVEST,STUDY,LIFE)',
    `zone`               VARCHAR(50) COMMENT '难度分区(COMFORT,STRETCH,DIFFICULTY)',
    `goal_id`            BIGINT COMMENT '关联目标ID',
    `start_time`         TIME COMMENT '循环计划开始时间(时分秒)',
    `end_time`           TIME COMMENT '循环计划结束时间(时分秒)',
    `start_date_time`    DATETIME COMMENT '单次计划开始时间',
    `end_date_time`      DATETIME COMMENT '单次计划结束时间',
    `repeat_rule_type`   VARCHAR(50) DEFAULT 'NONE' COMMENT '重复规则类型(NONE,CUSTOM)',
    `repeat_rule_config` TEXT COMMENT '重复规则配置(JSON)',
    `group_id`           VARCHAR(100) COMMENT '重复任务组ID',
    `user_id`            BIGINT       NOT NULL COMMENT '所属用户ID',
    `is_delete`          TINYINT     DEFAULT 0 COMMENT '逻辑删除(0:未删除,1:已删除)',
    `created_at`         DATETIME COMMENT '创建时间',
    `updated_at`         DATETIME COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_goal_id` (`goal_id`),
    INDEX `idx_start_date_time` (`start_date_time`),
    INDEX `idx_end_date_time` (`end_date_time`)
) COMMENT '计划表';

-- 实际事项表
CREATE TABLE `activity`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '实际事项ID',
    `schedule_id`     BIGINT COMMENT '关联计划ID',
    `title`           VARCHAR(255) NOT NULL COMMENT '实际事项标题',
    `content`         TEXT COMMENT '实际事项内容',
    `type`            VARCHAR(50) COMMENT '类型',
    `zone`            VARCHAR(50) COMMENT '难度分区',
    `goal_id`         BIGINT COMMENT '关联目标ID',
    `start_date_time` DATETIME     NOT NULL COMMENT '开始时间',
    `end_date_time`   DATETIME     NOT NULL COMMENT '结束时间',
    `user_id`         BIGINT       NOT NULL COMMENT '所属用户ID',
    `is_delete`       TINYINT DEFAULT 0 COMMENT '逻辑删除(0:未删除,1:已删除)',
    `created_at`      DATETIME COMMENT '创建时间',
    `updated_at`      DATETIME COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_schedule_id` (`schedule_id`),
    INDEX `idx_goal_id` (`goal_id`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_end_time` (`end_time`)
) COMMENT '实际事项表';