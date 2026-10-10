package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新办借书证请求 DTO，用于提交办证申请。
 * 证号由后端自动生成，前端只需提供学生学号、姓名和可选的初始密码。
 * 系统会校验学号是否存在于学籍表、该学生是否已持有借书证。
 */
@Data
@Schema(description = "新办借书证（仅需学号与姓名，证号由系统自动生成）")
public class CardCreateDTO {

    /** 学生学号，必须是学籍表中已存在的有效学号 */
    @NotBlank
    @Schema(description = "学号", example = "2021101001")
    private String sno;

    /** 学生姓名，须与学籍表中的记录一致，用于校验身份 */
    @NotBlank
    @Schema(description = "学生姓名（须与学籍表一致）", example = "张三")
    private String studentName;

    /** 借书证初始密码，默认为"123456"，可由学生后续修改 */
    @Schema(description = "密码，默认123456")
    private String password = "123456";
}
