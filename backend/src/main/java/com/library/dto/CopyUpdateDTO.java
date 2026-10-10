package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "副本信息更新")
public class CopyUpdateDTO {

    @NotBlank
    @Schema(description = "馆藏位置，格式 X区-XX架-XX层")
    private String place;
}
