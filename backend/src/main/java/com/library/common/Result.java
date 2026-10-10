package com.library.common;

import lombok.Data;

/**
 * 统一 API 响应包装类，所有接口返回值均使用此类封装。
 * 前端统一按 code===0 判断请求是否成功，data 字段承载业务数据。
 * 提供 success/fail 静态工厂方法，简化 Controller 层代码。
 */
@Data
public class Result<T> {

    /** 响应状态码，0=成功，非0=失败（具体含义见 ResultCode 枚举） */
    private int code;

    /** 响应提示信息，成功时为"success"，失败时为具体错误描述 */
    private String message;

    /** 响应业务数据，失败时通常为 null */
    private T data;

    /**
     * 返回成功的响应，携带业务数据。
     * @param data 业务数据对象
     * @param <T>  数据类型
     * @return 包装后的成功响应
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * 返回成功的响应，不携带数据（用于删除、更新等无返回值的操作）。
     * @param <T> 数据类型
     * @return 包装后的成功响应
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 返回失败的响应，使用预定义的错误码枚举。
     * @param resultCode 错误码枚举值
     * @param <T>        数据类型
     * @return 包装后的失败响应
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }

    /**
     * 返回失败的响应，支持自定义错误码和消息。
     * 用于需要动态描述错误信息的场景，如参数校验的具体字段错误。
     * @param code    自定义错误码
     * @param message 自定义错误消息
     * @param <T>     数据类型
     * @return 包装后的失败响应
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
