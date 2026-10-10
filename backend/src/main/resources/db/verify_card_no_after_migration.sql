-- ============================================================
-- 迁移后验证：执行 migrate_card_no_to_sno_suffix.sql 后运行
-- 期望：以下查询均无结果（或 unmapped 为空）
-- ============================================================

-- 1) 不应再存在 C 开头的证号
SELECT '仍有旧格式证号' AS problem, card_no
FROM (
    SELECT cardNo AS card_no FROM libcard WHERE cardNo LIKE 'C%'
    UNION SELECT newCardNo FROM cardrec WHERE newCardNo LIKE 'C%'
    UNION SELECT originCardNo FROM cardrec WHERE originCardNo LIKE 'C%'
    UNION SELECT cardNo FROM finePayment WHERE cardNo LIKE 'C%'
) t;

-- 2) 新证号格式：学号(10位) + 三位数字
SELECT cardNo, sno,
       CASE
           WHEN cardNo <> CONCAT(sno, RIGHT(cardNo, 3)) THEN '证号不以学号开头'
           WHEN LENGTH(cardNo) <> LENGTH(sno) + 3 THEN '长度应为学号+3位'
           WHEN RIGHT(cardNo, 3) NOT REGEXP '^[0-9]{3}$' THEN '末三位非数字'
           ELSE 'OK'
       END AS check_result
FROM libcard
HAVING check_result <> 'OK';

-- 3) libcard 与 cardrec 抽样对照（新办记录）
SELECT cr.serNum, cr.sno, cr.newCardNo, lc.cardNo,
       CASE WHEN cr.newCardNo = lc.cardNo OR lc.cardNo IS NULL THEN 'OK' ELSE '不一致' END AS match_flag
FROM cardrec cr
LEFT JOIN libcard lc ON lc.sno = cr.sno AND lc.status = 1
WHERE cr.opType = 1
ORDER BY cr.serNum;

-- 4) 完整 cardrec（人工目视）
SELECT serNum, sno, originCardNo, newCardNo, opType, opTime FROM cardrec ORDER BY serNum;
