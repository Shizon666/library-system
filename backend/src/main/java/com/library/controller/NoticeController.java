package com.library.controller;

import com.library.common.Result;
import com.library.dto.NoticeSaveDTO;
import com.library.service.NoticeService;
import com.library.vo.NoticeVO;
import com.library.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器：公开浏览 + 管理员维护。
 */
@Tag(name = "公告管理")
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "最新公告（公开）")
    @GetMapping("/latest")
    public Result<List<NoticeVO>> latest(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(noticeService.listLatest(limit));
    }

    @Operation(summary = "公告分页列表（公开）")
    @GetMapping
    public Result<PageVO<NoticeVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(noticeService.listPage(keyword, page, size));
    }

    @Operation(summary = "公告详情（公开）")
    @GetMapping("/{id}")
    public Result<NoticeVO> detail(@PathVariable int id) {
        return Result.success(noticeService.getById(id));
    }

    @Operation(summary = "发布公告（管理员）")
    @PostMapping
    public Result<NoticeVO> create(
            @Valid @RequestBody NoticeSaveDTO dto,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return Result.success(noticeService.create(dto, role, username));
    }

    @Operation(summary = "编辑公告（管理员）")
    @PutMapping("/{id}")
    public Result<NoticeVO> update(
            @PathVariable int id,
            @Valid @RequestBody NoticeSaveDTO dto,
            @RequestHeader(value = "X-Role", required = false) String role) {
        return Result.success(noticeService.update(id, dto, role));
    }

    @Operation(summary = "删除公告（管理员）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable int id,
            @RequestHeader(value = "X-Role", required = false) String role) {
        noticeService.delete(id, role);
        return Result.success(null);
    }
}
