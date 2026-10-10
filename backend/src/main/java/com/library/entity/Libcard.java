package com.library.entity;

import lombok.Data;

import java.time.LocalDate;

/**
 * 借书证实体类，记录证件状态与当前在借数量。
 * 对应数据库 libcard 表，与 Student 通过 sno 一对一关联。
 * 证件有正常、挂失、注销三种状态，不同状态影响借还操作权限。
 */
@Data
public class Libcard {

    /** 借书证号，格式如 C2024001，由系统自动生成 */
    private String cardNo;

    /** 持证学生学号，关联 student 表 */
    private String sno;

    /** 持证学生姓名，冗余存储便于查询展示 */
    private String sname;

    /** 学生类型（本科生/研究生），冗余字段用于快速判断借阅限额 */
    private String type;

    /** 所属学院，冗余字段 */
    private String college;

    /** 所学专业，冗余字段 */
    private String major;

    /** 出生日期，冗余字段 */
    private LocalDate birth;

    /** 籍贯，冗余字段 */
    private String originPlace;

    /** 证件状态：正常 / 挂失 / 注销，挂失和注销状态下不可借书 */
    private String cardStatus;

    /** 当前在借数量，每次借书+1还书-1，用于判断是否超出借阅上限 */
    private Integer times;
}
