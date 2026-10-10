package com.library.vo;

import lombok.Data;

import java.util.List;

@Data
public class PortalHomeVO {
    private List<RankingVO.BookRankItem> hotBooks;
    private List<RankingVO.StudentRankItem> borrowStars;
    private List<BookBriefVO> newBooks;
    private List<NoticeVO> latestNotices;
}
