package com.library.vo;

import lombok.Data;

@Data
public class CardDetailVO {
    private String cardNo;
    private String sno;
    private Integer status;
    private String studentName;
    private String sex;
    private Integer studentType;
    private String college;
    private String major;
    private String phone;
    private Long currentBorrowCount;
    private Integer borrowLimit;
    private java.math.BigDecimal unpaidFineAmount;
}
