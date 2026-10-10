package com.library.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RankingVO {
    private List<StudentRankItem> studentRankings;
    private List<BookRankItem> bookRankings;

    @Data
    public static class StudentRankItem {
        private String sno;
        private String studentName;
        private long borrowCount;
    }

    @Data
    public static class BookRankItem {
        @JsonProperty("ISBN")
        private String ISBN;
        private String bookName;
        private String author;
        private long borrowCount;
    }
}
