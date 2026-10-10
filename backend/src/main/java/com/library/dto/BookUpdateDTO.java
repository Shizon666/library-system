package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "书目信息更新")
public class BookUpdateDTO {

    @NotBlank
    private String bookName;
    @NotBlank
    private String author;
    @NotBlank
    private String publisher;
    private LocalDate pubDate;
    private String introduction;
    private String category;
    private BigDecimal price;
}
