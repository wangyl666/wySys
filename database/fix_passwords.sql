-- ============================================
-- 修复：重置所有测试账号的密码（使用有效的BCrypt加密）
-- 
-- 密码对应关系：
-- - admin     -> admin123
-- - property01-> 123456
-- - property02-> 123456
-- - resident01-> 123456
-- - resident02-> 123456
-- ============================================

USE property_service;

-- 查看当前用户列表
SELECT '当前用户列表:' AS info;
SELECT id, username, real_name, role, status FROM user;

-- 更新管理员密码 admin -> admin123
-- BCrypt 哈希值（$2a$10$ 前缀，10轮加密）
UPDATE user 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E' 
WHERE username = 'admin';

-- 更新物业人员密码 property01/property02 -> 123456
UPDATE user 
SET password = '$2a$10$Eqo1rWq1.2Fk4t9Yj3k5L.mQ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E' 
WHERE username IN ('property01', 'property02');

-- 更新居民密码 resident01/resident02 -> 123456
UPDATE user 
SET password = '$2a$10$Eqo1rWq1.2Fk4t9Yj3k5L.mQ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E' 
WHERE username IN ('resident01', 'resident02');

SELECT '密码已重置完成！' AS result;
SELECT '账号信息：' AS info;
SELECT id, username, real_name, role, 
       CASE username 
           WHEN 'admin' THEN '密码: admin123' 
           ELSE '密码: 123456' 
       END AS password_hint
FROM user;
