package com.library.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录返回")
public class LoginVO {
    private String role;
    private String username;
    private String sno;
    private String cardNo;
    private String displayName;
}
