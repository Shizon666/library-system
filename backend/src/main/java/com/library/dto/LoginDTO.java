package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录请求")
public class LoginDTO {

    @NotBlank
    @Schema(description = "角色: admin 或 student", example = "admin")
    private String role;

    @NotBlank
    @Schema(description = "管理员填用户名，学生填借书证号", example = "admin")
    private String username;

    @NotBlank
    @Schema(description = "密码", example = "123456")
    private String password;
}
