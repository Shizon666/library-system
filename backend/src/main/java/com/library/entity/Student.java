package com.library.entity;

import lombok.Data;

import java.time.LocalDate;

/**
 * 学生信息实体类，以 sno（学号）作为业务主键。
 * 对应数据库 student 表，存储学生的个人基本信息和学籍状态。
 * 与 Libcard 通过 sno 关联，一个学生最多持有一张借书证。
 */
@Data
public class Student {

    /** 主键 ID，数据库自增 */
    private Integer id;

    /** 学号，全局唯一的业务主键，格式如 2021101001 */
    private String sno;

    /** 学生姓名 */
    private String username;

    /** 学生类型，取值为"本科生"或"研究生"，影响借阅上限 */
    private String type;

    /** 所属学院名称 */
    private String college;

    /** 所学专业名称 */
    private String major;

    /** 出生日期 */
    private LocalDate birth;

    /** 籍贯，省市级格式 */
    private String originPlace;

    /** 登录密码，数据库中存储密文 */
    private String password;

    /** 毕业状态：0=未毕业，1=已毕业，已毕业学生不可借书 */
    private Integer graduated;
}
