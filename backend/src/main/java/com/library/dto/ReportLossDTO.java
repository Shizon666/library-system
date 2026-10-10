package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 图书挂失请求 DTO，用于学生报告借阅图书遗失。
 * 按副本条码提交挂失，后端校验该副本是否由当前学生借出。
 * 挂失后系统将生成罚款记录，借阅状态标记为"挂失还"。
 */
@Data
public class ReportLossDTO {

    /** 要挂失的副本条形码，必须是当前处于"已借出"状态的有效条码 */
    @NotBlank
    private String barCode;

    /** 借书证号，可省略时由后端根据登录态自动获取 */
    private String cardNo;
}
