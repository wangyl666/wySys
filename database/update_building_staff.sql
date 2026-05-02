-- 楼栋管理表
DROP TABLE IF EXISTS `building`;
CREATE TABLE `building` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '楼栋ID',
  `building_no` varchar(20) NOT NULL COMMENT '楼栋号（如：1号楼、A栋）',
  `building_name` varchar(100) DEFAULT NULL COMMENT '楼栋名称（如：凌云阁）',
  `total_floors` int(11) DEFAULT NULL COMMENT '总层数',
  `units_per_floor` int(11) DEFAULT NULL COMMENT '每层单元数',
  `total_houses` int(11) DEFAULT 0 COMMENT '总房屋数',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-停用, 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_building_no` (`building_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼栋管理表';

-- 楼栋-物业人员分配表
DROP TABLE IF EXISTS `building_staff`;
CREATE TABLE `building_staff` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `building_id` bigint(20) NOT NULL COMMENT '楼栋ID',
  `building_no` varchar(20) NOT NULL COMMENT '楼栋号',
  `staff_id` bigint(20) NOT NULL COMMENT '物业人员ID',
  `staff_name` varchar(50) DEFAULT NULL COMMENT '物业人员姓名',
  `assign_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_building_staff` (`building_id`, `staff_id`),
  KEY `idx_staff_id` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼栋-物业人员分配表';

-- 初始化楼栋数据（基于现有房屋数据）
INSERT INTO `building` (`building_no`, `building_name`, `total_floors`, `units_per_floor`, `total_houses`, `description`, `status`)
VALUES 
('1号楼', '春风阁', 18, 2, 72, '住宅楼栋，共18层', 1),
('2号楼', '夏雨阁', 18, 2, 72, '住宅楼栋，共18层', 1),
('3号楼', '秋韵阁', 25, 3, 150, '住宅楼栋，共25层', 1),
('4号楼', '冬暖阁', 25, 3, 150, '住宅楼栋，共25层', 1),
('A栋', '商务楼A', 12, 4, 96, '商业办公楼', 1),
('B栋', '商务楼B', 12, 4, 96, '商业办公楼', 1);

-- 将现有楼栋分配给物业人员
-- property01 (张物业) 分配 1号楼、A栋
INSERT INTO `building_staff` (`building_id`, `building_no`, `staff_id`, `staff_name`)
VALUES 
(1, '1号楼', 2, '张物业'),
(5, 'A栋', 2, '张物业');

-- property02 (李维修) 分配 2号楼、B栋
INSERT INTO `building_staff` (`building_id`, `building_no`, `staff_id`, `staff_name`)
VALUES 
(2, '2号楼', 3, '李维修'),
(6, 'B栋', 3, '李维修');

-- 更新房屋表，确保与楼栋分配一致
-- resident01 (王居民) 的 1号楼 房屋归张物业管理
-- resident02 (赵居民) 的 2号楼 房屋归李维修管理
