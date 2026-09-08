/*
 Navicat Premium Data Transfer

 Source Server         : klt
 Source Server Type    : MySQL
 Source Server Version : 80033
 Source Host           : localhost:3306
 Source Schema         : supermarket_erp

 Target Server Type    : MySQL
 Target Server Version : 80033
 File Encoding         : 65001

 Date: 07/09/2026 14:18:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oms_cashier_transaction
-- ----------------------------
DROP TABLE IF EXISTS `oms_cashier_transaction`;
CREATE TABLE `oms_cashier_transaction`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `txn_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流水号',
  `cashier_id` bigint NOT NULL COMMENT '收银员ID',
  `shift_id` bigint NULL DEFAULT NULL COMMENT '班次ID',
  `payment_method` tinyint NOT NULL COMMENT '支付方式：1-现金 2-微信 3-支付宝 4-银联 5-储值卡',
  `transaction_type` tinyint NOT NULL COMMENT '交易类型：1-收入 2-退款',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额（正数）',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `payment_id` bigint NULL DEFAULT NULL COMMENT '关联支付明细ID',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `txn_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '交易时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_txn_no`(`txn_no`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_cashier_id`(`cashier_id`) USING BTREE,
  INDEX `idx_payment_method`(`payment_method`) USING BTREE,
  INDEX `idx_transaction_type`(`transaction_type`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_txn_time`(`txn_time`) USING BTREE,
  INDEX `idx_store_paytime`(`store_id`, `txn_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收银流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_cashier_transaction
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `order_type` tinyint NOT NULL DEFAULT 1 COMMENT '订单类型：1-销售 2-退货 3-换货',
  `member_id` bigint NULL DEFAULT NULL COMMENT '会员ID（散客为空）',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总额',
  `discount_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '应付金额',
  `profit_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '利润',
  `change_amount` decimal(10, 2) NULL DEFAULT 0.00,
  `pay_method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式编码',
  `is_combined_pay` tinyint NOT NULL DEFAULT 0 COMMENT '是否组合支付：0-否 1-是',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付 1-已支付 2-部分退款 3-全额退款',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付 1-已完成 2-已取消 3-已退款',
  `total_quantity` int NOT NULL DEFAULT 0 COMMENT '商品总数',
  `operator_id` bigint NULL DEFAULT NULL,
  `cashier_id` bigint NULL DEFAULT NULL,
  `order_time` datetime(0) NULL DEFAULT NULL COMMENT '下单时间',
  `pay_time` datetime(0) NULL DEFAULT NULL COMMENT '支付时间',
  `complete_time` datetime(0) NULL DEFAULT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_order_no`(`tenant_id`, `order_no`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE,
  INDEX `idx_pay_status`(`pay_status`) USING BTREE,
  INDEX `idx_order_status`(`order_status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_cashier_id`(`cashier_id`) USING BTREE,
  INDEX `idx_tenant_store`(`tenant_id`, `store_id`) USING BTREE,
  INDEX `idx_tenant_create`(`tenant_id`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order
-- ----------------------------
INSERT INTO `oms_order` VALUES (17, 1, 1, '202609040937060010001', 1, NULL, 2.50, 0.00, 2.50, 1.20, 0.00, 'wechat', 0, 1, 1, 0, 1, NULL, '2026-09-04 09:37:07', '2026-09-04 09:37:07', NULL, NULL, '2026-09-04 09:37:07', '2026-09-04 09:37:07', NULL, NULL, 0);
INSERT INTO `oms_order` VALUES (21, 1, 1, '202609041026200010001', 1, 1, 8.80, 0.00, 8.80, 3.40, 0.00, 'stored', 0, 1, 1, 0, 1, NULL, '2026-09-04 10:26:20', '2026-09-04 10:26:20', NULL, NULL, '2026-09-04 10:26:20', '2026-09-04 10:26:20', NULL, NULL, 0);

-- ----------------------------
-- Table structure for oms_order_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL,
  `store_id` bigint NULL DEFAULT NULL,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称快照',
  `barcode` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条形码快照',
  `spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格快照',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '单位快照',
  `quantity` decimal(10, 2) NOT NULL COMMENT '数量',
  `sale_price` decimal(10, 2) NOT NULL COMMENT '零售价',
  `discount_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '折后单价',
  `total_price` decimal(10, 2) NOT NULL COMMENT '小计金额',
  `cost_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '成本价',
  `is_weight` tinyint NOT NULL DEFAULT 0 COMMENT '是否称重商品：0-否 1-是',
  `deleted` int NULL DEFAULT 0,
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_item
-- ----------------------------
INSERT INTO `oms_order_item` VALUES (14, NULL, NULL, 17, 29, '晨光中性笔', '6927804081108', '1 支 / 盒', '件', 1.00, 2.50, 0.00, 2.50, 1.30, 0, 0, '2026-09-04 09:37:07', '2026-09-04 09:37:07');
INSERT INTO `oms_order_item` VALUES (18, NULL, NULL, 21, 28, '家用加厚垃圾袋', '6935349200614', '100 只 / 包', '件', 1.00, 8.80, 0.00, 8.80, 5.40, 0, 0, '2026-09-04 10:26:20', '2026-09-04 10:26:20');

-- ----------------------------
-- Table structure for oms_order_payment
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_payment`;
CREATE TABLE `oms_order_payment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL,
  `store_id` bigint NULL DEFAULT NULL,
  `payment_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `payment_method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付方式编码',
  `amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `received_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '实收金额（现金支付）',
  `change_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '找零金额（现金支付）',
  `operator_id` bigint NULL DEFAULT NULL,
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `pay_time` datetime(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_no`(`payment_no`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_payment_method`(`payment_method`) USING BTREE,
  INDEX `idx_operator_id`(`operator_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_pay_time`(`pay_time`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_paytime`(`store_id`, `pay_time`) USING BTREE,
  INDEX `idx_store_method_time`(`store_id`, `payment_method`, `pay_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单支付明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_payment
-- ----------------------------
INSERT INTO `oms_order_payment` VALUES (13, 1, 1, NULL, 17, 'wechat', 2.50, 0.00, 0.00, NULL, NULL, '2026-09-04 09:37:07', '2026-09-04 09:37:07', '2026-09-04 09:37:07', 0);
INSERT INTO `oms_order_payment` VALUES (17, 1, 1, NULL, 21, 'stored', 8.80, 0.00, 0.00, NULL, NULL, '2026-09-04 10:26:20', '2026-09-04 10:26:20', '2026-09-04 10:26:20', 0);

-- ----------------------------
-- Table structure for oms_refund
-- ----------------------------
DROP TABLE IF EXISTS `oms_refund`;
CREATE TABLE `oms_refund`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `refund_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '退款单号',
  `order_id` bigint NOT NULL COMMENT '原订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原订单号',
  `refund_amount` decimal(10, 2) NOT NULL COMMENT '退款金额',
  `refund_method` tinyint NOT NULL COMMENT '退款方式：1-现金 2-原路退回',
  `refund_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '退款原因',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-待退款 1-已退款 2-退款失败',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `refund_time` datetime(0) NULL DEFAULT NULL COMMENT '退款时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_refund_no`(`tenant_id`, `refund_no`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '退款记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_refund
-- ----------------------------

-- ----------------------------
-- Table structure for pms_brand
-- ----------------------------
DROP TABLE IF EXISTS `pms_brand`;
CREATE TABLE `pms_brand`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '品牌名称',
  `logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品牌Logo',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品牌描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '品牌表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_brand
-- ----------------------------
INSERT INTO `pms_brand` VALUES (1, 1, '奥利奥', '', '奥利奥奥利奥奥利奥奥利奥', 1, 0, '2026-09-04 17:36:35', '2026-09-04 17:36:35', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (2, 1, '卫龙', '', '', 1, 0, '2026-09-04 17:36:42', '2026-09-04 17:36:42', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (3, 1, '洽洽', '', '', 1, 0, '2026-09-04 17:36:46', '2026-09-04 17:36:46', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (4, 1, '乐事', '', '', 1, 0, '2026-09-04 17:36:51', '2026-09-04 17:36:51', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (5, 1, '康师傅', '', '', 1, 0, '2026-09-04 17:36:56', '2026-09-04 17:36:56', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (6, 1, '统一', '', '', 1, 0, '2026-09-04 17:37:05', '2026-09-04 17:37:05', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (7, 1, '三全', '', '', 1, 0, '2026-09-04 17:37:11', '2026-09-04 17:37:11', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (8, 1, '农夫山泉', '', '', 1, 0, '2026-09-04 17:37:16', '2026-09-04 17:37:16', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (9, 1, '可口可乐', '', '', 1, 0, '2026-09-04 17:37:22', '2026-09-04 17:37:22', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (10, 1, '香飘飘', '', '', 1, 0, '2026-09-04 17:37:26', '2026-09-04 17:37:26', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (11, 1, '十月稻田', '', '', 1, 0, '2026-09-04 17:37:30', '2026-09-04 17:37:30', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (12, 1, '金龙鱼', '', '', 1, 0, '2026-09-04 17:37:33', '2026-09-04 17:37:33', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (13, 1, '海天', '', '', 1, 0, '2026-09-04 17:37:37', '2026-09-04 17:37:37', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (14, 1, '本地果业', '', '', 1, 0, '2026-09-04 17:37:40', '2026-09-04 17:37:40', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (15, 1, '本地蔬菜基地', '', '', 1, 0, '2026-09-04 17:37:45', '2026-09-04 17:37:45', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (17, 1, '农家鲜蛋', '', '', 1, 0, '2026-09-04 17:37:49', '2026-09-04 17:37:49', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (18, 1, '本地屠宰场', '', '', 1, 0, '2026-09-04 17:37:53', '2026-09-04 17:37:53', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (19, 1, '海天下', '', '', 1, 0, '2026-09-04 17:37:56', '2026-09-04 17:37:56', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (20, 1, '国联水产', '', '', 1, 0, '2026-09-04 17:38:02', '2026-09-04 17:38:02', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (21, 1, '海飞丝', '', '', 1, 0, '2026-09-04 17:38:08', '2026-09-04 17:38:08', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (22, 1, '舒肤佳', '', '', 1, 0, '2026-09-04 17:38:12', '2026-09-04 17:38:12', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (23, 1, '高露洁', '', '', 1, 0, '2026-09-04 17:38:16', '2026-09-04 17:38:16', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (24, 1, '维达', '', '', 1, 0, '2026-09-04 17:38:20', '2026-09-04 17:38:20', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (25, 1, '蓝月亮', '', '', 1, 0, '2026-09-04 17:38:24', '2026-09-04 17:38:24', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (26, 1, '美的', '', '', 1, 0, '2026-09-04 17:38:27', '2026-09-04 17:38:27', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (27, 1, '洁成', '', '', 1, 0, '2026-09-04 17:38:33', '2026-09-04 17:38:33', 1, 1, 0);
INSERT INTO `pms_brand` VALUES (28, 1, '晨光', '', '', 1, 0, '2026-09-04 17:38:36', '2026-09-04 17:38:36', 1, 1, 0);

-- ----------------------------
-- Table structure for pms_category
-- ----------------------------
DROP TABLE IF EXISTS `pms_category`;
CREATE TABLE `pms_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类编码',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID（0表示顶级分类）',
  `level` tinyint NOT NULL COMMENT '层级（1/2/3）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `icon` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类图标',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_tenant_parent`(`tenant_id`, `parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 115 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_category
-- ----------------------------
INSERT INTO `pms_category` VALUES (94, 1, '食品饮料', 'FD', 0, 1, 0, NULL, 1, '2026-09-01 16:11:02', '2026-09-01 16:11:02', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (95, 1, '生鲜果蔬', 'FV', 0, 1, 0, NULL, 1, '2026-09-01 16:11:18', '2026-09-01 16:11:18', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (96, 1, '个护日化', 'PC', 0, 1, 0, NULL, 1, '2026-09-01 16:11:31', '2026-09-01 16:11:31', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (97, 1, '家居百货', 'HH', 0, 1, 0, NULL, 1, '2026-09-01 16:11:41', '2026-09-01 16:11:41', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (98, 1, '休闲零食', 'FD-01', 94, 2, 0, NULL, 1, '2026-09-01 16:20:37', '2026-09-01 16:20:37', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (99, 1, '方便速食', 'FD‑02', 94, 2, 0, NULL, 1, '2026-09-01 16:21:18', '2026-09-01 16:21:18', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (100, 1, '饮料冲调', 'FD‑03', 94, 2, 0, NULL, 1, '2026-09-01 16:25:06', '2026-09-01 16:25:06', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (101, 1, '粮油调味', 'FD‑04', 94, 2, 0, NULL, 1, '2026-09-01 16:25:18', '2026-09-01 16:25:18', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (102, 1, '新鲜水果', 'FV‑01', 95, 2, 0, NULL, 1, '2026-09-01 16:25:34', '2026-09-01 16:25:34', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (103, 1, '新鲜蔬菜', 'FV‑02', 95, 2, 0, NULL, 1, '2026-09-01 16:26:09', '2026-09-01 16:26:09', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (104, 1, '肉禽蛋类', 'FV‑03', 95, 2, 0, NULL, 1, '2026-09-01 16:26:49', '2026-09-01 16:26:49', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (105, 1, '水产海鲜', 'FV‑04', 95, 2, 0, NULL, 1, '2026-09-01 16:28:51', '2026-09-01 16:28:51', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (106, 1, '个人洗护', 'PC‑01', 96, 2, 0, NULL, 1, '2026-09-01 16:29:08', '2026-09-01 16:29:08', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (107, 1, '口腔护理', 'PC‑02', 96, 2, 0, NULL, 1, '2026-09-01 16:29:27', '2026-09-01 16:29:27', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (108, 1, '纸品家清', 'PC‑03', 96, 2, 0, NULL, 1, '2026-09-01 16:29:38', '2026-09-01 16:29:38', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (109, 1, '美妆护肤', 'PC‑04', 96, 2, 0, NULL, 1, '2026-09-01 16:29:50', '2026-09-01 16:29:50', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (110, 1, '厨具餐具', 'HH‑01', 97, 2, 0, NULL, 1, '2026-09-01 16:30:56', '2026-09-01 16:30:56', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (111, 1, '家纺布艺', 'HH‑02', 97, 2, 0, NULL, 1, '2026-09-01 16:32:20', '2026-09-01 16:32:20', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (112, 1, '收纳用品', 'HH‑03', 97, 2, 0, NULL, 1, '2026-09-01 16:32:31', '2026-09-01 16:32:31', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (113, 1, '文体玩具', 'HH‑04', 97, 2, 0, NULL, 1, '2026-09-01 16:32:41', '2026-09-01 16:32:41', NULL, NULL, 0);
INSERT INTO `pms_category` VALUES (114, 1, '一次性用品', 'HH‑05', 97, 2, 0, NULL, 1, '2026-09-01 16:32:50', '2026-09-01 16:32:50', NULL, NULL, 0);

-- ----------------------------
-- Table structure for pms_product
-- ----------------------------
DROP TABLE IF EXISTS `pms_product`;
CREATE TABLE `pms_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `product_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品编号',
  `barcode` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '条形码',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `pinyin` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '拼音首字母（用于搜索）',
  `short_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '简称（小票打印用）',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `brand_id` bigint NULL DEFAULT NULL COMMENT '品牌ID',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格（如 500ml、1kg）',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '单位（个/瓶/袋/kg）',
  `purchase_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '进货价',
  `sale_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '零售价',
  `vip_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '会员价',
  `weight` decimal(10, 3) NULL DEFAULT NULL COMMENT '重量（kg）',
  `image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
  `is_weight` tinyint NOT NULL DEFAULT 0 COMMENT '是否称重商品：0-否 1-是',
  `is_promotion` tinyint NOT NULL DEFAULT 0 COMMENT '是否促销：0-否 1-是',
  `sale_count` int NOT NULL DEFAULT 0 COMMENT '销售数量（用于热销排行）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_product_no`(`tenant_id`, `product_no`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_barcode`(`barcode`) USING BTREE,
  INDEX `idx_pinyin`(`pinyin`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_brand_id`(`brand_id`) USING BTREE,
  INDEX `idx_supplier_id`(`supplier_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_sale_count`(`sale_count`) USING BTREE,
  INDEX `idx_tenant_status`(`tenant_id`, `status`) USING BTREE,
  INDEX `idx_tenant_category`(`tenant_id`, `category_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_product
-- ----------------------------
INSERT INTO `pms_product` VALUES (1, 1, 'P1788252955941', '6901668002424', '奥利奥原味夹心饼干', '奥利奥原味夹心饼干', NULL, 98, 1, NULL, '388g / 袋', '件', 8.20, 12.90, 11.50, NULL, '/api/v1/upload/file/product/df5a8557ab4b4bef9cc2d249f9d5a17a.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 16:55:56', '2026-09-01 16:55:56', 1, 1, 0);
INSERT INTO `pms_product` VALUES (2, 1, 'P1788253252471', '6924743230222', '卫龙大面筋辣条', '卫龙大面筋辣条', NULL, 98, 2, NULL, '68g / 包', '件', 3.60, 6.50, 5.80, NULL, '/api/v1/upload/file/product/e62b586580f0462a96db8aea876b0c91.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:00:52', '2026-09-01 17:00:52', 1, 1, 0);
INSERT INTO `pms_product` VALUES (3, 1, 'P1788253279735', '6924187800553', '洽洽香瓜子', '洽洽香瓜子', NULL, 98, 3, NULL, '260g / 袋', '件', 6.10, 9.90, 8.80, NULL, '/api/v1/upload/file/product/dbb4e09a7b634d3291241343c7e23748.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:01:20', '2026-09-01 17:01:20', 1, 1, 0);
INSERT INTO `pms_product` VALUES (4, 1, 'P1788253412109', '6924743222029', '乐事原味薯片', 'fanjun原味薯片', NULL, 98, 4, NULL, '75g / 罐', '件', 4.70, 8.00, 7.20, NULL, '/api/v1/upload/file/product/71fc7816a8154a12b81e3dc76cb84c49.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:03:32', '2026-09-01 17:03:32', 1, 1, 0);
INSERT INTO `pms_product` VALUES (5, 1, 'P1788253452965', '6903252100136', '康师傅红烧牛肉面', '康师傅红烧牛肉面', NULL, 99, 5, NULL, '142g / 桶', '件', 2.40, 4.50, 3.79, NULL, '/api/v1/upload/file/product/b6816cb982554609a25f05217b75be57.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:04:13', '2026-09-01 17:04:13', 1, 1, 0);
INSERT INTO `pms_product` VALUES (6, 1, 'P1788253559508', '6925037202426', '统一老坛酸菜牛肉面', '统a老坛酸菜牛肉面', NULL, 99, 6, NULL, '138g / 桶', '件', 2.40, 4.20, 3.79, NULL, '/api/v1/upload/file/product/3fc17eebed4040759a414494eeebf763.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:06:00', '2026-09-01 17:06:00', 1, 1, 0);
INSERT INTO `pms_product` VALUES (7, 1, 'P1788253607868', '6908791004727', '三全猪肉白菜水饺', 'bao全猪肉白菜水饺', NULL, 99, 7, NULL, '1000g / 袋', '件', 11.20, 16.80, 14.90, NULL, '/api/v1/upload/file/product/5421edb8b1bf4ecdb46015a796727cec.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:06:48', '2026-09-01 17:06:48', 1, 1, 0);
INSERT INTO `pms_product` VALUES (8, 1, 'P1788253950243', '6921168500244', '农夫山泉饮用天然水', '农夫山泉饮用天然水', NULL, 100, 8, NULL, '550ml / 瓶', '件', 1.10, 2.00, 1.79, NULL, '/api/v1/upload/file/product/7c4541a4d3ef44dcb41fb027a8ae013e.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:12:30', '2026-09-01 17:12:30', 1, 1, 0);
INSERT INTO `pms_product` VALUES (9, 1, 'P1788254104114', '6921317700208', '可口可乐碳酸饮料', '可口可fan碳酸饮料', NULL, 100, 9, NULL, '500ml / 瓶', '件', 2.00, 3.50, 3.10, NULL, '/api/v1/upload/file/product/97612e45c1df442abe530d81a6712d62.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:15:04', '2026-09-01 17:15:04', 1, 1, 0);
INSERT INTO `pms_product` VALUES (10, 1, 'P1788254580995', '6921770082327', '香飘飘原味奶茶', '香飘飘原味奶茶', NULL, 100, 10, NULL, '80g / 杯', '件', 3.40, 6.00, 5.30, NULL, '/api/v1/upload/file/product/354e8cdf40e54708bd39d45dad205b7e.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:23:01', '2026-09-01 17:23:01', 1, 1, 0);
INSERT INTO `pms_product` VALUES (11, 1, 'P1788254680600', '6971134460012', '五常大米', 'kong常大米', NULL, 101, 11, NULL, '5kg / 袋', '件', 42.50, 59.90, 54.90, NULL, '/api/v1/upload/file/product/ec7ddb457c794276aed1be99a33abf54.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:24:41', '2026-09-01 17:24:41', 1, 1, 0);
INSERT INTO `pms_product` VALUES (12, 1, 'P1788254719196', '6948195800122', '金龙鱼食用调和油', '金龙鱼食用调和油', NULL, 101, 12, NULL, '5L / 桶', '件', 51.00, 68.00, 62.00, NULL, '/api/v1/upload/file/product/5d9f4a9a8cd042ce8641492af489fbc6.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:25:19', '2026-09-01 17:25:19', 1, 1, 0);
INSERT INTO `pms_product` VALUES (13, 1, 'P1788254777288', '6902262200148', '海天金标生抽', '海天金标生抽', NULL, 101, 13, NULL, '1.28L / 瓶', '件', 9.30, 14.50, 12.90, NULL, '/api/v1/upload/file/product/f2f39876122145baa6dc87256c559e8c.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:26:17', '2026-09-01 17:26:17', 1, 1, 0);
INSERT INTO `pms_product` VALUES (14, 1, 'P1788255795380', '2100000000017', '红富士苹果', '红富士苹果', NULL, 102, 14, NULL, '1kg / 份', '件', 4.80, 7.99, 6.99, NULL, '/api/v1/upload/file/product/b1702361eed4426cacca695447f097cf.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:43:15', '2026-09-01 17:43:15', 1, 1, 0);
INSERT INTO `pms_product` VALUES (15, 1, 'P1788255863589', '2100000000024', '阳光玫瑰葡萄', '阳光玫瑰葡萄', NULL, 102, 14, NULL, '500g / 份', '件', 12.50, 19.90, 17.90, NULL, '/api/v1/upload/file/product/7a16d17b127a436aa28c1e18a6519761.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:44:24', '2026-09-01 17:44:24', 1, 1, 0);
INSERT INTO `pms_product` VALUES (16, 1, 'P1788255921808', '2200000000031', '上海青', 'bei海青', NULL, 103, 15, NULL, '500g / 份', '件', 1.40, 2.99, 2.49, NULL, '/api/v1/upload/file/product/6b149e27edbc4710906387a47a2cfd28.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:45:22', '2026-09-01 17:45:22', 1, 1, 0);
INSERT INTO `pms_product` VALUES (17, 1, 'P1788255971510', '2200000000048', '西红柿', '西红柿', NULL, 103, 15, NULL, '500g / 份', '件', 2.30, 4.50, 3.99, NULL, '/api/v1/upload/file/product/474ec98b40cb49038ed38144fa290d4b.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:46:12', '2026-09-01 17:46:12', 1, 1, 0);
INSERT INTO `pms_product` VALUES (18, 1, 'P1788256099821', '2300000000055', '新鲜土鸡蛋', '新鲜土鸡蛋', NULL, 104, 17, NULL, '30 枚 / 盒', '件', 8.10, 12.80, 11.20, NULL, '/api/v1/upload/file/product/f13098e5e9a74f9bbddc057b3281f38e.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:48:20', '2026-09-01 17:48:20', 1, 1, 0);
INSERT INTO `pms_product` VALUES (19, 1, 'P1788256134333', '2300000000062', '猪前腿肉', '猪前腿肉', NULL, 104, 18, NULL, '500g / 份', '件', 16.30, 22.90, 20.90, NULL, '/api/v1/upload/file/product/5cb1365bf7664595ba2fdb0c5d95cd7b.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:48:54', '2026-09-01 17:48:54', 1, 1, 0);
INSERT INTO `pms_product` VALUES (20, 1, 'P1788256163385', '2400000000079', '冷冻巴沙鱼柳', '冷冻巴沙鱼柳', NULL, 105, 20, NULL, '1000g / 袋', '件', 10.40, 15.90, 14.20, NULL, '/api/v1/upload/file/product/3fd9e31208344ed8b7190962ee4d9cd7.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:49:23', '2026-09-01 17:49:23', 1, 1, 0);
INSERT INTO `pms_product` VALUES (21, 1, 'P1788256202293', '2400000000086', '海虾冷冻大虾', '海虾冷冻大虾', NULL, 105, 20, NULL, '500g / 袋', '件', 26.00, 36.90, 33.90, NULL, '/api/v1/upload/file/product/837c613522674d7c81fe8cdf43cccac7.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:50:02', '2026-09-01 17:50:02', 1, 1, 0);
INSERT INTO `pms_product` VALUES (22, 1, 'P1788256412488', '6903148041068', '海飞丝去屑洗发水', '海飞cha去屑洗发水', NULL, 106, 21, NULL, '750ml / 瓶', '件', 27.20, 39.90, 35.90, NULL, '/api/v1/upload/file/product/67bb150dda7b48c6ac3808c897331072.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:53:32', '2026-09-03 18:26:35', 1, 1, 0);
INSERT INTO `pms_product` VALUES (23, 1, 'P1788256461666', '6903148044205', '舒肤佳沐浴露', '舒肤zhan沐浴露', NULL, 106, 22, NULL, '720ml / 瓶', '件', 16.10, 24.80, 22.30, NULL, '/api/v1/upload/file/product/c1e7b1111a8043f19ba52a9e8bcd0f34.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:54:22', '2026-09-03 18:26:33', 1, 1, 0);
INSERT INTO `pms_product` VALUES (24, 1, 'P1788256582580', '6920354503414', '高露洁冰爽牙膏', '高露洁冰爽牙膏', NULL, 107, 23, NULL, '180g / 支', '件', 10.20, 16.50, 14.80, NULL, '/api/v1/upload/file/product/d12a2407a34841ddacc5023b0c524aba.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:56:23', '2026-09-03 18:26:30', 1, 1, 0);
INSERT INTO `pms_product` VALUES (25, 1, 'P1788256617103', '6922168802448', '维达卷纸', '维达卷纸', NULL, 108, 24, NULL, '10 卷 / 提', '件', 20.50, 29.90, 26.90, NULL, '/api/v1/upload/file/product/40555cbebdeb45d69bccdbe7a7da246f.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:56:57', '2026-09-03 18:26:28', 1, 1, 0);
INSERT INTO `pms_product` VALUES (26, 1, 'P1788256779342', '6902022110388', '蓝月亮洗衣液', '蓝月long洗衣液', NULL, 108, 25, NULL, '3kg / 瓶', '件', 31.60, 45.00, 40.80, NULL, '/api/v1/upload/file/product/34a1e00969b5478f9e119fe2763b683b.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 17:59:39', '2026-09-03 18:26:25', 1, 1, 0);
INSERT INTO `pms_product` VALUES (27, 1, 'P1788256812139', '6940096031225', '美的不锈钢汤勺', '美的bi锈钢汤勺', NULL, 110, 26, NULL, '1 把 / 个', '件', 7.80, 12.90, 11.50, NULL, '/api/v1/upload/file/product/dce28fb4ebe84f23969e2b77602fbd44.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 18:00:12', '2026-09-03 18:26:22', 1, 1, 0);
INSERT INTO `pms_product` VALUES (28, 1, 'P1788256846145', '6935349200614', '家用加厚垃圾袋', '家用加厚垃圾袋', NULL, 114, 27, NULL, '100 只 / 包', '件', 5.40, 9.90, 8.80, NULL, '/api/v1/upload/file/product/37a4bde8a20942e8bdad9b6e1659e4f2.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 18:00:46', '2026-09-03 18:26:20', 1, 1, 0);
INSERT INTO `pms_product` VALUES (29, 1, 'P1788257030160', '6927804081108', '晨光中性笔', '晨光chui性笔', NULL, 113, 28, NULL, '1 支 / 盒', '件', 1.30, 2.50, 2.20, NULL, '/api/v1/upload/file/product/0d2c6e0310fa4edf96806ae8714b321d.png', 1, 0, 0, 0, 0, NULL, '2026-09-01 18:03:50', '2026-09-03 18:13:14', 1, 1, 0);

-- ----------------------------
-- Table structure for pms_supplier
-- ----------------------------
DROP TABLE IF EXISTS `pms_supplier`;
CREATE TABLE `pms_supplier`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '银行账号',
  `bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开户行',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_supplier_no`(`tenant_id`, `supplier_no`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '供应商表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_supplier
-- ----------------------------
INSERT INTO `pms_supplier` VALUES (1, 1, 'SUP001', '华润万家供应商', '张经理', '13800138001', '', '', '', 1, NULL, '2026-09-02 08:48:23', '2026-09-02 08:48:23', NULL, NULL, 0);
INSERT INTO `pms_supplier` VALUES (2, 1, 'SUP002', '中粮集团', '李经理', '13900139000', NULL, NULL, NULL, 1, NULL, '2026-09-02 08:48:23', '2026-09-02 08:48:23', NULL, NULL, 0);
INSERT INTO `pms_supplier` VALUES (3, 1, 'SUP003', '伊利乳业', '王经理', '13700137000', NULL, NULL, NULL, 1, NULL, '2026-09-02 08:48:23', '2026-09-02 08:48:23', NULL, NULL, 0);

-- ----------------------------
-- Table structure for rpt_daily_sales
-- ----------------------------
DROP TABLE IF EXISTS `rpt_daily_sales`;
CREATE TABLE `rpt_daily_sales`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `order_count` int NOT NULL DEFAULT 0 COMMENT '订单数',
  `total_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '销售总额',
  `discount_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠总额',
  `pay_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '实收总额',
  `cost_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '成本总额',
  `profit_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '毛利总额',
  `cash_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '现金收入',
  `wechat_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '微信收入',
  `alipay_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '支付宝收入',
  `unionpay_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '银联收入',
  `stored_value_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '储值卡消费',
  `member_count` int NOT NULL DEFAULT 0 COMMENT '会员消费人次',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_store_date`(`tenant_id`, `store_id`, `stat_date`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_stat_date`(`stat_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '日销售汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rpt_daily_sales
-- ----------------------------

-- ----------------------------
-- Table structure for rpt_monthly_sales
-- ----------------------------
DROP TABLE IF EXISTS `rpt_monthly_sales`;
CREATE TABLE `rpt_monthly_sales`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `stat_year` int NOT NULL COMMENT '统计年份',
  `stat_month` int NOT NULL COMMENT '统计月份',
  `order_count` int NOT NULL DEFAULT 0 COMMENT '订单数',
  `total_amount` decimal(14, 2) NOT NULL DEFAULT 0.00 COMMENT '销售总额',
  `discount_amount` decimal(14, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠总额',
  `pay_amount` decimal(14, 2) NOT NULL DEFAULT 0.00 COMMENT '实收总额',
  `cost_amount` decimal(14, 2) NOT NULL DEFAULT 0.00 COMMENT '成本总额',
  `profit_amount` decimal(14, 2) NOT NULL DEFAULT 0.00 COMMENT '毛利总额',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_store_month`(`tenant_id`, `store_id`, `stat_year`, `stat_month`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_stat_year_month`(`stat_year`, `stat_month`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '月销售汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rpt_monthly_sales
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置值',
  `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'string' COMMENT '配置类型：string/number/boolean/json',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典类型',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典名称',
  `dict_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典值',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_type_code`(`dict_type`, `dict_code`) USING BTREE,
  INDEX `idx_dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `login_type` tinyint NOT NULL COMMENT '登录类型：1-后台登录 2-POS登录',
  `login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录IP',
  `login_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作系统',
  `status` tinyint NOT NULL COMMENT '登录状态：0-失败 1-成功',
  `msg` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提示消息',
  `login_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_login_time`(`login_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父菜单ID（0表示顶级菜单）',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '菜单类型：1-目录 2-菜单 3-按钮',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由地址',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识（如 product:add）',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否可见：0-隐藏 1-显示',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 805 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (100, 0, '商品管理', 0, '/admin/product', NULL, NULL, 'CartOutline', 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (101, 100, '商品列表', 1, '/admin/product/list', '/admin/product/ProductList.vue', 'pms:product:list', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (102, 100, '商品分类', 1, '/admin/product/category', '/admin/product/ProductCategory.vue', 'pms:category:list', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (103, 100, '品牌管理', 1, '/admin/product/brand', '/admin/product/BrandList.vue', 'pms:brand:list', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (104, 100, '供应商管理', 1, '/admin/supplier/list', '/admin/supplier/SupplierList.vue', 'pms:supplier:list', NULL, 4, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (111, 101, '商品新增', 2, NULL, NULL, 'pms:product:add', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (112, 101, '商品编辑', 2, NULL, NULL, 'pms:product:edit', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (113, 101, '商品删除', 2, NULL, NULL, 'pms:product:delete', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (121, 103, '品牌新增', 2, NULL, NULL, 'pms:brand:add', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (122, 103, '品牌编辑', 2, NULL, NULL, 'pms:brand:edit', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (123, 103, '品牌删除', 2, NULL, NULL, 'pms:brand:delete', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (131, 104, '供应商新增', 2, NULL, NULL, 'pms:supplier:add', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (132, 104, '供应商编辑', 2, NULL, NULL, 'pms:supplier:edit', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (133, 104, '供应商删除', 2, NULL, NULL, 'pms:supplier:delete', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (200, 0, '库存管理', 0, '/admin/stock', NULL, NULL, 'LayersOutline', 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (201, 200, '库存列表', 1, '/admin/stock/list', '/admin/stock/StockList.vue', 'wms:stock:list', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (202, 200, '入库管理', 1, '/admin/stock/inbound', '/admin/stock/StockInbound.vue', 'wms:stock:inbound', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (203, 200, '出库管理', 1, '/admin/stock/outbound', '/admin/stock/StockOutbound.vue', 'wms:stock:outbound', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (204, 200, '库存盘点', 1, '/admin/stock/check', '/admin/stock/StockCheck.vue', 'wms:stock:check', NULL, 4, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (211, 202, '入库操作', 2, NULL, NULL, 'wms:stock:inbound:add', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (221, 203, '出库操作', 2, NULL, NULL, 'wms:stock:outbound:add', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (300, 0, '订单管理', 0, '/admin/order', NULL, NULL, 'DocumentTextOutline', 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (301, 300, '订单列表', 1, '/admin/order/list', '/admin/order/OrderList.vue', 'oms:order:list', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (302, 300, '支付流水', 1, '/admin/order/payment-flow', '/admin/order/PaymentFlow.vue', 'oms:order:payment', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (311, 301, '订单详情', 2, NULL, NULL, 'oms:order:detail', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (312, 301, '订单取消', 2, NULL, NULL, 'oms:order:cancel', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (313, 301, '订单完成', 2, NULL, NULL, 'oms:order:complete', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (314, 301, '订单退款', 2, NULL, NULL, 'oms:order:refund', NULL, 4, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (400, 0, '会员管理', 0, '/admin/member', NULL, NULL, 'PeopleOutline', 4, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (401, 400, '会员列表', 1, '/admin/member/list', '/admin/member/MemberList.vue', 'ums:member:list', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (411, 401, '会员新增', 2, NULL, NULL, 'ums:member:add', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (412, 401, '会员编辑', 2, NULL, NULL, 'ums:member:edit', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (413, 401, '会员充值', 2, NULL, NULL, 'ums:member:recharge', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (500, 0, '报表统计', 0, '/admin/report', NULL, NULL, 'BarChartOutline', 5, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (501, 500, '销售报表', 1, '/admin/report/sales', '/admin/report/SalesReport.vue', 'rpt:sales:list', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (502, 500, '库存报表', 1, '/admin/report/stock', '/admin/report/ReportStock.vue', 'rpt:stock:list', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (503, 500, '会员报表', 1, '/admin/report/member', '/admin/report/ReportMember.vue', 'rpt:member:list', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (600, 0, '系统管理', 0, '/system', NULL, NULL, 'SettingsOutline', 6, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (601, 600, '租户管理', 1, '/system/tenant/list', '/system/tenant/TenantList.vue', 'sys:tenant:list', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (602, 600, '门店管理', 1, '/admin/store/list', '/system/store/StoreList.vue', 'sys:store:list', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-06 13:03:05');
INSERT INTO `sys_menu` VALUES (603, 600, '用户管理', 1, '/admin/system/users', '/system/user/UserList.vue', 'sys:user:list', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-06 13:03:05');
INSERT INTO `sys_menu` VALUES (604, 600, '角色管理', 1, '/admin/system/roles', '/system/role/RoleList.vue', 'sys:role:list', NULL, 4, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-06 13:03:05');
INSERT INTO `sys_menu` VALUES (605, 600, '菜单管理', 1, '/system/menu/list', '/system/menu/MenuList.vue', 'sys:menu:list', NULL, 5, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (606, 600, '系统设置', 1, '/admin/system/settings', '/system/SystemSettings.vue', 'sys:settings:view', NULL, 6, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-06 13:03:05');
INSERT INTO `sys_menu` VALUES (607, 600, '操作日志', 1, '/admin/system/logs', '/system/logs/SystemLogs.vue', 'sys:logs:list', NULL, 7, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-06 13:03:05');
INSERT INTO `sys_menu` VALUES (611, 601, '租户新增', 2, NULL, NULL, 'sys:tenant:add', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (612, 601, '租户编辑', 2, NULL, NULL, 'sys:tenant:edit', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (613, 601, '租户删除', 2, NULL, NULL, 'sys:tenant:delete', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (621, 603, '用户新增', 2, NULL, NULL, 'sys:user:add', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (622, 603, '用户编辑', 2, NULL, NULL, 'sys:user:edit', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (623, 603, '用户删除', 2, NULL, NULL, 'sys:user:delete', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (624, 603, '重置密码', 2, NULL, NULL, 'sys:user:resetpwd', NULL, 4, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (631, 604, '角色新增', 2, NULL, NULL, 'sys:role:add', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (632, 604, '角色编辑', 2, NULL, NULL, 'sys:role:edit', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (633, 604, '角色删除', 2, NULL, NULL, 'sys:role:delete', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (641, 605, '菜单新增', 2, NULL, NULL, 'sys:menu:add', NULL, 1, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (642, 605, '菜单编辑', 2, NULL, NULL, 'sys:menu:edit', NULL, 2, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (643, 605, '菜单删除', 2, NULL, NULL, 'sys:menu:delete', NULL, 3, 1, 1, 0, '2026-09-05 14:19:40', '2026-09-05 14:19:40');
INSERT INTO `sys_menu` VALUES (700, 0, '收银台', 0, '/pos', NULL, NULL, 'CartOutline', 0, 1, 1, 0, '2026-09-06 10:39:23', '2026-09-06 10:39:23');
INSERT INTO `sys_menu` VALUES (701, 700, '收银结算', 1, '/pos/cashier', '/pos/Cashier.vue', 'pos:cashier', NULL, 1, 1, 1, 0, '2026-09-06 10:39:23', '2026-09-06 10:39:23');
INSERT INTO `sys_menu` VALUES (702, 700, '今日订单', 1, '/pos/orders', '/pos/OrderDetail.vue', 'pos:orders', NULL, 2, 1, 1, 0, '2026-09-06 10:39:23', '2026-09-06 10:39:23');
INSERT INTO `sys_menu` VALUES (804, 300, '财务日结', 1, '/admin/order/daily-settlement', '/admin/order/DailySettlement.vue', NULL, NULL, 4, 1, 1, 0, '2026-09-07 08:14:53', '2026-09-07 08:14:53');

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作模块',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作类型（增/删/改/查）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求方法',
  `url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求URL',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '响应结果',
  `status` tinyint NOT NULL COMMENT '操作状态：0-失败 1-成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作IP',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作人用户名',
  `duration` bigint NULL DEFAULT NULL COMMENT '耗时（毫秒）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_module`(`module`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_payment_method
-- ----------------------------
DROP TABLE IF EXISTS `sys_payment_method`;
CREATE TABLE `sys_payment_method`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付方式名称',
  `code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编码：cash/wechat/alipay/card/stored',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_code`(`tenant_id`, `code`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付方式表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_payment_method
-- ----------------------------
INSERT INTO `sys_payment_method` VALUES (1, 1, '现金', 'cash', NULL, 1, 1, 1000.00, '', '2026-09-02 15:02:09', '2026-09-02 15:45:30', NULL, NULL, 0);
INSERT INTO `sys_payment_method` VALUES (2, 1, '微信支付', 'wechat', NULL, 2, 1, 20102.50, NULL, '2026-09-02 15:02:09', '2026-09-04 10:53:29', NULL, NULL, 0);
INSERT INTO `sys_payment_method` VALUES (3, 1, '支付宝', 'alipay', NULL, 3, 1, 50000.00, NULL, '2026-09-02 15:02:09', '2026-09-02 15:02:09', NULL, NULL, 0);
INSERT INTO `sys_payment_method` VALUES (4, 1, '建设银行柘城支行', 'card', NULL, 4, 1, 300000.00, '', '2026-09-02 15:02:09', '2026-09-02 20:56:00', NULL, NULL, 0);
INSERT INTO `sys_payment_method` VALUES (5, 1, '会员储值卡', 'stored', NULL, 5, 1, 0.00, '', '2026-09-02 15:02:09', '2026-09-02 20:56:09', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_role_code`(`tenant_id`, `role_code`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 1, '租户管理员', 'TENANT_ADMIN', '租户最高管理员，拥有所有业务模块权限', 1, 1, '2026-09-05 14:19:40', '2026-09-05 14:19:40', NULL, NULL, 0);
INSERT INTO `sys_role` VALUES (2, 1, '店长', 'STORE_MANAGER', '门店店长，管理商品、库存、订单、会员', 1, 2, '2026-09-05 14:19:40', '2026-09-05 14:19:40', NULL, NULL, 0);
INSERT INTO `sys_role` VALUES (3, 1, '收银员', 'CASHIER', '收银员，负责收银和会员管理', 1, 3, '2026-09-05 14:19:40', '2026-09-05 14:19:40', NULL, NULL, 0);
INSERT INTO `sys_role` VALUES (4, 1, '仓管员', 'WAREHOUSE_KEEPER', '仓管员，负责库存管理', 1, 4, '2026-09-05 14:19:40', '2026-09-05 14:19:40', NULL, NULL, 0);
INSERT INTO `sys_role` VALUES (9, 1, '系统管理员', 'sysAdmin', '', 1, 0, '2026-09-06 17:25:32', '2026-09-06 17:25:32', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id`, `menu_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 438 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (39, 2, 100, 0);
INSERT INTO `sys_role_menu` VALUES (40, 2, 101, 0);
INSERT INTO `sys_role_menu` VALUES (41, 2, 102, 0);
INSERT INTO `sys_role_menu` VALUES (42, 2, 103, 0);
INSERT INTO `sys_role_menu` VALUES (43, 2, 104, 0);
INSERT INTO `sys_role_menu` VALUES (44, 2, 111, 0);
INSERT INTO `sys_role_menu` VALUES (45, 2, 112, 0);
INSERT INTO `sys_role_menu` VALUES (46, 2, 121, 0);
INSERT INTO `sys_role_menu` VALUES (47, 2, 122, 0);
INSERT INTO `sys_role_menu` VALUES (48, 2, 131, 0);
INSERT INTO `sys_role_menu` VALUES (49, 2, 132, 0);
INSERT INTO `sys_role_menu` VALUES (50, 2, 200, 0);
INSERT INTO `sys_role_menu` VALUES (51, 2, 201, 0);
INSERT INTO `sys_role_menu` VALUES (52, 2, 202, 0);
INSERT INTO `sys_role_menu` VALUES (53, 2, 203, 0);
INSERT INTO `sys_role_menu` VALUES (54, 2, 204, 0);
INSERT INTO `sys_role_menu` VALUES (56, 2, 211, 0);
INSERT INTO `sys_role_menu` VALUES (57, 2, 221, 0);
INSERT INTO `sys_role_menu` VALUES (58, 2, 300, 0);
INSERT INTO `sys_role_menu` VALUES (59, 2, 301, 0);
INSERT INTO `sys_role_menu` VALUES (60, 2, 302, 0);
INSERT INTO `sys_role_menu` VALUES (61, 2, 311, 0);
INSERT INTO `sys_role_menu` VALUES (62, 2, 312, 0);
INSERT INTO `sys_role_menu` VALUES (63, 2, 313, 0);
INSERT INTO `sys_role_menu` VALUES (64, 2, 314, 0);
INSERT INTO `sys_role_menu` VALUES (65, 2, 400, 0);
INSERT INTO `sys_role_menu` VALUES (66, 2, 401, 0);
INSERT INTO `sys_role_menu` VALUES (67, 2, 411, 0);
INSERT INTO `sys_role_menu` VALUES (68, 2, 412, 0);
INSERT INTO `sys_role_menu` VALUES (69, 2, 413, 0);
INSERT INTO `sys_role_menu` VALUES (70, 2, 500, 0);
INSERT INTO `sys_role_menu` VALUES (71, 2, 501, 0);
INSERT INTO `sys_role_menu` VALUES (72, 2, 502, 0);
INSERT INTO `sys_role_menu` VALUES (73, 2, 503, 0);
INSERT INTO `sys_role_menu` VALUES (84, 4, 200, 0);
INSERT INTO `sys_role_menu` VALUES (85, 4, 201, 0);
INSERT INTO `sys_role_menu` VALUES (86, 4, 202, 0);
INSERT INTO `sys_role_menu` VALUES (87, 4, 203, 0);
INSERT INTO `sys_role_menu` VALUES (88, 4, 204, 0);
INSERT INTO `sys_role_menu` VALUES (90, 4, 211, 0);
INSERT INTO `sys_role_menu` VALUES (91, 4, 221, 0);
INSERT INTO `sys_role_menu` VALUES (92, 6, 100, 0);
INSERT INTO `sys_role_menu` VALUES (93, 6, 101, 0);
INSERT INTO `sys_role_menu` VALUES (94, 6, 111, 0);
INSERT INTO `sys_role_menu` VALUES (95, 6, 112, 0);
INSERT INTO `sys_role_menu` VALUES (96, 6, 113, 0);
INSERT INTO `sys_role_menu` VALUES (97, 6, 102, 0);
INSERT INTO `sys_role_menu` VALUES (98, 6, 103, 0);
INSERT INTO `sys_role_menu` VALUES (99, 6, 121, 0);
INSERT INTO `sys_role_menu` VALUES (100, 6, 122, 0);
INSERT INTO `sys_role_menu` VALUES (101, 6, 123, 0);
INSERT INTO `sys_role_menu` VALUES (102, 6, 104, 0);
INSERT INTO `sys_role_menu` VALUES (103, 6, 131, 0);
INSERT INTO `sys_role_menu` VALUES (104, 6, 132, 0);
INSERT INTO `sys_role_menu` VALUES (105, 6, 133, 0);
INSERT INTO `sys_role_menu` VALUES (106, 6, 200, 0);
INSERT INTO `sys_role_menu` VALUES (107, 6, 201, 0);
INSERT INTO `sys_role_menu` VALUES (108, 6, 202, 0);
INSERT INTO `sys_role_menu` VALUES (109, 6, 211, 0);
INSERT INTO `sys_role_menu` VALUES (110, 6, 203, 0);
INSERT INTO `sys_role_menu` VALUES (111, 6, 221, 0);
INSERT INTO `sys_role_menu` VALUES (112, 6, 204, 0);
INSERT INTO `sys_role_menu` VALUES (114, 6, 300, 0);
INSERT INTO `sys_role_menu` VALUES (115, 6, 301, 0);
INSERT INTO `sys_role_menu` VALUES (116, 6, 311, 0);
INSERT INTO `sys_role_menu` VALUES (117, 6, 312, 0);
INSERT INTO `sys_role_menu` VALUES (118, 6, 313, 0);
INSERT INTO `sys_role_menu` VALUES (119, 6, 314, 0);
INSERT INTO `sys_role_menu` VALUES (120, 6, 302, 0);
INSERT INTO `sys_role_menu` VALUES (121, 6, 400, 0);
INSERT INTO `sys_role_menu` VALUES (122, 6, 401, 0);
INSERT INTO `sys_role_menu` VALUES (123, 6, 411, 0);
INSERT INTO `sys_role_menu` VALUES (124, 6, 412, 0);
INSERT INTO `sys_role_menu` VALUES (125, 6, 413, 0);
INSERT INTO `sys_role_menu` VALUES (126, 6, 500, 0);
INSERT INTO `sys_role_menu` VALUES (127, 6, 501, 0);
INSERT INTO `sys_role_menu` VALUES (128, 6, 502, 0);
INSERT INTO `sys_role_menu` VALUES (129, 6, 503, 0);
INSERT INTO `sys_role_menu` VALUES (130, 6, 600, 0);
INSERT INTO `sys_role_menu` VALUES (135, 6, 602, 0);
INSERT INTO `sys_role_menu` VALUES (136, 6, 603, 0);
INSERT INTO `sys_role_menu` VALUES (137, 6, 621, 0);
INSERT INTO `sys_role_menu` VALUES (138, 6, 622, 0);
INSERT INTO `sys_role_menu` VALUES (139, 6, 623, 0);
INSERT INTO `sys_role_menu` VALUES (140, 6, 624, 0);
INSERT INTO `sys_role_menu` VALUES (141, 6, 604, 0);
INSERT INTO `sys_role_menu` VALUES (142, 6, 631, 0);
INSERT INTO `sys_role_menu` VALUES (143, 6, 632, 0);
INSERT INTO `sys_role_menu` VALUES (144, 6, 633, 0);
INSERT INTO `sys_role_menu` VALUES (149, 6, 606, 0);
INSERT INTO `sys_role_menu` VALUES (150, 6, 607, 0);
INSERT INTO `sys_role_menu` VALUES (151, 7, 700, 0);
INSERT INTO `sys_role_menu` VALUES (152, 7, 701, 0);
INSERT INTO `sys_role_menu` VALUES (153, 7, 702, 0);
INSERT INTO `sys_role_menu` VALUES (158, 7, 300, 0);
INSERT INTO `sys_role_menu` VALUES (159, 7, 301, 0);
INSERT INTO `sys_role_menu` VALUES (160, 7, 302, 0);
INSERT INTO `sys_role_menu` VALUES (161, 7, 311, 0);
INSERT INTO `sys_role_menu` VALUES (162, 7, 313, 0);
INSERT INTO `sys_role_menu` VALUES (163, 6, 700, 0);
INSERT INTO `sys_role_menu` VALUES (164, 6, 701, 0);
INSERT INTO `sys_role_menu` VALUES (165, 6, 702, 0);
INSERT INTO `sys_role_menu` VALUES (166, 8, 700, 0);
INSERT INTO `sys_role_menu` VALUES (167, 8, 701, 0);
INSERT INTO `sys_role_menu` VALUES (168, 8, 702, 0);
INSERT INTO `sys_role_menu` VALUES (310, 9, 700, 0);
INSERT INTO `sys_role_menu` VALUES (311, 9, 701, 0);
INSERT INTO `sys_role_menu` VALUES (312, 9, 702, 0);
INSERT INTO `sys_role_menu` VALUES (313, 9, 100, 0);
INSERT INTO `sys_role_menu` VALUES (314, 9, 101, 0);
INSERT INTO `sys_role_menu` VALUES (315, 9, 111, 0);
INSERT INTO `sys_role_menu` VALUES (316, 9, 112, 0);
INSERT INTO `sys_role_menu` VALUES (317, 9, 113, 0);
INSERT INTO `sys_role_menu` VALUES (318, 9, 102, 0);
INSERT INTO `sys_role_menu` VALUES (319, 9, 103, 0);
INSERT INTO `sys_role_menu` VALUES (320, 9, 121, 0);
INSERT INTO `sys_role_menu` VALUES (321, 9, 122, 0);
INSERT INTO `sys_role_menu` VALUES (322, 9, 123, 0);
INSERT INTO `sys_role_menu` VALUES (323, 9, 104, 0);
INSERT INTO `sys_role_menu` VALUES (324, 9, 131, 0);
INSERT INTO `sys_role_menu` VALUES (325, 9, 132, 0);
INSERT INTO `sys_role_menu` VALUES (326, 9, 133, 0);
INSERT INTO `sys_role_menu` VALUES (327, 9, 200, 0);
INSERT INTO `sys_role_menu` VALUES (328, 9, 201, 0);
INSERT INTO `sys_role_menu` VALUES (329, 9, 202, 0);
INSERT INTO `sys_role_menu` VALUES (330, 9, 211, 0);
INSERT INTO `sys_role_menu` VALUES (331, 9, 203, 0);
INSERT INTO `sys_role_menu` VALUES (332, 9, 221, 0);
INSERT INTO `sys_role_menu` VALUES (333, 9, 204, 0);
INSERT INTO `sys_role_menu` VALUES (334, 9, 300, 0);
INSERT INTO `sys_role_menu` VALUES (335, 9, 301, 0);
INSERT INTO `sys_role_menu` VALUES (336, 9, 311, 0);
INSERT INTO `sys_role_menu` VALUES (337, 9, 312, 0);
INSERT INTO `sys_role_menu` VALUES (338, 9, 313, 0);
INSERT INTO `sys_role_menu` VALUES (339, 9, 314, 0);
INSERT INTO `sys_role_menu` VALUES (340, 9, 302, 0);
INSERT INTO `sys_role_menu` VALUES (341, 9, 400, 0);
INSERT INTO `sys_role_menu` VALUES (342, 9, 401, 0);
INSERT INTO `sys_role_menu` VALUES (343, 9, 411, 0);
INSERT INTO `sys_role_menu` VALUES (344, 9, 412, 0);
INSERT INTO `sys_role_menu` VALUES (345, 9, 413, 0);
INSERT INTO `sys_role_menu` VALUES (346, 9, 500, 0);
INSERT INTO `sys_role_menu` VALUES (347, 9, 501, 0);
INSERT INTO `sys_role_menu` VALUES (348, 9, 502, 0);
INSERT INTO `sys_role_menu` VALUES (349, 9, 503, 0);
INSERT INTO `sys_role_menu` VALUES (350, 9, 600, 0);
INSERT INTO `sys_role_menu` VALUES (351, 9, 601, 0);
INSERT INTO `sys_role_menu` VALUES (352, 9, 611, 0);
INSERT INTO `sys_role_menu` VALUES (353, 9, 612, 0);
INSERT INTO `sys_role_menu` VALUES (354, 9, 613, 0);
INSERT INTO `sys_role_menu` VALUES (355, 9, 602, 0);
INSERT INTO `sys_role_menu` VALUES (356, 9, 603, 0);
INSERT INTO `sys_role_menu` VALUES (357, 9, 621, 0);
INSERT INTO `sys_role_menu` VALUES (358, 9, 622, 0);
INSERT INTO `sys_role_menu` VALUES (359, 9, 623, 0);
INSERT INTO `sys_role_menu` VALUES (360, 9, 624, 0);
INSERT INTO `sys_role_menu` VALUES (361, 9, 604, 0);
INSERT INTO `sys_role_menu` VALUES (362, 9, 631, 0);
INSERT INTO `sys_role_menu` VALUES (363, 9, 632, 0);
INSERT INTO `sys_role_menu` VALUES (364, 9, 633, 0);
INSERT INTO `sys_role_menu` VALUES (365, 9, 605, 0);
INSERT INTO `sys_role_menu` VALUES (366, 9, 641, 0);
INSERT INTO `sys_role_menu` VALUES (367, 9, 642, 0);
INSERT INTO `sys_role_menu` VALUES (368, 9, 643, 0);
INSERT INTO `sys_role_menu` VALUES (369, 9, 606, 0);
INSERT INTO `sys_role_menu` VALUES (370, 9, 607, 0);
INSERT INTO `sys_role_menu` VALUES (375, 3, 300, 0);
INSERT INTO `sys_role_menu` VALUES (376, 3, 301, 0);
INSERT INTO `sys_role_menu` VALUES (377, 3, 311, 0);
INSERT INTO `sys_role_menu` VALUES (378, 3, 312, 0);
INSERT INTO `sys_role_menu` VALUES (379, 3, 313, 0);
INSERT INTO `sys_role_menu` VALUES (380, 3, 400, 0);
INSERT INTO `sys_role_menu` VALUES (381, 3, 401, 0);
INSERT INTO `sys_role_menu` VALUES (382, 3, 411, 0);
INSERT INTO `sys_role_menu` VALUES (383, 3, 413, 0);
INSERT INTO `sys_role_menu` VALUES (385, 2, 804, 0);
INSERT INTO `sys_role_menu` VALUES (386, 3, 804, 0);
INSERT INTO `sys_role_menu` VALUES (387, 6, 804, 0);
INSERT INTO `sys_role_menu` VALUES (388, 9, 804, 0);
INSERT INTO `sys_role_menu` VALUES (389, 1, 100, 0);
INSERT INTO `sys_role_menu` VALUES (390, 1, 101, 0);
INSERT INTO `sys_role_menu` VALUES (391, 1, 102, 0);
INSERT INTO `sys_role_menu` VALUES (392, 1, 103, 0);
INSERT INTO `sys_role_menu` VALUES (393, 1, 104, 0);
INSERT INTO `sys_role_menu` VALUES (394, 1, 111, 0);
INSERT INTO `sys_role_menu` VALUES (395, 1, 112, 0);
INSERT INTO `sys_role_menu` VALUES (396, 1, 113, 0);
INSERT INTO `sys_role_menu` VALUES (397, 1, 121, 0);
INSERT INTO `sys_role_menu` VALUES (398, 1, 122, 0);
INSERT INTO `sys_role_menu` VALUES (399, 1, 123, 0);
INSERT INTO `sys_role_menu` VALUES (400, 1, 131, 0);
INSERT INTO `sys_role_menu` VALUES (401, 1, 132, 0);
INSERT INTO `sys_role_menu` VALUES (402, 1, 133, 0);
INSERT INTO `sys_role_menu` VALUES (403, 1, 200, 0);
INSERT INTO `sys_role_menu` VALUES (404, 1, 201, 0);
INSERT INTO `sys_role_menu` VALUES (405, 1, 202, 0);
INSERT INTO `sys_role_menu` VALUES (406, 1, 203, 0);
INSERT INTO `sys_role_menu` VALUES (407, 1, 204, 0);
INSERT INTO `sys_role_menu` VALUES (408, 1, 211, 0);
INSERT INTO `sys_role_menu` VALUES (409, 1, 221, 0);
INSERT INTO `sys_role_menu` VALUES (410, 1, 300, 0);
INSERT INTO `sys_role_menu` VALUES (411, 1, 301, 0);
INSERT INTO `sys_role_menu` VALUES (412, 1, 302, 0);
INSERT INTO `sys_role_menu` VALUES (413, 1, 311, 0);
INSERT INTO `sys_role_menu` VALUES (414, 1, 312, 0);
INSERT INTO `sys_role_menu` VALUES (415, 1, 313, 0);
INSERT INTO `sys_role_menu` VALUES (416, 1, 314, 0);
INSERT INTO `sys_role_menu` VALUES (417, 1, 400, 0);
INSERT INTO `sys_role_menu` VALUES (418, 1, 401, 0);
INSERT INTO `sys_role_menu` VALUES (419, 1, 411, 0);
INSERT INTO `sys_role_menu` VALUES (420, 1, 412, 0);
INSERT INTO `sys_role_menu` VALUES (421, 1, 413, 0);
INSERT INTO `sys_role_menu` VALUES (422, 1, 500, 0);
INSERT INTO `sys_role_menu` VALUES (423, 1, 501, 0);
INSERT INTO `sys_role_menu` VALUES (424, 1, 502, 0);
INSERT INTO `sys_role_menu` VALUES (425, 1, 503, 0);
INSERT INTO `sys_role_menu` VALUES (426, 1, 600, 0);
INSERT INTO `sys_role_menu` VALUES (427, 1, 603, 0);
INSERT INTO `sys_role_menu` VALUES (428, 1, 604, 0);
INSERT INTO `sys_role_menu` VALUES (429, 1, 621, 0);
INSERT INTO `sys_role_menu` VALUES (430, 1, 622, 0);
INSERT INTO `sys_role_menu` VALUES (431, 1, 623, 0);
INSERT INTO `sys_role_menu` VALUES (432, 1, 624, 0);
INSERT INTO `sys_role_menu` VALUES (433, 1, 631, 0);
INSERT INTO `sys_role_menu` VALUES (434, 1, 632, 0);
INSERT INTO `sys_role_menu` VALUES (435, 1, 633, 0);
INSERT INTO `sys_role_menu` VALUES (436, 1, 804, 0);
INSERT INTO `sys_role_menu` VALUES (437, 1, 607, 0);

-- ----------------------------
-- Table structure for sys_store
-- ----------------------------
DROP TABLE IF EXISTS `sys_store`;
CREATE TABLE `sys_store`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '门店编号',
  `store_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '门店名称',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '门店地址',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `manager_id` bigint NULL DEFAULT NULL COMMENT '店长用户ID',
  `store_type` tinyint NOT NULL DEFAULT 1 COMMENT '门店类型：1-直营店 2-加盟店',
  `business_hours` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间（如 08:00-22:00）',
  `logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '门店Logo',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-装修中 1-营业中 2-暂停营业 3-已关闭',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_store_no`(`store_no`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_tenant_status`(`tenant_id`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '门店信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_store
-- ----------------------------
INSERT INTO `sys_store` VALUES (1, 'ST001', '阳光超市总店', 1, '北京市朝阳区建国路88号', NULL, NULL, 1, '08:00-22:00', NULL, 1, 0, NULL, '2026-09-01 23:24:50', '2026-09-01 23:24:50', NULL, NULL, 0);
-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '租户编号',
  `tenant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '租户名称（超市名称）',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人邮箱',
  `logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '企业Logo',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `license_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业执照号',
  `plan_type` tinyint NOT NULL DEFAULT 1 COMMENT '套餐类型：1-基础版 2-专业版 3-企业版',
  `max_stores` int NOT NULL DEFAULT 1 COMMENT '最大门店数',
  `max_users` int NOT NULL DEFAULT 5 COMMENT '最大用户数',
  `expire_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常 2-已到期',
  `admin_user_id` bigint NULL DEFAULT NULL COMMENT '管理员用户ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_no`(`tenant_no`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_expire_time`(`expire_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '租户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_tenant
-- ----------------------------
INSERT INTO `sys_tenant` VALUES (1, 'T001', '故辰', '管理员', '13800000000', '', NULL, '', '', 1, 100, 1000, '2099-12-31 23:59:59', 1, NULL, '', '2026-09-01 23:24:03', '2026-09-01 23:24:03', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_tenant_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_config`;
CREATE TABLE `sys_tenant_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置值（JSON格式）',
  `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'string' COMMENT '配置类型：string/number/boolean/json',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_config`(`tenant_id`, `config_key`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '租户配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_tenant_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID（0表示超级管理员）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
  `store_id` bigint NULL DEFAULT NULL COMMENT '所属门店ID',
  `user_type` tinyint NOT NULL DEFAULT 1 COMMENT '用户类型：1-普通用户 2-系统管理员 3-超级管理员',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `last_login_time` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `login_count` int NOT NULL DEFAULT 0 COMMENT '登录次数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_username`(`tenant_id`, `username`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 1, 'admin', '$2a$10$MJumaSWZiEW73NOiGiAx/.IPEGG3yTzmCbG3SXYflWAaVl8K70PVq', '故辰店长', '13800138000', NULL, NULL, 1, 1, 1, '2026-09-07 14:04:23', NULL, 68, NULL, '2026-09-01 12:07:20', '2026-09-07 14:04:23', 1, 1, 0);
INSERT INTO `sys_user` VALUES (2, 1, 'sysadmin', '$2a$10$MJumaSWZiEW73NOiGiAx/.IPEGG3yTzmCbG3SXYflWAaVl8K70PVq', '系统管理员', '13800138001', NULL, NULL, 1, 3, 1, '2026-09-07 14:04:15', NULL, 48, NULL, '2026-09-01 12:07:23', '2026-09-07 14:04:15', 1, 2, 0);
INSERT INTO `sys_user` VALUES (7, 1, 'linlin', '$2a$10$0gN0X21EgZiMEOknbIQaK.ZhbKxYx4XV./TOlC7WxsawgUZ4TiquG', '琳琳', '13999999992', NULL, NULL, NULL, 1, 1, '2026-09-06 18:37:38', NULL, 3, NULL, '2026-09-06 18:14:10', '2026-09-06 18:37:38', NULL, 7, 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 5, 6, 0);
INSERT INTO `sys_user_role` VALUES (2, 6, 7, 0);
INSERT INTO `sys_user_role` VALUES (7, 7, 3, 0);

-- ----------------------------
-- Table structure for ums_member
-- ----------------------------
DROP TABLE IF EXISTS `ums_member`;
CREATE TABLE `ums_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `member_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会员编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会员姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `level_id` bigint NULL DEFAULT NULL COMMENT '会员等级ID',
  `points` int NOT NULL DEFAULT 0 COMMENT '积分余额',
  `balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '储值余额',
  `total_consume` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '累计消费',
  `total_points` int NOT NULL DEFAULT 0 COMMENT '累计获得积分',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `register_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '注册时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_member_no`(`tenant_id`, `member_no`) USING BTREE,
  UNIQUE INDEX `uk_tenant_phone`(`tenant_id`, `phone`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_level_id`(`level_id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member
-- ----------------------------
INSERT INTO `ums_member` VALUES (1, 1, 'M1788243464892', '五六七', '13333333333', 1, NULL, 1, 0, 101.20, 8.80, 8, NULL, NULL, NULL, 1, '2026-09-01 14:17:45', NULL, '2026-09-01 14:17:45', '2026-09-04 10:26:20', 1, 1, 0);

-- ----------------------------
-- Table structure for ums_member_card
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_card`;
CREATE TABLE `ums_member_card`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `member_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会员编号',
  `card_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '卡号',
  `card_type` tinyint NOT NULL DEFAULT 1 COMMENT '卡类型：1-普通卡 2-银卡 3-金卡 4-钻石卡',
  `balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '卡余额',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-冻结 1-正常 2-已挂失',
  `expire_time` datetime(0) NULL DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_card_no`(`card_no`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员卡表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_card
-- ----------------------------

-- ----------------------------
-- Table structure for ums_member_level
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_level`;
CREATE TABLE `ums_member_level`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '等级名称',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '等级编码',
  `min_points` int NOT NULL DEFAULT 0 COMMENT '最低积分门槛',
  `discount` decimal(3, 2) NOT NULL DEFAULT 1.00 COMMENT '折扣率（0.90=9折）',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等级描述',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_min_points`(`min_points`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员等级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_level
-- ----------------------------
INSERT INTO `ums_member_level` VALUES (1, 1, '普通会员', 'NORMAL', 0, 1.00, '注册即为普通会员', 1, 1, '2026-09-01 12:07:27', '2026-09-01 12:07:27', 0);
INSERT INTO `ums_member_level` VALUES (2, 1, '银卡会员', 'SILVER', 500, 0.98, '累计500积分升级', 2, 1, '2026-09-01 12:07:27', '2026-09-01 12:07:27', 0);
INSERT INTO `ums_member_level` VALUES (3, 1, '金卡会员', 'GOLD', 2000, 0.95, '累计2000积分升级', 3, 1, '2026-09-01 12:07:27', '2026-09-01 12:07:27', 0);
INSERT INTO `ums_member_level` VALUES (4, 1, '钻石会员', 'DIAMOND', 5000, 0.90, '累计5000积分升级', 4, 1, '2026-09-01 12:07:27', '2026-09-01 12:07:27', 0);

-- ----------------------------
-- Table structure for ums_member_points_log
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_points_log`;
CREATE TABLE `ums_member_points_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `member_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会员姓名',
  `type` tinyint NOT NULL COMMENT '变动类型：1-获得 2-使用 3-调整 4-过期',
  `points` int NOT NULL COMMENT '变动积分（+获得/-使用）',
  `before_points` int NOT NULL COMMENT '变动前积分',
  `after_points` int NOT NULL COMMENT '变动后积分',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变动说明',
  `related_order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联订单号',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '积分变动日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_points_log
-- ----------------------------

-- ----------------------------
-- Table structure for ums_member_recharge_log
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_recharge_log`;
CREATE TABLE `ums_member_recharge_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `member_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会员姓名',
  `type` tinyint NOT NULL COMMENT '变动类型：1-充值 2-消费 3-退款 4-调整',
  `amount` decimal(10, 2) NOT NULL COMMENT '变动金额',
  `gift_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '赠送金额',
  `before_balance` decimal(10, 2) NOT NULL COMMENT '变动前余额',
  `after_balance` decimal(10, 2) NOT NULL COMMENT '变动后余额',
  `pay_method` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式编码',
  `related_order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联订单号',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '储值变动日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_recharge_log
-- ----------------------------
INSERT INTO `ums_member_recharge_log` VALUES (1, 1, 1, '五六七', 1, 100.00, 10.00, 0.00, 110.00, 'wechat', NULL, 1, 'admin', '', '2026-09-04 09:55:18');

-- ----------------------------
-- Table structure for wms_purchase_in
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_in`;
CREATE TABLE `wms_purchase_in`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `in_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '入库单号',
  `supplier_id` bigint NULL DEFAULT NULL COMMENT '供应商ID',
  `store_id` bigint NOT NULL COMMENT '入库门店ID',
  `total_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
  `total_quantity` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '总数量',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已审核 2-已入库',
  `in_time` datetime(0) NULL DEFAULT NULL COMMENT '入库时间',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `review_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核备注',
  `stock_in_user_id` bigint NULL DEFAULT NULL COMMENT '入库人ID',
  `stock_in_time` datetime(0) NULL DEFAULT NULL COMMENT '入库时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_in_no`(`tenant_id`, `in_no`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_supplier_id`(`supplier_id`) USING BTREE,
  INDEX `idx_tenant_status`(`tenant_id`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '采购入库单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wms_purchase_in
-- ----------------------------
INSERT INTO `wms_purchase_in` VALUES (5, 1, 'PI1788482855482', 1, 1, 400.00, 150.00, 2, NULL, 1, 1, '2026-09-04 08:56:07', NULL, 1, '2026-09-04 08:57:20', NULL, '2026-09-04 08:47:35', '2026-09-04 08:47:35', NULL, NULL, 0);

-- ----------------------------
-- Table structure for wms_purchase_in_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_in_item`;
CREATE TABLE `wms_purchase_in_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `in_id` bigint NOT NULL COMMENT '入库单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` decimal(10, 2) NOT NULL COMMENT '入库数量',
  `purchase_price` decimal(10, 2) NOT NULL COMMENT '进货单价',
  `total_price` decimal(12, 2) NOT NULL COMMENT '小计金额',
  `batch_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '批次号',
  `expire_time` date NULL DEFAULT NULL COMMENT '过期日期',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_in_id`(`in_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '采购入库明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wms_purchase_in_item
-- ----------------------------
INSERT INTO `wms_purchase_in_item` VALUES (13, 5, 29, 100.00, 1.30, 130.00, NULL, NULL, NULL, 0);
INSERT INTO `wms_purchase_in_item` VALUES (14, 5, 28, 50.00, 5.40, 270.00, NULL, NULL, NULL, 0);

-- ----------------------------
-- Table structure for wms_stock
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock`;
CREATE TABLE `wms_stock`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `warehouse_quantity` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '仓库库存',
  `shelf_quantity` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '货架库存',
  `avg_cost` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '平均成本价',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_product_store`(`tenant_id`, `product_id`, `store_id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wms_stock
-- ----------------------------
INSERT INTO `wms_stock` VALUES (9, 1, 1, 29, 50.00, 49.00, 1.30, '2026-09-04 08:57:20', '2026-09-04 09:37:07', 0);
INSERT INTO `wms_stock` VALUES (10, 1, 1, 28, 30.00, 19.00, 5.40, '2026-09-04 08:57:20', '2026-09-04 10:26:20', 0);

-- ----------------------------
-- Table structure for wms_stock_check
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_check`;
CREATE TABLE `wms_stock_check`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `check_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '盘点单号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待盘点 1-盘点中 2-已完成 3-已取消',
  `total_system` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '系统总数量',
  `total_actual` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '实际总数量',
  `total_diff` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '差异总数量',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '盘点人ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_check_no`(`tenant_id`, `check_no`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '库存盘点单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wms_stock_check
-- ----------------------------

-- ----------------------------
-- Table structure for wms_stock_check_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_check_item`;
CREATE TABLE `wms_stock_check_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `check_id` bigint NOT NULL COMMENT '盘点单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `system_stock` decimal(10, 2) NOT NULL COMMENT '系统库存',
  `actual_stock` decimal(10, 2) NOT NULL COMMENT '实际库存',
  `difference` decimal(10, 2) NOT NULL COMMENT '差异（实际-系统）',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_check_id`(`check_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '盘点明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wms_stock_check_item
-- ----------------------------

-- ----------------------------
-- Table structure for wms_stock_log
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_log`;
CREATE TABLE `wms_stock_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `log_type` tinyint NOT NULL COMMENT '变动类型：1-采购入库 2-销售出库 3-调拨 4-报损 5-盘点调整 6-退货',
  `quantity` decimal(10, 2) NOT NULL COMMENT '变动数量（+入库/-出库）',
  `before_stock` decimal(10, 2) NOT NULL COMMENT '变动前库存',
  `after_stock` decimal(10, 2) NOT NULL COMMENT '变动后库存',
  `related_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联单据号',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联单据ID',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_log_type`(`log_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '库存变动日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wms_stock_log
-- ----------------------------
INSERT INTO `wms_stock_log` VALUES (46, 1, 1, 29, 1, 100.00, 0.00, 100.00, 'PI1788482855482', 5, 1, NULL, '2026-09-04 08:57:20', 0);
INSERT INTO `wms_stock_log` VALUES (47, 1, 1, 28, 1, 50.00, 0.00, 50.00, 'PI1788482855482', 5, 1, NULL, '2026-09-04 08:57:20', 0);
INSERT INTO `wms_stock_log` VALUES (48, 1, 1, 29, 4, 50.00, 100.00, 50.00, 'SO1788484140318', 2, 1, NULL, '2026-09-04 09:15:27', 0);
INSERT INTO `wms_stock_log` VALUES (49, 1, 1, 28, 4, 20.00, 50.00, 30.00, 'SO1788484140318', 2, 1, NULL, '2026-09-04 09:15:27', 0);
INSERT INTO `wms_stock_log` VALUES (50, 1, 1, 29, 4, 1.00, 50.00, 49.00, NULL, 17, 1, NULL, '2026-09-04 09:37:07', 0);
INSERT INTO `wms_stock_log` VALUES (53, 1, 1, 28, 4, 1.00, 20.00, 19.00, NULL, 21, 1, NULL, '2026-09-04 10:26:20', 0);

-- ----------------------------
-- Table structure for wms_stock_out
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_out`;
CREATE TABLE `wms_stock_out`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `out_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '出库单号',
  `out_type` tinyint NOT NULL COMMENT '出库类型：1-销售出库 2-调拨出库 3-报损出库 4-其他',
  `store_id` bigint NOT NULL COMMENT '出库门店ID',
  `target_store_id` bigint NULL DEFAULT NULL COMMENT '目标门店ID（调拨出库时）',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID（销售出库时）',
  `total_quantity` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '总出库数量',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待出库 1-已出库',
  `out_time` datetime(0) NULL DEFAULT NULL COMMENT '出库时间',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `stock_out_user_id` bigint NULL DEFAULT NULL COMMENT '出库人ID',
  `stock_out_time` datetime(0) NULL DEFAULT NULL COMMENT '出库时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_out_no`(`tenant_id`, `out_no`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE,
  INDEX `idx_store_id`(`store_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '出库单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wms_stock_out
-- ----------------------------
INSERT INTO `wms_stock_out` VALUES (2, 1, 'SO1788484140318', 1, 1, NULL, NULL, 70.00, 1, NULL, 1, NULL, NULL, 1, '2026-09-04 09:15:27', NULL, '2026-09-04 09:09:00', '2026-09-04 09:09:00', NULL, NULL, 0);
INSERT INTO `wms_stock_out` VALUES (3, 1, 'SO1788484548870', 1, 1, NULL, NULL, 1.00, 0, NULL, 1, NULL, NULL, NULL, NULL, NULL, '2026-09-04 09:15:49', '2026-09-04 09:15:49', NULL, NULL, 0);

-- ----------------------------
-- Table structure for wms_stock_out_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_out_item`;
CREATE TABLE `wms_stock_out_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `out_id` bigint NOT NULL COMMENT '出库单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` decimal(10, 2) NOT NULL COMMENT '出库数量',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_out_id`(`out_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '出库明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wms_stock_out_item
-- ----------------------------
INSERT INTO `wms_stock_out_item` VALUES (5, 2, 29, 50.00, NULL, 0);
INSERT INTO `wms_stock_out_item` VALUES (6, 2, 28, 20.00, NULL, 0);
INSERT INTO `wms_stock_out_item` VALUES (7, 3, 29, 1.00, NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;
