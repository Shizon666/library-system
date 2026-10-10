package com.library.mapper;

import com.library.entity.Borrowrec;
import com.library.vo.BorrowingVO;
import com.library.vo.HistoryVO;
import com.library.vo.RankingVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 借阅记录表 Mapper */
public interface BorrowrecMapper {
    /** 新增借阅流水，借书成功后写入 */
    int insert(Borrowrec borrowrec);

    /** 按条码查未归还的借阅记录，防止同一本实体书被重复借出 */
    Borrowrec findActiveByBarCode(@Param("barCode") String barCode);

    /** 按流水号查单条记录，还书和罚款操作前先拿这条做判断 */
    Borrowrec findBySerNum(@Param("serNum") int serNum);

    /** 统计某学生当前在借数量，借书前校验是否超过上限 */
    long countActiveBySno(@Param("sno") String sno);

    /** 还书：回写归还状态、实际还书日期、逾期天数及罚款信息 */
    int returnBook(@Param("serNum") int serNum,
                   @Param("retStatus") String retStatus,
                   @Param("realRetDate") LocalDate realRetDate,
                   @Param("ovdDays") int ovdDays,
                   @Param("fineMoney") BigDecimal fineMoney,
                   @Param("fineStatus") String fineStatus);

    /** 批量将罚款状态更新为已缴费，并关联缴费流水号 */
    int updateFinePaid(@Param("serNums") List<Integer> serNums, @Param("paySerNum") int paySerNum);

    /** 查询某学生的在借记录（JOIN 图书和副本信息），个人中心"在借"页签用 */
    List<BorrowingVO> findActiveBySno(@Param("sno") String sno);

    /** 查询某学生全部借阅历史（JOIN 图书和副本信息），个人中心"历史"页签用 */
    List<HistoryVO> findHistoryBySno(@Param("sno") String sno);

    /** 统计全局当前在借总量，仪表盘展示 */
    long countActive();

    /** 本月借阅量学生排行榜（按借阅次数降序取 Top10） */
    List<RankingVO.StudentRankItem> studentMonthlyRanking();

    /** 本月热门图书排行榜（按借阅次数降序取 Top10） */
    List<RankingVO.BookRankItem> bookMonthlyRanking();
}
