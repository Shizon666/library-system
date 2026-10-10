-- 问题修改清单数据库迁移（在 library_db 中执行一次）

-- DB-01: bookcopy 被借次数
ALTER TABLE bookcopy
    ADD COLUMN borrowCount INT NOT NULL DEFAULT 0 COMMENT '被借次数' AFTER status;

-- DB-02: 电话唯一
ALTER TABLE student ADD CONSTRAINT uk_student_phone UNIQUE (phone);

-- DB-03: libcard 扩展字段（可选）
ALTER TABLE libcard ADD COLUMN issueDate DATE COMMENT '发证日期';
ALTER TABLE libcard ADD COLUMN expireDate DATE COMMENT '有效期至';
ALTER TABLE libcard ADD COLUMN operator VARCHAR(50) COMMENT '办证操作员';
