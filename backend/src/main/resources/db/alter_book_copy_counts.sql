-- 在 book 表增加册数统计字段（请在 library_db 中执行一次）

ALTER TABLE book
    ADD COLUMN totalCopies INT NOT NULL DEFAULT 0 COMMENT '馆藏总册数' AFTER status,
    ADD COLUMN borrowedCopies INT NOT NULL DEFAULT 0 COMMENT '已借出册数' AFTER totalCopies,
    ADD COLUMN availableCopies INT NOT NULL DEFAULT 0 COMMENT '可借册数（未借出）' AFTER borrowedCopies;

-- 按 bookcopy 现有数据回填
UPDATE book b
SET
    totalCopies = (SELECT COUNT(*) FROM bookcopy bc WHERE bc.bookISBN = b.ISBN),
    borrowedCopies = (SELECT COUNT(*) FROM bookcopy bc WHERE bc.bookISBN = b.ISBN AND bc.status = 2),
    availableCopies = (SELECT COUNT(*) FROM bookcopy bc WHERE bc.bookISBN = b.ISBN AND bc.status = 1);
