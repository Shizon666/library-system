package com.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 图书采编入库请求 DTO，用于管理员新增图书书目及副本。
 * 一次请求可同时创建书目信息和若干馆藏副本，实现批量入库。
 * 若 copies 列表为空，则仅创建书目记录，不生成副本。
 */
@Data
@Schema(description = "图书采编入库")
public class BookAcquireDTO {

    /** 国际标准书号（ISBN），唯一标识一种图书 */
    @NotBlank
    @JsonProperty("ISBN")
    private String ISBN;

    /** 图书名称 */
    @NotBlank
    private String bookName;

    /** 图书作者 */
    @NotBlank
    private String author;

    /** 出版社名称 */
    @NotBlank
    private String publisher;

    /** 出版日期，可选 */
    private LocalDate pubDate;

    /** 图书简介/内容提要，可选 */
    private String introduction;

    /** 中图法分类号，如 TP311，可选 */
    private String category;

    /** 图书定价（元），可选 */
    private java.math.BigDecimal price;

    /** 新增副本列表，每项包含条形码和馆藏位置，可为空（仅创建书目） */
    @Valid
    @Schema(description = "新增副本列表，可为空（仅创建书目时）")
    private List<CopyItem> copies;

    /**
     * 副本明细内部类，描述单本实体书的入库信息。
     * 与 BookAcquireDTO 一起提交，实现书目与副本的一次性创建。
     */
    @Data
    public static class CopyItem {

        /** 副本的唯一条形码编号 */
        @NotBlank
        private String barCode;

        /** 副本的馆藏位置，如"三楼文学区A架" */
        @NotBlank
        private String place;
    }
}
