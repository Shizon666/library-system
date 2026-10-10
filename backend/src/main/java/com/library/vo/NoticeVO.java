package com.library.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "公告信息")
public class NoticeVO {

    private Integer id;

    private String topic;

    private String content;

    private String author;

    private LocalDateTime createDate;
}
