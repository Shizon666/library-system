package com.library.service;

import com.library.mapper.*;
import com.library.vo.DashboardStatsVO;
import com.library.vo.PortalHomeVO;
import com.library.vo.RankingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 仪表盘与门户首页服务。
 * 管理端展示全局统计数据，学生端展示个人罚款概览。
 * 同时提供排行榜（当月借阅之星 + 热门图书）和门户首页聚合数据。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    /** 图书基本信息数据访问 */
    private final BookMapper bookMapper;

    /** 馆藏副本数据访问 */
    private final BookcopyMapper bookcopyMapper;

    /** 仪表盘专用统计查询 */
    private final DashboardMapper dashboardMapper;

    /** 借书证数据访问 */
    private final LibcardMapper libcardMapper;

    /** 借阅记录数据访问 */
    private final BorrowrecMapper borrowrecMapper;

    /** 罚款数据访问 */
    private final FineMapper fineMapper;

    /** 公告服务 */
    private final NoticeService noticeService;

    /**
     * 获取仪表盘统计数据。
     * 管理员：全局图书总量、在馆副本、借出副本、活跃学生数、有效证件数、当前借阅数、未缴罚款数。
     * 学生：仅看图书总量、在馆副本数，以及个人未缴罚款笔数和金额。
     *
     * @param role 当前登录用户角色
     * @param sno  学号，学生角色时必传
     * @return 仪表盘统计视图
     */
    public DashboardStatsVO getStats(String role, String sno) {
        DashboardStatsVO vo = new DashboardStatsVO();

        // 管理员和学生都能看到的基础数据：图书总量和在馆副本数
        vo.setTotalBooks(bookMapper.countAll());
        vo.setAvailableCopies(bookcopyMapper.countByStatus(1));

        // 学生角色：只返回个人罚款相关数据，不暴露全局统计
        if ("student".equalsIgnoreCase(role) && StringUtils.hasText(sno)) {
            // 该学生未缴罚款笔数
            vo.setUnpaidFines(fineMapper.countUnpaidBySno(sno));
            // 该学生未缴罚款总金额
            BigDecimal unpaidAmount = fineMapper.sumUnpaidBySno(sno);
            vo.setUnpaidFineAmount(unpaidAmount);
            return vo;
        }

        // 管理员角色：填充完整的全局统计数据
        vo.setTotalCopies(bookcopyMapper.countAll());
        // 已借出副本数（状态为 0 表示借出）
        vo.setBorrowedCopies(bookcopyMapper.countByStatus(0));
        // 当前活跃学生数
        vo.setActiveStudents(dashboardMapper.countStudents());
        // 有效借书证数
        vo.setActiveCards(libcardMapper.countActive());
        // 当前在借记录数
        vo.setCurrentBorrowings(borrowrecMapper.countActive());
        // 全局未缴罚款数
        vo.setUnpaidFines(fineMapper.countUnpaid());
        return vo;
    }

    /**
     * 获取当月排行榜。
     * 包含两个榜单：当月借阅次数最多的学生 TOP10，以及当月被借次数最多的图书 TOP10。
     *
     * @return 排行榜视图
     */
    public RankingVO getRankings() {
        RankingVO vo = new RankingVO();
        // 当月借阅之星：按学生当月借阅次数降序取前 10
        vo.setStudentRankings(borrowrecMapper.studentMonthlyRanking());
        // 热门图书：按图书当月被借次数降序取前 10
        vo.setBookRankings(borrowrecMapper.bookMonthlyRanking());
        return vo;
    }

    /**
     * 聚合门户首页数据。
     * 将排行榜（热门图书 + 借阅之星）和新书推荐合并为一个视图，
     * 前端一次请求即可渲染首页所有模块。
     *
     * @return 门户首页聚合视图
     */
    public PortalHomeVO getPortalHome() {
        // 复用排行榜接口获取热门图书和借阅之星
        RankingVO rankings = getRankings();
        PortalHomeVO vo = new PortalHomeVO();
        // 热门图书 TOP10
        vo.setHotBooks(rankings.getBookRankings());
        // 借阅之星 TOP10
        vo.setBorrowStars(rankings.getStudentRankings());
        // 最新入库的 10 本图书
        vo.setNewBooks(bookMapper.findNewBooks(10));
        // 最新 5 条公告
        vo.setLatestNotices(noticeService.listLatest(3));
        return vo;
    }
}
