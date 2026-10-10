package com.library.vo;

import lombok.Data;

@Data
public class BorrowCheckVO {
    private String cardNo;
    private String sno;
    private String studentName;
    private Boolean canBorrow;
    private String reason;
    private Integer borrowLimit;
    private Long currentBorrowCount;
    private Integer remainingSlots;
}
