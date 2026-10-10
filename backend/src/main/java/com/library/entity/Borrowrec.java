package com.library.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 借阅记录实体类，完整跟踪一次借还操作的全生命周期。
 * 对应数据库 borrowrec 表，从借书到还书、罚款缴纳形成闭环。
 * 每条记录关联一个学生和一本实体书副本。
 */
@Data
public class Borrowrec {

    /** 流水号，主键 */
    private Integer serNum;

    /** 借书学生学号 */
    private String sno;

    /** 所借副本的条形码 */
    private String barCode;

    /** 借出日期，借书操作的时间点 */
    private LocalDate borDate;

    /** 应还日期 = 借出日期 + 借阅天数（默认30天） */
    private LocalDate retDate;

    /** 实际归还日期，未归还时为 null */
    private LocalDate realRetDate;

    /** 归还状态：按时还 / 超时还 / 未归还 */
    private String retStatus;

    /** 逾期天数，未逾期时为0或null */
    private Integer ovdDays;

    /** 累计罚款金额（元），由逾期天数 * 日罚款率计算 */
    private BigDecimal fineMoney;

    /** 罚款缴纳状态：已缴 / 未缴 */
    private String fineStatus;

    /** 关联的缴费流水号，对应 payrec 表的 serNum */
    private Integer paySerNum;
}
