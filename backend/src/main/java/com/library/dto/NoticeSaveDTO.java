package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "公告发布/编辑")
public class NoticeSaveDTO {

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 50, message = "公告标题不能超过50字")
    private String topic;

    @NotBlank(message = "公告内容不能为空")
    @Size(max = 255, message = "公告内容不能超过255字")
    private String content;
}
