package com.library.entity;

import lombok.Data;

import java.time.LocalDate;

/**
 * 图书基本信息实体类（书目级别），对应数据库 book 表。
 * 一条 Book 记录代表一种图书（按 ISBN 区分），可对应多本 Bookcopy 实体副本。
 * 管理员通过采编模块维护书目信息。
 */
@Data
public class Book {

    /** 主键 ID，数据库自增 */
    private Integer id;

    /** 国际标准书号，同一 ISBN 对应同一种图书 */
    private String ISBN;

    /** 书名 */
    private String bname;

    /** 作者姓名 */
    private String author;

    /** 出版社名称 */
    private String publisher;

    /** 出版日期 */
    private LocalDate pubDate;

    /** 图书简介/内容提要 */
    private String introduction;

    /** 中国图书馆分类法分类号，如 TP311 */
    private String clcNum;

    /** 书目状态：0=下架（不可借），1=上架（可借） */
    private Integer bookStatus;

    /** 图书定价（元），用于罚款金额参考 */
    private java.math.BigDecimal price;
}
