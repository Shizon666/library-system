package com.library.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常拦截器，统一捕获各类异常并转为标准 Result JSON 响应。
 * 使用 @RestControllerAdvice 对所有 Controller 生效，避免每个方法重复 try-catch。
 * 异常优先级：业务异常 > 参数校验 > 数据库冲突 > 兜底异常。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常，直接透传错误码和消息。
     * 业务层抛出的 BusinessException 在此被转为前端可识别的 Result。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理 @Valid 参数校验失败异常，提取第一条字段错误信息返回。
     * 包括 @RequestBody 校验（MethodArgumentNotValidException）和表单绑定（BindException）。
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(Exception e) {
        String msg = "参数校验失败";
        // 尝试从校验结果中提取第一个字段错误的默认消息
        if (e instanceof MethodArgumentNotValidException manv
                && manv.getBindingResult().getFieldError() != null) {
            msg = manv.getBindingResult().getFieldError().getDefaultMessage();
        }
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 处理数据库唯一键冲突和数据完整性违反异常。
     * 根据异常信息中的关键词给出可读提示，避免直接暴露数据库堆栈。
     */
    @ExceptionHandler({DuplicateKeyException.class, DataIntegrityViolationException.class})
    public Result<Void> handleDuplicate(Exception e) {
        // 根据异常消息中的字段名给出针对性提示
        String msg = e.getMessage() != null && e.getMessage().contains("phone")
                ? "电话号码已被占用"
                : "数据已存在，请勿重复提交";
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 兜底异常处理器，捕获所有未被上述处理器匹配的异常。
     * 避免前端收到 500 空白页，返回友好的错误提示。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.fail(ResultCode.FAIL.getCode(), e.getMessage() != null ? e.getMessage() : "系统异常");
    }
}
