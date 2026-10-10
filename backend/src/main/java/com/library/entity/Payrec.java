package com.library.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 罚款缴费记录实体类，记录每一次缴费行为。
 * 对应数据库 payrec 表，一次缴费可对应多条借阅记录（批量缴纳场景）。
 * 缴费完成后，关联的 borrowrec 记录的罚款状态会更新为"已缴"。
 */
@Data
public class Payrec {

    /** 缴费流水号，主键 */
    private Integer serNum;

    /** 缴费学生学号 */
    private String sno;

    /** 本次实际缴费金额（元），可能包含多笔罚款的合计 */
    private BigDecimal payAmount;

    /** 缴费日期 */
    private LocalDate payDate;
}
