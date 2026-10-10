package com.library.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 罚款缴费实体类，兼容前端 API 响应格式，映射 payrec 表数据。
 * 与 Payrec 功能类似，但字段命名更贴近前端展示需求。
 * 用于返回给前端的罚款缴费记录列表。
 */
@Data
public class FinePayment {

    /** 缴费记录 ID，对应 payrec 表的 serNum */
    private Integer paymentId;

    /** 借书证号，冗余字段便于前端展示 */
    private String cardNo;

    /** 缴费学生学号 */
    private String sno;

    /** 本次缴费金额（元） */
    private BigDecimal paymentAmount;

    /** 缴费时间 */
    private LocalDate paymentTime;

    /** 经办人姓名（操作员） */
    private String operator;

    /** 备注信息 */
    private String remark;
}
