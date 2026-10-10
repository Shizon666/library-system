package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 借书请求参数 DTO，支持批量借阅（一次传多个条码）。
 * 管理员代借时需指定 cardNo，学生端可省略由登录态自动获取。
 * 条码列表不能为空，后端校验每个条码的有效性和可借状态。
 */
@Data
@Schema(description = "借书请求")
public class BorrowDTO {

    /** 借书证号，管理员代借时必填；学生端可省略，由后端根据登录态自动填充 */
    @Schema(description = "借书证号（管理员代借必填；学生端可省略，由登录态自动获取）", example = "C2024001")
    private String cardNo;

    /** 要借阅的副本条码列表，支持同时借多本，每个条码对应一本实体书 */
    @NotEmpty
    @Schema(description = "副本条码列表", example = "[\"BC-2024001\"]")
    private List<String> barCodes;
}
