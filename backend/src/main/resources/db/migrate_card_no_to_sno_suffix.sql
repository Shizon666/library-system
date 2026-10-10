-- 将旧证号（如 C2024001）统一为「学号+三位序号」（如 2024101001001）
-- 按 cardrec 中新办/补办时间顺序为同一学号的证分配 001、002…
-- 执行前请备份 library_db

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 旧证号 -> 新证号（按办证先后顺序）
CREATE TEMPORARY TABLE card_no_mapping (
    old_card_no VARCHAR(30) PRIMARY KEY,
    new_card_no VARCHAR(30) NOT NULL,
    sno         VARCHAR(30) NOT NULL
);

INSERT INTO card_no_mapping (old_card_no, new_card_no, sno) VALUES
('C2021001', '2021101001001', '2021101001'),
('C2021002', '2021201001001', '2021201001'),
('C2023001', '2023201001001', '2023201001'),
('C2023002', '2023202001001', '2023202001'),
('C2023003', '2023202001002', '2023202001'),
('C2023004', '2023206001001', '2023206001'),
('C2023005', '2023104001001', '2023104001'),
('C2024001', '2024101001001', '2024101001'),
('C2024002', '2024102001001', '2024102001'),
('C2024003', '2024103001001', '2024103001'),
('C2024004', '2024105001001', '2024105001'),
('C2024005', '2024107001001', '2024107001'),
('C2025001', '2025104001001', '2025104001'),
('C2025002', '2025102001001', '2025102001');

UPDATE finePayment fp
    INNER JOIN card_no_mapping m ON fp.cardNo = m.old_card_no
SET fp.cardNo = m.new_card_no;

UPDATE cardrec cr
    INNER JOIN card_no_mapping m ON cr.originCardNo = m.old_card_no
SET cr.originCardNo = m.new_card_no;

UPDATE cardrec cr
    INNER JOIN card_no_mapping m ON cr.newCardNo = m.old_card_no
SET cr.newCardNo = m.new_card_no;

UPDATE libcard lc
    INNER JOIN card_no_mapping m ON lc.cardNo = m.old_card_no
SET lc.cardNo = m.new_card_no;

DROP TEMPORARY TABLE card_no_mapping;

SET FOREIGN_KEY_CHECKS = 1;
