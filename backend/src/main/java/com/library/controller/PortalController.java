package com.library.controller;

import com.library.common.Result;
import com.library.service.DashboardService;
import com.library.vo.PortalHomeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图书馆对外门户控制器。
 * <p>
 * 提供无需登录即可访问的公开展示页面数据，聚合热读推荐、
 * 借书明星和新书上架三块内容，作为图书馆的对外形象展示入口。
 * </p>
 */
@Tag(name = "图书馆门户")
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    /** 首页统计服务，复用排行榜和推荐数据 */
    private final DashboardService dashboardService;

    /**
     * 门户首页聚合数据。
     * <p>
     * 一次请求返回四块内容：最新公告、热读推荐（近期被借次数最多的图书）、
     * 借书明星（当月借阅量最多的学生）、新书上架（最近入库的图书）。
     * 聚合返回以减少前端请求数，提升首屏加载速度。
     * </p>
     *
     * @return 门户首页聚合数据，包含热读推荐、借书明星、新书上架三个列表
     */
    @Operation(summary = "门户首页数据（公告/热读推荐/借书明星/新书上架）")
    @GetMapping("/home")
    public Result<PortalHomeVO> home() {
        // Service 层内部并行查询三块数据后组装为 PortalHomeVO 返回
        return Result.success(dashboardService.getPortalHome());
    }
}
