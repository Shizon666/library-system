package com.library.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HistoryVO {
    private Integer recNo;
    private String barCode;
    private String bookName;
    private String author;
    @JsonProperty("ISBN")
    private String ISBN;
    private LocalDate borDate;
    private LocalDate retDate;
    private LocalDate realRetDate;
    private Integer status;
    /** 还书状态：未归还/按时还/超时还 */
    private String retStatusText;
    private java.math.BigDecimal fineAmount;
}
