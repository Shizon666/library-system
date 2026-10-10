package com.library.entity;

import lombok.Data;

/**
 * 管理员账号实体类，通过 adminType 区分不同职能角色。
 * 对应数据库 admin 表，不同类型的管理员拥有不同的操作权限。
 * 0=证件管理员，1=采编管理员，2=流通管理员。
 */
@Data
public class Admin {

    /** 主键 ID，数据库自增 */
    private Integer id;

    /** 登录用户名，唯一 */
    private String username;

    /** 登录密码，数据库中存储密文 */
    private String password;

    /** 管理员类型：0=证件管理（办证/挂失），1=采编（入库），2=流通（借还） */
    private Integer adminType;
}
