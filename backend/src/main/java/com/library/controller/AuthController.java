package com.library.controller;

import com.library.common.Result;
import com.library.dto.LoginDTO;
import com.library.service.AuthService;
import com.library.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 登录认证控制器。
 * <p>
 * 管理员和学生共用同一个登录入口，后端通过账号中的角色字段区分身份。
 * 登录成功后返回 token 和用户基本信息，前端据此判断跳转到管理端还是学生端。
 * </p>
 */
@Tag(name = "登录认证")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    /** 认证服务，负责校验账号密码并生成登录凭证 */
    private final AuthService authService;

    /**
     * 统一登录接口。
     * <p>
     * 接收学号/工号和密码，校验通过后返回 JWT token 与用户角色信息。
     * 管理员和学生走同一接口，由 AuthService 内部根据账号查询角色表判断身份。
     * </p>
     *
     * @param dto 包含账号和密码的登录请求体，字段均有非空校验
     * @return 登录成功返回 token、姓名、角色等信息
     */
    @Operation(summary = "管理员/学生登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        // 委托给 AuthService 完成密码校验与 token 生成
        return Result.success(authService.login(dto));
    }
}
