package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 罚款缴纳请求 DTO，支持合并缴纳多笔罚款。
 * 前端传入要缴纳的罚款 ID 列表，后端一次性处理并生成缴费记录。
 * 缴纳完成后，关联的借阅记录罚款状态更新为"已缴"。
 */
@Data
@Schema(description = "罚款缴纳（支持合并缴纳）")
public class FinePayDTO {

    /** 借书证号，用于标识缴费学生 */
    @NotBlank
    @Schema(description = "借书证号", example = "C2025001")
    private String cardNo;

    /** 要缴纳的罚款记录 ID 列表，支持一次缴多笔 */
    @NotEmpty
    @Schema(description = "要缴纳的罚款ID列表", example = "[3,4]")
    private List<Integer> fineIds;

    /** 经办人姓名（操作员），记录谁执行了缴费操作 */
    @Schema(description = "经办人", example = "管理员A")
    private String operator;

    /** 缴费备注信息，可选 */
    @Schema(description = "备注")
    private String remark;
}
