package com.library.common;

import lombok.Getter;

/**
 * 自定义业务异常类，用于在业务逻辑中中断流程并返回错误信息。
 * 抛出后由 GlobalExceptionHandler 统一拦截，转为 Result JSON 响应。
 * 相比直接返回 Result，使用异常可以减少 Controller 中的 if-else 嵌套。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码，对应 ResultCode 枚举的 code 值 */
    private final int code;

    /**
     * 通过预定义的错误码枚举构造业务异常。
     * @param resultCode 错误码枚举，自动提取 code 和 message
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 通过自定义错误码和消息构造业务异常。
     * 用于需要动态生成错误描述的场景。
     * @param code    自定义数字错误码
     * @param message 自定义错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
