package com.library.mapper;

import com.library.vo.FineVO;
import com.library.entity.Borrowrec;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/** 罚款记录 Mapper（基于借阅记录中的罚款字段） */
public interface FineMapper {
    /** 按流水号查单条罚款记录，缴费前先校验是否存在且未缴 */
    Borrowrec findBySerNum(@Param("serNum") int serNum);

    /** 学生端查自己的罚款列表，按实际归还时间倒序 */
    List<FineVO> findBySno(@Param("sno") String sno);

    /** 查询全部罚款记录（JOIN 学生和图书信息，支持按缴费状态过滤） */
    List<FineVO> findAll(@Param("payStatus") Integer payStatus);

    /** 按学号查询罚款记录（JOIN 学生和图书信息，支持按缴费状态过滤） */
    List<FineVO> findAllBySno(@Param("sno") String sno, @Param("payStatus") Integer payStatus);

    /** 统计某学生未缴罚款笔数，个人中心展示待缴数量角标 */
    long countUnpaidBySno(@Param("sno") String sno);

    /** 统计某学生未缴罚款总金额，缴费页面展示 */
    BigDecimal sumUnpaidBySno(@Param("sno") String sno);

    /** 统计全局未缴罚款笔数，仪表盘提醒管理员催缴 */
    long countUnpaid();
}
