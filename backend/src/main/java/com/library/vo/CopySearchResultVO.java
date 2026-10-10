package com.library.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CopySearchResultVO {
    private boolean found;
    private boolean allBorrowed;
    private String message;
    /** 保留兼容：第一个可借副本 */
    private CopyBorrowVO copy;
    /** 所有副本（含借出、可借，不含注销） */
    private List<CopyBorrowVO> copies;

    @Data
    public static class CopyBorrowVO {
        private String barCode;
        @JsonProperty("ISBN")
        private String ISBN;
        private String bookName;
        private String author;
        private String publisher;
        private String place;
        /** 副本状态：1可借 0借出 2注销 */
        private Integer status;
        /** 状态文字：可借/借出/注销 */
        private String statusText;
    }
}
