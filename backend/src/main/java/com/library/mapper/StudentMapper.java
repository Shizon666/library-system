package com.library.mapper;

import com.library.entity.Student;
import org.apache.ibatis.annotations.Param;

/** 学生表 Mapper */
public interface StudentMapper {
    /** 按学号查学生，登录鉴权和借书前身份校验都走这个查询 */
    Student findBySno(@Param("sno") String sno);

    /** 新增学生账户，注册或管理员导入时使用 */
    int insert(Student student);

    /** 编辑个人资料：只允许改姓名/类别/学院/专业/籍贯，不改学号和密码 */
    int update(Student student);

    /** 单独修改密码，需要旧密码验证后才能调用 */
    int updatePassword(@Param("sno") String sno, @Param("password") String password);
}
