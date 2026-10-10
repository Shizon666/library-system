-- ============================================================
-- 迁移前核对：在 Navicat 对 192.168.110.191 的 library_db 执行
-- 将查询结果与下方「标准映射表」对比，不一致则需改 migrate 脚本
-- ============================================================

-- 1) 当前 libcard 全部证号
SELECT 'libcard' AS src, cardNo AS old_card_no, sno, status FROM libcard ORDER BY sno, cardNo;

-- 2) cardrec 中出现过的所有证号（去重）
SELECT 'cardrec' AS src, card_no, sno
FROM (
    SELECT newCardNo AS card_no, sno FROM cardrec WHERE newCardNo IS NOT NULL
    UNION
    SELECT originCardNo AS card_no, sno FROM cardrec WHERE originCardNo IS NOT NULL
) t
ORDER BY sno, card_no;

-- 3) finePayment 中的证号
SELECT 'finePayment' AS src, cardNo AS old_card_no, sno FROM finePayment ORDER BY sno, cardNo;

-- 4) 检查是否还有脚本未覆盖的 C 开头证号
SELECT card_no AS unmapped_card_no, '出现在 cardrec/libcard/finePayment，但不在映射表' AS note
FROM (
    SELECT cardNo AS card_no FROM libcard WHERE cardNo LIKE 'C%'
    UNION
    SELECT newCardNo FROM cardrec WHERE newCardNo LIKE 'C%'
    UNION
    SELECT originCardNo FROM cardrec WHERE originCardNo LIKE 'C%'
    UNION
    SELECT cardNo FROM finePayment WHERE cardNo LIKE 'C%'
) u
WHERE card_no NOT IN (
    'C2021001','C2021002','C2023001','C2023002','C2023003','C2023004','C2023005',
    'C2024001','C2024002','C2024003','C2024004','C2024005','C2025001','C2025002'
);

-- 若上一句有结果行 → 请把多出来的旧证号补进 migrate_card_no_to_sno_suffix.sql
