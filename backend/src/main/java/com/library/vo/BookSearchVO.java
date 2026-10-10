package com.library.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.library.entity.Bookcopy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "图书检索结果")
public class BookSearchVO {
    @JsonProperty("ISBN")
    private String ISBN;
    private String bookName;
    private String author;
    private String publisher;
    private LocalDate pubDate;
    private String introduction;
    private String category;
    private Integer status;
    private List<Bookcopy> copies;

    @Schema(description = "馆藏总册数")
    private Integer totalCopies;
    @Schema(description = "可借册数 status=1")
    private Integer availableCopies;
    @Schema(description = "已借出册数（book 表字段）")
    private Integer borrowedCopies;
}
