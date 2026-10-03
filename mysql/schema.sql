SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

-- ---------------------------------------------------------------------
-- 五金店 · 工具配件与开单销售
-- 库名 hardware_shop；application.yml 里 ddl-auto: none，表结构全部由本文件在 MySQL 镜像构建期导入。
-- 注意：mysql 容器是 UTC，时间戳一律写 UTC_TIMESTAMP() + INTERVAL 8 HOUR（北京时间）。

CREATE DATABASE IF NOT EXISTS hardware_shop
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE hardware_shop;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS after_sale;
DROP TABLE IF EXISTS sales_order;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS product;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------- 商品 ----------------
CREATE TABLE product (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku        VARCHAR(32) NOT NULL UNIQUE COMMENT '货号',
    name       VARCHAR(128) NOT NULL COMMENT '品名',
    category   VARCHAR(16)  NOT NULL COMMENT '类别：工具/紧固件/电工/建材',
    price      DECIMAL(10, 2) NOT NULL COMMENT '单价',
    stock      INT          NOT NULL COMMENT '库存',
    status     VARCHAR(16)  NOT NULL COMMENT '在售/停用',
    created_at DATETIME     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 客户 ----------------
CREATE TABLE customer (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(64) NOT NULL COMMENT '客户名',
    phone        VARCHAR(24) NOT NULL UNIQUE COMMENT '手机号',
    member_level VARCHAR(16) NOT NULL COMMENT '等级：普通/批发',
    created_at   DATETIME    COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 开单 ----------------
CREATE TABLE sales_order (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT       COMMENT '客户 id',
    step       VARCHAR(16)  NOT NULL COMMENT '步骤：选客/选型/确认/完成',
    total      DECIMAL(12, 2) NOT NULL COMMENT '总价（自动计算）',
    status     VARCHAR(16)  NOT NULL COMMENT '草稿/已结算/已作废',
    created_at DATETIME     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 开单明细 ----------------
CREATE TABLE order_item (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT NOT NULL COMMENT '所属开单 id',
    product_id BIGINT NOT NULL COMMENT '商品 id',
    qty        INT    NOT NULL COMMENT '数量',
    created_at DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 售后 ----------------
CREATE TABLE after_sale (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT       COMMENT '关联开单 id',
    reason     VARCHAR(256) COMMENT '原因',
    handle     VARCHAR(16)  NOT NULL COMMENT '处理：维修/退换/作废',
    status     VARCHAR(16)  NOT NULL COMMENT '待处理/已处理',
    created_at DATETIME     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- 种子数据
-- =====================================================================

INSERT INTO product (id, sku, name, category, price, stock, status, created_at) VALUES
(1, 'HW-1001', '世达10mm活动扳手',          '工具',   28.50, 120, '在售', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 60 DAY),
(2, 'HW-1002', '博世电钻GBM320',            '电工',  199.00,  40, '在售', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 60 DAY),
(3, 'HW-1003', 'M8不锈钢内六角螺栓(100颗)',  '紧固件', 15.00, 300, '在售', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 60 DAY),
(4, 'HW-1004', '联塑PVC排水管DN50',         '建材',    9.90,   0, '在售', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 60 DAY),
(5, 'HW-1005', '史丹利羊角锤',              '工具',   45.00,  60, '停用', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 50 DAY),
(6, 'HW-1006', '公牛五孔插座',              '电工',   12.50, 150, '在售', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 50 DAY),
(7, 'HW-1007', '膨胀螺丝M10(50套)',         '紧固件', 22.00,  80, '在售', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 40 DAY),
(8, 'HW-1008', '瓷砖粘结剂20kg',            '建材',   35.00,  50, '在售', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 40 DAY);

INSERT INTO customer (id, name, phone, member_level, created_at) VALUES
(1, '王建斌',     '13810002001', '普通', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 55 DAY),
(2, '李红建材批发', '13920003002', '批发', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 55 DAY),
(3, '陈晓',       '13730004003', '普通', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 50 DAY),
(4, '赵立五金',    '13640005004', '批发', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 50 DAY),
(5, '孙悦',       '13550006005', '普通', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 45 DAY);

-- 开单：含 已结算(含明细) / 草稿(异常态) / 已作废(异常态)
INSERT INTO sales_order (id, customer_id, step, total, status, created_at) VALUES
(1, 1, '完成', 41.00, '已结算', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 20 DAY),
(2, 2, '完成', 314.10, '已结算', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 18 DAY),
(3, 3, '选型', 0.00,  '草稿',  UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 3 DAY),
(4, 4, '选客', 0.00,  '已作废', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 12 DAY);

INSERT INTO order_item (id, order_id, product_id, qty, created_at) VALUES
(1, 1, 1, 1, UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 20 DAY),
(2, 1, 6, 1, UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 20 DAY),
(3, 2, 3, 10, UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 18 DAY),
(4, 2, 2, 1,  UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 18 DAY),
(5, 3, 8, 1,  UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 3 DAY);

-- 售后：含 待处理(退换,可点处理回滚库存) / 已处理 / 待处理(作废)
INSERT INTO after_sale (id, order_id, reason, handle, status, created_at) VALUES
(1, 1, '扳手包装破损要求退换',   '退换', '待处理', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 6 DAY),
(2, 2, '电钻功率达不到标注值',   '维修', '已处理', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 10 DAY),
(3, 1, '发错型号，申请作废',     '作废', '待处理', UTC_TIMESTAMP() + INTERVAL 8 HOUR - INTERVAL 2 DAY);
