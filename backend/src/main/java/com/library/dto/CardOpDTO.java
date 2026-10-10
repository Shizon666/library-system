package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "证件操作（补办时填写新证号）")
public class CardOpDTO {

    @Schema(description = "补办时的新证号", example = "C2023999")
    private String newCardNo;
}
