package com.library.mapper;

import com.library.entity.Libcard;
import org.apache.ibatis.annotations.Param;

/** 借书证表 Mapper */
public interface LibcardMapper {
    /** 按卡号查证件，挂失/补办/注销操作前先验证卡是否存在 */
    Libcard findByCardNo(@Param("cardNo") String cardNo);

    /** 查学生当前有效的借书证，借书时必须有这张卡才能操作 */
    Libcard findActiveBySno(@Param("sno") String sno);

    /** 判断某学生是否存在已挂失的借书证，防止重复挂失 */
    boolean existsLostBySno(@Param("sno") String sno);

    /** 统计该学生名下有几张卡，新办时用来限制卡数上限 */
    long countBySno(@Param("sno") String sno);

    /** 新办借书证，cardNo 由学号+3位序号拼接生成 */
    int insert(Libcard libcard);

    /** 挂失/恢复/注销时切换卡状态，前端通过状态控制可用操作按钮 */
    int updateCardStatus(@Param("cardNo") String cardNo, @Param("cardStatus") String cardStatus);

    /** 增减借书证的借阅次数（delta 借书+1 还书-1），用于判断是否达到上限 */
    int updateTimes(@Param("cardNo") String cardNo, @Param("delta") int delta);

    /** 统计当前有效的借书证总数，仪表盘展示 */
    long countActive();

    /** 查找某学号前缀下最大的借书证号（用于自动生成新证号） */
    String findMaxCardNoBySnoPrefix(@Param("sno") String sno);

    /** 查询某学生最新一张借书证（含已挂失的），补办时用来复制旧卡资料 */
    Libcard findLatestBySno(@Param("sno") String sno);
}
