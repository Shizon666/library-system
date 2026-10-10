package com.library.mapper;

import com.library.entity.Bookcopy;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 图书副本表 Mapper */
public interface BookcopyMapper {
    /** 按条码查单本实体书，借书/还书/定位都需要先拿到副本记录 */
    Bookcopy findByBarCode(@Param("barCode") String barCode);

    /** 按 ISBN 查所有副本，用于展示某本书的馆藏明细（在架/借出/注销） */
    List<Bookcopy> findByIsbn(@Param("ISBN") String ISBN);

    /** 新增一本实体副本入库，条码由管理员手动分配或系统生成 */
    int insert(Bookcopy bookcopy);

    /** 借出/归还时切换副本状态：1=在馆、0=借出 */
    int updateStatus(@Param("barCode") String barCode, @Param("status") int status);

    /** 馆藏调拨：修改副本的存放位置（楼层/书架） */
    int updatePlace(@Param("barCode") String barCode, @Param("place") String place);

    /** 批量更新同一 ISBN 下所有在馆副本的状态，同时备份旧状态便于恢复 */
    int updateAllByIsbn(@Param("ISBN") String ISBN, @Param("status") int status);

    /** 注销副本：将状态置为已注销，需校验旧状态以便后续恢复 */
    int discardCopy(@Param("barCode") String barCode, @Param("oldStatus") int oldStatus);

    /** 统计有效馆藏副本总数（排除已注销），仪表盘展示 */
    long countAll();

    /** 按状态统计副本数，用于仪表盘各状态分布图 */
    long countByStatus(@Param("status") int status);

    /** 搜索可借副本（关联图书目录，按关键字过滤），可借副本排最前 */
    List<com.library.vo.CopySearchResultVO.CopyBorrowVO> searchCopiesForBorrow(@Param("keyword") String keyword);
}
