package com.library.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookBriefVO {
    @JsonProperty("ISBN")
    private String ISBN;
    private String bookName;
    private String author;
    private String publisher;
    private LocalDate pubDate;
}
