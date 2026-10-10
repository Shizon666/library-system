package com.library.mapper;

import com.library.entity.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 公告表 Mapper */
public interface NoticeMapper {

    List<Notice> findLatest(@Param("limit") int limit);

    long count(@Param("keyword") String keyword);

    List<Notice> list(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    Notice findById(@Param("id") int id);

    int insert(Notice notice);

    int update(Notice notice);

    int deleteById(@Param("id") int id);
}
