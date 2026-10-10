package com.library.entity;

import lombok.Data;

/**
 * 证件操作记录实体类，跟踪借书证的全生命周期操作。
 * 对应数据库 cardrec 表，记录新办、挂失、补办、注销等操作历史。
 * 每次操作生成一条记录，便于追溯证件变更轨迹。
 */
@Data
public class Cardrec {

    /** 流水号，主键 */
    private Integer serNum;

    /** 操作所关联的学生学号 */
    private String sno;

    /** 操作前的卡号，挂失/注销时记录原卡号 */
    private String originCardNo;

    /** 操作后的新卡号，仅补办操作时有值，其他操作为 null */
    private String newCardNo;

    /** 操作类型：新办 / 挂失 / 补办 / 注销 */
    private String opType;
}
