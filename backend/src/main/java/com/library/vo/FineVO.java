package com.library.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FineVO {
    private Integer fineId;
    private Integer recNo;
    private String barCode;
    private String bookName;
    private String author;
    private BigDecimal amount;
    private Integer payStatus;
    private LocalDate retDate;
    private LocalDate realRetDate;
    private LocalDate createTime;
    private Integer overdueDays;
    private String sno;
    private String studentName;
}
