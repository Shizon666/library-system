package com.library.mapper;

import org.apache.ibatis.annotations.Mapper;

/** 仪表盘统计 Mapper：聚合各维度数据供首页看板展示 */
@Mapper
public interface DashboardMapper {
    /** 统计注册学生总数，仪表盘"学生总量"卡片的数据源 */
    long countStudents();
}
