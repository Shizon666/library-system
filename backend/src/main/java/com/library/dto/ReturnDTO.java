package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 还书请求参数 DTO，按副本条码归还单本图书。
 * 系统根据条码找到对应的借阅记录，计算逾期天数和罚款金额。
 * 归还后自动更新副本状态为"可借"，并减少持证人的在借数量。
 */
@Data
@Schema(description = "还书请求")
public class ReturnDTO {

    /** 要归还的副本条形码，必须是当前处于"已借出"状态的有效条码 */
    @NotBlank
    @Schema(description = "副本条码", example = "BC-2024001")
    private String barCode;
}
