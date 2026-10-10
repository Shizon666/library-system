/*
 Navicat MySQL Data Transfer

 Source Server         : MySQL8pwd123456
 Source Server Type    : MySQL
 Source Server Version : 80017
 Source Host           : localhost:3307
 Source Schema         : xuanfa

 Target Server Type    : MySQL
 Target Server Version : 80017
 File Encoding         : 65001

 Date: 08/06/2026 09:37:09
*/

-- 设置客户端字符集为 utf8mb4，支持完整 Unicode（含 emoji 等四字节字符）
SET NAMES utf8mb4;
-- 临时关闭外键检查，避免建表/删表时因顺序问题报错
SET FOREIGN_KEY_CHECKS = 0;

-- ================================================================
-- 数据库全表关系概览（ER 简图）
-- ================================================================
--   admin             独立表，存放管理员账号，adminType 区分三种后台角色
--
--   student(sno)      --1:N-->  libcard(sno)       一个学生可持有多张借书证（挂失/补办场景）
--   student(sno)      --1:N-->  borrowrec(sno)     一个学生有多条借阅记录
--   book(ISBN)        --1:N-->  bookcopy(ISBN)     一种图书对应多个馆藏副本
--   bookcopy(barCode) <--N:1--  borrowrec(barCode) 每条借阅记录对应一个副本
--   payrec(serNum)    --1:N-->  borrowrec(paySer)  付款记录通过 paySerNum 与借阅记录关联
--   cardrec(sno)      --1:N-->  libcard(cardNo)    借书证操作记录追溯办卡/挂失/补办/注销历史
--   notice            独立表，存放图书馆公告，由管理员发布
-- ================================================================


-- ================================================================
-- admin：管理员表
-- ================================================================
-- 角色：系统后台用户，三种角色分别负责不同业务模块
--   adminType=0 证件管理员 → 负责借书证办理/挂失/补办/注销
--   adminType=1 采编管理员 → 负责图书入库/上架/下架/注销
--   adminType=2 流通管理员 → 负责借还书/罚款/公告发布
-- 与其他表无外键关联，属于独立的权限控制表
-- ================================================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录用户名',
  `password` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录密码（明文存储，仅作教学演示）',
  `adminType` int(11) NULL DEFAULT 1 COMMENT '管理员角色类型：0-证件管理 1-采编 2-流通',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '管理员表' ROW_FORMAT = DYNAMIC;


-- ================================================================
-- book：图书信息表（品种级别）
-- ================================================================
-- 角色：记录图书馆馆藏的每一种图书的元数据信息
-- 与 bookcopy 为 1:N 关系：一种书可有多个物理副本（同 ISBN 不同条码）
-- bookStatus 控制该品种是否对读者可见（上架/下架）
-- 采编管理员通过此表管理图书的增删改和上下架
-- ================================================================
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键，内部使用',
  `ISBN` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '国际标准书号，同一品种唯一',
  `bname` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '书名',
  `author` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '作者（多位作者用逗号分隔）',
  `publisher` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '出版社名称',
  `introduction` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '图书简介/内容提要',
  `pubDate` date NOT NULL COMMENT '出版日期',
  `clcNum` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '中国图书馆分类号（如 TP312 表示程序语言）',
  `bookStatus` int(11) NOT NULL DEFAULT 1 COMMENT '品种状态：1-上架（读者可见可借） 0-下架（隐藏）',
  `price` decimal(10, 2) NOT NULL DEFAULT 50.00 COMMENT '图书定价（元），用于挂失赔偿金额计算',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '图书品种信息表' ROW_FORMAT = DYNAMIC;


-- ================================================================
-- bookcopy：图书副本表（物理实体级别）
-- ================================================================
-- 角色：记录每本图书的物理副本，是借还操作的最小单位
-- 与 book 为 N:1 关系：通过 ISBN 关联到图书品种
-- 与 borrowrec 为 1:N 关系：一个副本可被多次借还
-- status 字段是借还流程的核心状态机：可借(1) ↔ 借出(0)，注销(2) 为终态
-- oldStatus 仅在注销时使用，记录注销前的可借状态以便恢复
-- ================================================================
DROP TABLE IF EXISTS `bookcopy`;
CREATE TABLE `bookcopy`  (
  `barCode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '馆藏条码号，全馆唯一标识一个物理副本',
  `ISBN` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所属图书品种的 ISBN，外键关联 book 表',
  `place` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '馆藏位置（区-架-层），如 A区-01架-01层',
  `status` int(1) NOT NULL DEFAULT 1 COMMENT '副本状态：1-可借 0-已借出 2-已注销',
  `oldStatus` int(11) NULL DEFAULT NULL COMMENT '注销前状态（注销时置为1，用于恢复场景）',
  PRIMARY KEY (`barCode`) USING BTREE,
  INDEX `ISBN`(`ISBN` ASC) USING BTREE   -- 按 ISBN 建索引，加速按品种查副本
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '图书副本信息表' ROW_FORMAT = DYNAMIC;


-- ================================================================
-- borrowrec：借阅记录表
-- ================================================================
-- 角色：记录每一次借书和还书的完整生命周期，是流通业务的核心表
-- 与 student 为 N:1 关系：通过 sno 关联借书学生
-- 与 bookcopy 为 N:1 关系：通过 barCode 关联具体副本
-- 与 payrec 为 N:1 关系（可选）：通过 paySerNum 关联罚款付款记录
-- retStatus 四种终态：未归还(在借中)、按时还、超时还(产生罚款)、挂失还(按定价赔偿)
-- fineStatus 记录罚款缴纳情况，与 payrec 表配合完成罚款闭环
-- ================================================================
DROP TABLE IF EXISTS `borrowrec`;
CREATE TABLE `borrowrec`  (
  `serNum` int(11) NOT NULL AUTO_INCREMENT COMMENT '借阅流水号，全局唯一',
  `sno` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '借书学生学号，外键关联 student 表',
  `barCode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所借副本条码号，外键关联 bookcopy 表',
  `borDate` date NOT NULL COMMENT '借书日期',
  `retDate` date NULL DEFAULT NULL COMMENT '应还日期（借书时自动计算，通常为借期30天后）',
  `realRetDate` date NULL DEFAULT NULL COMMENT '实际归还日期（在借时为 NULL）',
  `retStatus` enum('按时还','超时还','未归还','挂失还') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '未归还' COMMENT '还书状态：未归还-在借中 按时还-正常 超时还-逾期 挂失还-遗失赔偿',
  `ovdDays` int(11) NULL DEFAULT 0 COMMENT '超期天数（按时还/在借时为0）',
  `fineMoney` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '罚款金额（元）：超期按1元/天，挂失按定价',
  `fineStatus` enum('已缴','未缴') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '未缴' COMMENT '罚款缴纳状态：已缴-已付款 未缴-待缴纳',
  `paySerNum` int(11) NULL DEFAULT NULL COMMENT '关联的付款流水号，外键关联 payrec 表（未缴罚款时为 NULL）',
  PRIMARY KEY (`serNum`) USING BTREE,
  INDEX `sno`(`sno` ASC) USING BTREE,      -- 按学号索引，加速查某学生的借阅记录
  INDEX `barCode`(`barCode` ASC) USING BTREE -- 按条码索引，加速查某副本的借阅历史
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '借阅记录表' ROW_FORMAT = DYNAMIC;


-- ================================================================
-- cardrec：借书证操作记录表
-- ================================================================
-- 角色：记录借书证的每次状态变更操作，用于审计追溯
-- 与 libcard 为 N:1 关系：通过 originCardNo/newCardNo 关联具体证件
-- 与 student 为 N:1 关系：通过 sno 关联学生
-- opType 四种操作：新办（首次或挂失后重新办）、挂失、补办（注销后重新办）、注销
-- 规则：挂失/注销不产生新卡号（newCardNo=NULL），新办/补办才生成新卡号
-- ================================================================
DROP TABLE IF EXISTS `cardrec`;
CREATE TABLE `cardrec`  (
  `serNum` int(11) NOT NULL AUTO_INCREMENT COMMENT '操作流水号，全局唯一',
  `sno` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '操作学生学号，外键关联 student 表',
  `originCardNo` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '原借书证号（挂失/补办/注销时填写，新办时为 NULL）',
  `newCardNo` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '新借书证号（新办/补办时生成，挂失/注销时为 NULL）',
  `opType` enum('新办','挂失','补办','注销') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '操作类型：新办-首次办证或挂失后重办 挂失-证件挂失 补办-注销后重办 注销-证件作废',
  PRIMARY KEY (`serNum`) USING BTREE,
  INDEX `sno`(`sno` ASC) USING BTREE,           -- 按学号索引，查某学生全部证件操作
  INDEX `originCardNo`(`originCardNo` ASC) USING BTREE, -- 按原卡号索引，追溯证件变更链
  INDEX `newCardNo`(`newCardNo` ASC) USING BTREE  -- 按新卡号索引，反向追溯证件来源
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '借书证操作记录表' ROW_FORMAT = DYNAMIC;


-- ================================================================
-- libcard：借书证信息表
-- ================================================================
-- 角色：记录每张借书证的当前状态和持证人信息，是学生借书的凭证
-- 与 student 为 N:1 关系：一个学生可持有多张证（挂失后重新办理）
-- 与 borrowrec 间接关联：借书时校验 cardStatus 是否为"正常"
-- cardStatus 三种状态：正常（可借书）、挂失（不可借）、注销（不可借）
-- times 记录该证当前在借数量，用于限制最大借阅数
-- 冗余存储 sname/college/major 等学生信息，方便查询时无需联表
-- ================================================================
DROP TABLE IF EXISTS `libcard`;
CREATE TABLE `libcard`  (
  `cardNo` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '借书证号（学号+三位序号），全馆唯一',
  `sno` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '持证学生学号，外键关联 student 表',
  `sname` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '持证学生姓名（冗余字段，避免联表查询）',
  `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学生类型（本科生/研究生）',
  `college` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所在学院',
  `major` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所学专业',
  `birth` date NOT NULL COMMENT '出生日期',
  `originPlace` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '籍贯',
  `cardStatus` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '正常' COMMENT '证件状态：正常-可借书 挂失-冻结 注销-作废',
  `times` int(2) UNSIGNED ZEROFILL NULL DEFAULT 00 COMMENT '当前在借数量（归还时减1，借出时加1）',
  PRIMARY KEY (`cardNo`) USING BTREE,
  INDEX `sno`(`sno` ASC) USING BTREE   -- 按学号索引，查某学生所有证件
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '借书证信息表' ROW_FORMAT = DYNAMIC;


-- ================================================================
-- notice：公告表
-- ================================================================
-- 角色：存放图书馆向读者发布的各类公告通知
-- 独立表，与其他表无外键关系
-- 管理员通过后台发布开馆时间、借阅规则、新书推荐、系统维护等公告
-- 前端首页按 createDate 倒序展示最新公告
-- ================================================================
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '公告自增主键',
  `topic` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '公告标题',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '公告正文内容',
  `author` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发布人用户名（关联 admin 表 username）',
  `createDate` datetime NULL DEFAULT NULL COMMENT '公告发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '图书馆公告表' ROW_FORMAT = DYNAMIC;


-- ================================================================
-- payrec：付款记录表
-- ================================================================
-- 角色：记录学生缴纳罚款或赔偿款的财务流水
-- 与 student 为 N:1 关系：通过 sno 关联缴费学生
-- 与 borrowrec 为 1:N 关系：一条付款记录可对应多条借阅罚款（通过 paySerNum 反向关联）
-- 罚款类型：超期罚款（1元/天）、图书挂失赔偿（按定价）
-- 财务对账时可按 sno 或 payDate 汇总统计
-- ================================================================
DROP TABLE IF EXISTS `payrec`;
CREATE TABLE `payrec`  (
  `serNum` int(11) NOT NULL AUTO_INCREMENT COMMENT '付款流水号，全局唯一',
  `sno` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '缴费学生学号，外键关联 student 表',
  `payAmount` decimal(10, 2) NOT NULL COMMENT '付款金额（元）',
  `payDate` date NOT NULL COMMENT '付款日期',
  PRIMARY KEY (`serNum`) USING BTREE,
  INDEX `sno`(`sno` ASC) USING BTREE   -- 按学号索引，查某学生全部付款记录
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '付款记录表' ROW_FORMAT = DYNAMIC;


-- ================================================================
-- student：学生信息表
-- ================================================================
-- 角色：记录在校学生的基本信息，是借阅业务的主体
-- 与 libcard 为 1:N 关系：一个学生可办多张借书证
-- 与 borrowrec 为 1:N 关系：一个学生可有多条借阅记录
-- graduated 字段控制借书权限：已毕业学生（graduated=1）不可借书
-- password 默认值 123456，学生登录后应自行修改
-- ================================================================
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `sno` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '学号，全校唯一，兼做登录凭证之一',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '学生姓名',
  `type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '学生类型：本科生/研究生（影响最大借阅数量）',
  `college` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所在学院',
  `major` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所学专业',
  `birth` date NOT NULL COMMENT '出生日期',
  `originPlace` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '籍贯（省/直辖市）',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '123456' COMMENT '登录密码（默认123456，建议登录后修改）',
  `graduated` tinyint(1) NOT NULL DEFAULT 0 COMMENT '毕业标志：0-未毕业（可借书） 1-已毕业（禁止借书）',
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键，内部使用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '学生信息表' ROW_FORMAT = DYNAMIC;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;
