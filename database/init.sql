-- 创建数据库
CREATE DATABASE IF NOT EXISTS property_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE property_service;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `role` varchar(20) NOT NULL DEFAULT 'RESIDENT' COMMENT '角色：RESIDENT-居民, PROPERTY-物业人员, ADMIN-管理员',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 房屋信息表
DROP TABLE IF EXISTS `house`;
CREATE TABLE `house` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '房屋ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `building_no` varchar(20) NOT NULL COMMENT '楼栋号',
  `unit_no` varchar(20) DEFAULT NULL COMMENT '单元号',
  `room_no` varchar(20) NOT NULL COMMENT '房间号',
  `area` decimal(10,2) DEFAULT NULL COMMENT '房屋面积',
  `type` varchar(20) DEFAULT NULL COMMENT '房屋类型：住宅、商铺、车库',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-空置, 1-已入住, 2-已出租',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_building_room` (`building_no`, `unit_no`, `room_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房屋信息表';

-- 物业报修表
DROP TABLE IF EXISTS `repair`;
CREATE TABLE `repair` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '报修ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(100) NOT NULL COMMENT '报修标题',
  `content` text NOT NULL COMMENT '报修内容',
  `type` varchar(50) NOT NULL COMMENT '报修类型：水电维修、家电维修、管道疏通、房屋维修、其他',
  `images` text DEFAULT NULL COMMENT '报修图片（JSON数组）',
  `contact_name` varchar(50) NOT NULL COMMENT '联系人',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `address` varchar(200) NOT NULL COMMENT '维修地址',
  `preferred_time` datetime DEFAULT NULL COMMENT '期望维修时间',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理, PROCESSING-处理中, COMPLETED-已完成, CANCELLED-已取消',
  `staff_id` bigint(20) DEFAULT NULL COMMENT '处理人员ID',
  `staff_name` varchar(50) DEFAULT NULL COMMENT '处理人员姓名',
  `staff_phone` varchar(20) DEFAULT NULL COMMENT '处理人员电话',
  `process_content` text DEFAULT NULL COMMENT '处理内容',
  `process_time` datetime DEFAULT NULL COMMENT '处理时间',
  `rating` tinyint(1) DEFAULT NULL COMMENT '评分：1-5星',
  `comment` text DEFAULT NULL COMMENT '评价',
  `comment_time` datetime DEFAULT NULL COMMENT '评价时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_staff_id` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物业报修表';

-- 访客预约表
DROP TABLE IF EXISTS `visitor`;
CREATE TABLE `visitor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '访客预约ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID（预约人）',
  `visitor_name` varchar(50) NOT NULL COMMENT '访客姓名',
  `visitor_phone` varchar(20) NOT NULL COMMENT '访客电话',
  `visitor_id_card` varchar(18) DEFAULT NULL COMMENT '访客身份证号',
  `visitor_count` int(11) DEFAULT 1 COMMENT '访客人数',
  `visit_reason` varchar(200) DEFAULT NULL COMMENT '来访事由',
  `visit_address` varchar(200) NOT NULL COMMENT '访问地址',
  `visit_time` datetime NOT NULL COMMENT '来访时间',
  `leave_time` datetime DEFAULT NULL COMMENT '离开时间',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝, CHECKED_IN-已签到, CHECKED_OUT-已签退, CANCELLED-已取消',
  `audit_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `audit_name` varchar(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_comment` varchar(200) DEFAULT NULL COMMENT '审核意见',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_visitor_phone` (`visitor_phone`),
  KEY `idx_status` (`status`),
  KEY `idx_visit_time` (`visit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客预约表';

-- 费用类型表
DROP TABLE IF EXISTS `fee_type`;
CREATE TABLE `fee_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '费用类型ID',
  `type_name` varchar(50) NOT NULL COMMENT '费用类型名称：物业费、水电费、停车费、维修费、其他',
  `unit` varchar(20) DEFAULT NULL COMMENT '计费单位：元/月、元/度、元/吨、元/次',
  `price` decimal(10,2) DEFAULT NULL COMMENT '单价',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用类型表';

-- 费用账单表
DROP TABLE IF EXISTS `fee_bill`;
CREATE TABLE `fee_bill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '账单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `house_id` bigint(20) DEFAULT NULL COMMENT '房屋ID',
  `fee_type_id` bigint(20) NOT NULL COMMENT '费用类型ID',
  `fee_type_name` varchar(50) NOT NULL COMMENT '费用类型名称',
  `bill_no` varchar(50) NOT NULL COMMENT '账单编号',
  `bill_month` varchar(20) DEFAULT NULL COMMENT '账单月份：2024-01',
  `quantity` decimal(10,2) DEFAULT NULL COMMENT '数量（如：用水量、用电量、房屋面积）',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '单价',
  `amount` decimal(10,2) NOT NULL COMMENT '总金额',
  `late_fee` decimal(10,2) DEFAULT 0.00 COMMENT '滞纳金',
  `total_amount` decimal(10,2) NOT NULL COMMENT '应付总额',
  `payable_date` date DEFAULT NULL COMMENT '应缴日期',
  `status` varchar(20) DEFAULT 'UNPAID' COMMENT '状态：UNPAID-未缴费, PARTIAL-部分缴费, PAID-已缴费, OVERDUE-已逾期',
  `paid_amount` decimal(10,2) DEFAULT 0.00 COMMENT '已缴金额',
  `paid_time` datetime DEFAULT NULL COMMENT '缴费时间',
  `payment_method` varchar(50) DEFAULT NULL COMMENT '支付方式：微信支付、支付宝、现金、银行卡',
  `transaction_no` varchar(100) DEFAULT NULL COMMENT '交易流水号',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bill_no` (`bill_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_house_id` (`house_id`),
  KEY `idx_status` (`status`),
  KEY `idx_bill_month` (`bill_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用账单表';

-- 缴费记录表
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `bill_id` bigint(20) NOT NULL COMMENT '账单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '缴费金额',
  `payment_method` varchar(50) NOT NULL COMMENT '支付方式',
  `transaction_no` varchar(100) NOT NULL COMMENT '交易流水号',
  `status` varchar(20) DEFAULT 'SUCCESS' COMMENT '状态：PENDING-处理中, SUCCESS-成功, FAILED-失败',
  `payment_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '缴费时间',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_no` (`transaction_no`),
  KEY `idx_bill_id` (`bill_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_payment_time` (`payment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费记录表';

-- 公告通知表
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(100) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `type` varchar(50) DEFAULT 'GENERAL' COMMENT '公告类型：GENERAL-一般通知, IMPORTANT-重要通知, EMERGENCY-紧急通知, ACTIVITY-活动通知',
  `publisher_id` bigint(20) NOT NULL COMMENT '发布人ID',
  `publisher_name` varchar(50) NOT NULL COMMENT '发布人姓名',
  `is_top` tinyint(1) DEFAULT 0 COMMENT '是否置顶：0-否, 1-是',
  `start_time` datetime DEFAULT NULL COMMENT '显示开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '显示结束时间',
  `view_count` int(11) DEFAULT 0 COMMENT '浏览次数',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-草稿, 1-已发布',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告通知表';

-- 公告阅读记录表
DROP TABLE IF EXISTS `notice_read`;
CREATE TABLE `notice_read` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `notice_id` bigint(20) NOT NULL COMMENT '公告ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `read_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`),
  KEY `idx_notice_id` (`notice_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告阅读记录表';

-- 初始化数据：插入默认管理员账号（密码：admin123）
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `role`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '系统管理员', '13800138000', 'ADMIN', 1);

-- 初始化费用类型
INSERT INTO `fee_type` (`type_name`, `unit`, `price`, `description`, `status`)
VALUES 
('物业费', '元/平方米/月', 2.5, '物业管理服务费，按房屋面积收取', 1),
('水费', '元/吨', 3.5, '居民生活用水费', 1),
('电费', '元/度', 0.6, '居民生活用电费', 1),
('停车费', '元/月', 200.00, '小区停车月租费', 1),
('维修费', '元/次', NULL, '上门维修服务费，按实际情况收取', 1);

-- 初始化物业人员账号（密码：123456）
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `role`, `status`)
VALUES 
('property01', '$2a$10$Eqo1rWq1.2Fk4t9Yj3k5L.mQ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '张物业', '13900139001', 'PROPERTY', 1),
('property02', '$2a$10$Eqo1rWq1.2Fk4t9Yj3k5L.mQ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '李维修', '13900139002', 'PROPERTY', 1);

-- 初始化居民测试账号（密码：123456）
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `id_card`, `role`, `status`)
VALUES 
('resident01', '$2a$10$Eqo1rWq1.2Fk4t9Yj3k5L.mQ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '王居民', '13800138001', '110101199001011234', 'RESIDENT', 1),
('resident02', '$2a$10$Eqo1rWq1.2Fk4t9Yj3k5L.mQ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '赵居民', '13800138002', '110101199002022345', 'RESIDENT', 1);

-- 初始化房屋信息
INSERT INTO `house` (`user_id`, `building_no`, `unit_no`, `room_no`, `area`, `type`, `status`)
VALUES 
(4, '1号楼', '1单元', '101室', 100.50, '住宅', 1),
(4, '1号楼', '1单元', '102室', 85.00, '住宅', 1),
(5, '2号楼', '2单元', '301室', 120.00, '住宅', 1);

-- 初始化测试公告
INSERT INTO `notice` (`title`, `content`, `type`, `publisher_id`, `publisher_name`, `is_top`, `status`)
VALUES 
('关于小区春节期间安全管理的通知', '尊敬的各位业主：春节将至，请各位业主注意关好门窗，做好防火防盗措施。物业值班电话：12345678。祝大家春节快乐！', 'IMPORTANT', 1, '系统管理员', 1, 1),
('关于小区停水检修的通知', '因水管检修，本周六（1月15日）上午8:00-12:00将临时停水，请各位业主提前做好储水准备。', 'EMERGENCY', 2, '张物业', 0, 1),
('小区业主春节联欢会通知', '为丰富业主文化生活，小区将于春节期间举办业主联欢会，欢迎各位业主积极报名参加。报名电话：12345678。', 'ACTIVITY', 2, '张物业', 0, 1);