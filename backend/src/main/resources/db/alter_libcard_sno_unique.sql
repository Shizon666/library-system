-- 同一学号可有多张借书证（历史证号保留），仅保留 cardNo 主键唯一
ALTER TABLE libcard DROP INDEX sno;
