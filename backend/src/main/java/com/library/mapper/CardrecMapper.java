package com.library.mapper;

import com.library.entity.Cardrec;
import com.library.vo.CardRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 借书证操作记录表 Mapper */
public interface CardrecMapper {
    /** 新增一条卡片操作流水，用于追溯卡片变更历史 */
    int insert(Cardrec cardrec);

    /** 按学号查该学生所有卡片操作记录，按时间倒序 */
    List<Cardrec> findBySno(@Param("sno") String sno);

    /** 查询借书证操作记录（支持按操作类型和学号过滤，JOIN 学生表取姓名） */
    List<CardRecordVO> findAllRecords(@Param("opType") String opType,
                                      @Param("sno") String sno);
}
