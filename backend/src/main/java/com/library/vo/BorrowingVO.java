package com.library.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BorrowingVO {
    private Integer recNo;
    private String barCode;
    private String bookName;
    private String author;
    @JsonProperty("ISBN")
    private String ISBN;
    private LocalDate borDate;
    private LocalDate retDate;
    private Integer overdueDays;
    private Integer status;
    private java.math.BigDecimal bookPrice;
}
