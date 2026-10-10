package com.library.common;

import lombok.Getter;

/**
 * 业务错误码枚举类，统一定义系统中所有响应状态码。
 * 约定：0=成功，1=通用失败，4xx=参数/资源问题，10xx=业务专属错误。
 * 前端可根据 code 值做分支提示，实现精细化的错误处理。
 */
@Getter
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(0, "success"),

    /** 通用操作失败 */
    FAIL(1, "操作失败"),

    /** 请求参数校验不通过 */
    PARAM_ERROR(400, "参数错误"),

    /** 请求的资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    // ---- 业务错误码：借书证 & 借阅相关 ----

    /** 借书证状态异常（挂失/注销），无法执行借书操作 */
    CARD_NOT_ACTIVE(1001, "借书证状态异常，无法借书"),

    /** 学生当前在借数量已达上限 */
    BORROW_LIMIT(1002, "已达借阅上限"),

    /** 未缴罚款累计超过阈值，系统禁止借书 */
    UNPAID_FINE_LIMIT(1003, "未缴罚款超过50元，禁止借书"),

    /** 副本当前不可借（已借出或已注销） */
    COPY_NOT_AVAILABLE(1004, "图书副本不可借"),

    /** 对应的书目已下架，所有副本均不可借 */
    BOOK_NOT_ON_SHELF(1005, "图书已下架"),

    /** 该学生已经持有借书证，不可重复办理 */
    STUDENT_HAS_CARD(1006, "该学生已有借书证"),

    /** 借书证号已存在，与现有证件冲突 */
    CARD_EXISTS(1007, "借书证号已存在"),

    /** 登录用户名或密码不匹配 */
    LOGIN_FAIL(1008, "用户名或密码错误"),

    /** 该笔罚款已缴纳，不可重复操作 */
    FINE_ALREADY_PAID(1009, "罚款已缴纳"),

    /** 不满足当前操作的前置条件 */
    INVALID_OP(1010, "无效操作");

    /** 错误码数字值 */
    private final int code;

    /** 错误码对应的中文提示信息 */
    private final String message;

    /**
     * 构造错误码枚举。
     * @param code    数字错误码
     * @param message 中文提示信息
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
