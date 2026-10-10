package com.library.controller;

import com.library.common.Result;
import com.library.service.DashboardService;
import com.library.vo.DashboardStatsVO;
import com.library.vo.RankingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 首页统计面板控制器。
 * <p>
 * 为管理端和学生端提供首页数据概览，根据用户角色返回不同维度的统计信息。
 * 管理员看到全局馆藏和借阅数据，学生看到个人借阅统计。
 * 同时提供当月排行榜数据，包括借书达人榜和热门图书榜。
 * </p>
 */
@Tag(name = "首页统计")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /** 首页统计服务，聚合各类统计数据 */
    private final DashboardService dashboardService;

    /**
     * 首页统计数据。
     * <p>
     * 根据用户角色返回不同维度的概览数据：
     * 管理员看到馆藏总量、在借册数、读者人数等全局数据；
     * 学生看到个人在借数量、历史借阅量等个人数据。
     * </p>
     *
     * @param role 请求头中的用户角色，默认 admin
     * @param sno  请求头中的学号，学生端必传
     * @return 首页统计数据面板
     */
    @Operation(summary = "首页统计数据")
    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats(
            @RequestHeader(value = "X-Role", defaultValue = "admin") String role,
            @RequestHeader(value = "X-Sno", required = false) String sno) {
        // Service 层根据 role 字段分支查询不同维度的统计数据
        return Result.success(dashboardService.getStats(role, sno));
    }

    /**
     * 当月排行榜。
     * <p>
     * 返回两个榜单：学生借书排行榜（按借阅量排名）和热门图书排行榜（按被借次数排名）。
     * 数据基于当月借阅记录统计。
     * </p>
     *
     * @return 排行榜数据，包含借书达人榜和热门图书榜
     */
    @Operation(summary = "当月排行榜（学生借书/热门图书）")
    @GetMapping("/rankings")
    public Result<RankingVO> rankings() {
        return Result.success(dashboardService.getRankings());
    }
}
