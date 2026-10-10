package com.library.mapper;

import com.library.entity.Admin;
import org.apache.ibatis.annotations.Param;

/** 管理员表 Mapper */
public interface AdminMapper {
    /** 登录时按用户名查管理员，拿到密码哈希后在 Service 层做校验 */
    Admin findByUsername(@Param("username") String username);
}
