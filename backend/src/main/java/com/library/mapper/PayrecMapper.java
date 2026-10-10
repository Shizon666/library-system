package com.library.mapper;

import com.library.entity.Payrec;
import com.library.entity.FinePayment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 罚款缴费记录表 Mapper */
public interface PayrecMapper {
    /** 新增缴费流水，serNum 自动生成后回写到 borrowrec 的 paySerNum 做关联 */
    int insert(Payrec payrec);

    /** 学生端查自己的缴费历史，按缴费时间倒序展示 */
    List<FinePayment> findBySno(@Param("sno") String sno);
}
