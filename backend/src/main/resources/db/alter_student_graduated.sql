-- ============================================================
-- 迁移脚本：为 student 表增加 graduated（是否毕业）字段
-- 执行方式：在 MySQL 客户端中运行本文件
-- ============================================================

USE library;

-- 添加 graduated 字段（默认 0 = 未毕业）
ALTER TABLE student
    ADD COLUMN graduated tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否毕业：0未毕业 1已毕业'
    AFTER password;

-- 验证
SELECT sno, username, type, graduated FROM student;

-- 如需手动设置某学生为已毕业：
-- UPDATE student SET graduated = 1 WHERE sno = '2021101001';
