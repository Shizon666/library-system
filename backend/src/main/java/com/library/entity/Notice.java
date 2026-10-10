package com.library.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书馆公告实体，对应 notice 表。
 */
@Data
public class Notice {

    private Integer id;

    /** 公告标题 */
    private String topic;

    /** 公告正文 */
    private String content;

    /** 发布人用户名 */
    private String author;

    /** 发布时间 */
    private LocalDateTime createDate;
}
