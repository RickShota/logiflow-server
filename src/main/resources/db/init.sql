CREATE DATABASE IF NOT EXISTS `logiflow_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `logiflow_db`;

-- 1. 系统用户表 (sys_user)
CREATE TABLE `sys_user` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
                            `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密后的密码',
                            `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
                            `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删，1-已删',
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`) COMMENT '账号唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 系统角色表 (sys_role)
CREATE TABLE `sys_role` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                            `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码（如 ROLE_ADMIN, ROLE_STAFF）',
                            `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
                            `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 3. 权限/菜单表 (sys_permission)
CREATE TABLE `sys_permission` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
                                  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID（0为顶级菜单）',
                                  `perm_name` VARCHAR(50) NOT NULL COMMENT '权限/菜单名称',
                                  `perm_code` VARCHAR(100) NOT NULL COMMENT '权限标识符（如 sys:order:add，对接 @PreAuthorize）',
                                  `route_path` VARCHAR(100) DEFAULT NULL COMMENT '前端Vue路由路径',
                                  `type` TINYINT NOT NULL COMMENT '类型：1-菜单，2-按钮/接口权限',
                                  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_perm_code` (`perm_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- 4. 用户-角色关联表 (sys_user_role)
CREATE TABLE `sys_user_role` (
                                 `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                 `role_id` BIGINT NOT NULL COMMENT '角色ID',
                                 PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 5. 角色-权限关联表 (sys_role_permission)
CREATE TABLE `sys_role_permission` (
                                       `role_id` BIGINT NOT NULL COMMENT '角色ID',
                                       `permission_id` BIGINT NOT NULL COMMENT '权限ID',
                                       PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 6. 物资信息表 (biz_material)
CREATE TABLE `biz_material` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物资ID',
                                `material_code` VARCHAR(50) NOT NULL COMMENT '物资编码',
                                `material_name` VARCHAR(100) NOT NULL COMMENT '物资名称',
                                `category` VARCHAR(50) DEFAULT NULL COMMENT '物资分类',
                                `stock` INT NOT NULL DEFAULT 0 COMMENT '当前可用库存',
                                `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（防并发超卖）',
                                `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_material_code` (`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资库存表';

-- 7. 物资申领订单表 (biz_order)
CREATE TABLE `biz_order` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单主键',
                             `order_no` VARCHAR(64) NOT NULL COMMENT '全局唯一订单流水号（MQ幂等防重唯一索引！）',
                             `user_id` BIGINT NOT NULL COMMENT '申领人ID',
                             `material_id` BIGINT NOT NULL COMMENT '申领物资ID',
                             `quantity` INT NOT NULL COMMENT '申领数量',
                             `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-排队处理中，1-已完成(已扣库存)，2-库存不足失败，3-超时关闭',
                             `remark` VARCHAR(255) DEFAULT NULL COMMENT '申领备注',
                             `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_order_no` (`order_no`) COMMENT '核心：防止RabbitMQ重复消费生成重复订单/重复扣库存',
                             KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资申领订单表';