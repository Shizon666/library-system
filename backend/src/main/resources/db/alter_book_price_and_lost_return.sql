-- 图书表增加定价；借阅记录增加「挂失还」状态
-- 在 Navicat 中选中 library 库后运行；若 price 列已存在会报错，可忽略
USE library;

ALTER TABLE book ADD COLUMN price DECIMAL(10, 2) NOT NULL DEFAULT 50.00 COMMENT '图书定价' AFTER bookStatus;

UPDATE book SET price = 89.00 WHERE ISBN = '9787111680011';
UPDATE book SET price = 45.00 WHERE ISBN = '9787302512345';

ALTER TABLE borrowrec MODIFY COLUMN retStatus
  ENUM('按时还','超时还','未归还','挂失还') NOT NULL DEFAULT '未归还' COMMENT '还书状态';
