package com.library.entity;

import lombok.Data;

/**
 * 图书馆藏副本实体类，每一本实体书对应一条记录。
 * 对应数据库 bookcopy 表，通过 ISBN 关联到 Book 书目记录。
 * 一个 ISBN 下可有多本副本，每本副本有唯一的条形码。
 */
@Data
public class Bookcopy {

    /** 实体书的条形码编号，唯一标识一本实体书，如 BC-2024001 */
    private String barCode;

    /** 对应的 ISBN，关联 book 表 */
    private String ISBN;

    /** 馆藏位置，如"三楼文学区A架"，便于读者找书 */
    private String place;

    /** 副本状态：1=可借，0=已借出，2=已注销（报废） */
    private Integer status;

    /** 注销前的状态，副本从注销恢复时还原到此状态 */
    private Integer oldStatus;
}
