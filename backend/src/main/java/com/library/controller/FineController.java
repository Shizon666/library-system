package com.library.controller;

import com.library.common.Result;
import com.library.dto.FinePayDTO;
import com.library.entity.FinePayment;
import com.library.service.FineService;
import com.library.vo.FineVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 罚款管理控制器。
 * <p>
 * 支持按缴纳状态筛选罚款记录，支持多条罚款合并缴纳。
 * 管理员可查看全部罚款，学生仅能查看自己的罚款记录。
 * 通过请求头 X-Role 和 X-Sno 区分不同角色的查询视角。
 * </p>
 */
@Tag(name = "罚款管理")
@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    /** 罚款服务，处理罚款查询和缴纳等业务逻辑 */
    private final FineService fineService;

    /**
     * 罚款列表查询。
     * <p>
     * 支持按缴纳状态筛选：0 表示未缴、1 表示已缴、不传则查全部。
     * 通过 X-Role 请求头区分管理员（查看全部）和学生（仅查看自己）。
     * </p>
     *
     * @param payStatus 缴纳状态筛选条件：0-未缴 1-已缴 null-全部
     * @param role      请求头中的用户角色，默认 admin
     * @param sno       请求头中的学号，学生端必传
     * @return 符合条件的罚款记录列表
     */
    @Operation(summary = "罚款查询（payStatus: 0未缴 1已缴，不传查全部）")
    @GetMapping
    public Result<List<FineVO>> list(
            @RequestParam(required = false) Integer payStatus,
            @RequestHeader(value = "X-Role", defaultValue = "admin") String role,
            @RequestHeader(value = "X-Sno", required = false) String sno) {
        // Service 层根据 role 判断查询范围：admin 查全部，student 按 sno 过滤
        return Result.success(fineService.listFines(payStatus, role, sno));
    }

    /**
     * 缴纳罚款（支持合并缴纳）。
     * <p>
     * 一次可传入多条罚款 ID 进行合并缴纳，系统生成统一的缴费凭证。
     * 缴纳后对应的罚款记录状态更新为已缴。
     * </p>
     *
     * @param dto 包含待缴纳罚款 ID 列表的请求体
     * @return 缴费凭证实体，包含缴费流水号、金额、时间等
     */
    @Operation(summary = "缴纳罚款（支持合并缴纳）")
    @PostMapping("/pay")
    public Result<FinePayment> pay(@Valid @RequestBody FinePayDTO dto) {
        // Service 层批量更新罚款状态并生成缴费凭证记录
        return Result.success(fineService.payFines(dto));
    }
}
