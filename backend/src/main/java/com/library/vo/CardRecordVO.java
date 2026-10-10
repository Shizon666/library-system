package com.library.vo;

import lombok.Data;

@Data
public class CardRecordVO {
    private Integer serNum;
    private String sno;
    private String studentName;
    private String originCardNo;
    private String newCardNo;
    private Integer opType;
}
