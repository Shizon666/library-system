package com.library.mapper;

import com.library.entity.Book;
import com.library.vo.BookBriefVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 图书目录表 Mapper */
public interface BookMapper {
    /** 按 ISBN 精确查一本书，新增/编辑副本时校验 ISBN 是否已存在 */
    Book findByIsbn(@Param("ISBN") String ISBN);

    /** 读者端搜书：关键字模糊搜索书名、作者、ISBN、出版社，只返回上架图书 */
    List<Book> search(@Param("keyword") String keyword);

    /** 后台分页查询前先拿总数，前端分页组件依赖这个数字算总页数 */
    long countCatalog(@Param("keyword") String keyword);

    /** 后台书目分页列表，keyword 为空时查全部 */
    List<Book> listCatalog(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    /** 编辑图书信息：不允许改 ISBN（主键），只更新元数据字段 */
    int update(Book book);

    /** 首页新书推荐：按出版日期倒序取最新的 N 本 */
    List<BookBriefVO> findNewBooks(@Param("limit") int limit);

    /** 新增图书元数据，ISBN 由管理员手动录入 */
    int insert(Book book);

    /** 上下架操作：bookStatus=1 上架、=0 下架 */
    int updateStatus(@Param("ISBN") String ISBN, @Param("bookStatus") int bookStatus);

    /** 统计馆藏图书种数，仪表盘展示 */
    long countAll();
}
