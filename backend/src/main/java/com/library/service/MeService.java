package com.library.service;

import com.library.common.BusinessException;
import com.library.common.ResultCode;
import com.library.dto.ChangePasswordDTO;
import com.library.entity.Libcard;
import com.library.entity.Student;
import com.library.mapper.LibcardMapper;
import com.library.mapper.StudentMapper;
import com.library.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 个人中心服务。
 * 学生查看自己的档案信息和修改密码。
 * 档案中的证件号优先使用登录时携带的值，缺失时自动查询有效卡。
 */
@Service
@RequiredArgsConstructor
public class MeService {

    /** 学生数据访问 */
    private final StudentMapper studentMapper;

    /** 借书证数据访问 */
    private final LibcardMapper libcardMapper;

    /**
     * 获取学生个人档案。
     * cardNo 优先使用登录时携带的值，未传则根据学号查有效借书证。
     *
     * @param sno    学号，必传
     * @param cardNo 证件号，可选（登录时由 AuthService 写入 LoginVO）
     * @return 学生档案视图
     */
    public ProfileVO getProfile(String sno, String cardNo) {
        // 学号为空则直接报错
        if (!StringUtils.hasText(sno)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "缺少学号信息");
        }
        // 查询学生基本信息
        Student student = studentMapper.findBySno(sno);
        if (student == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "学生不存在");
        }

        // 填充基本档案字段
        ProfileVO vo = new ProfileVO();
        vo.setSno(student.getSno());
        vo.setStudentName(student.getUsername());
        vo.setType(student.getType());
        vo.setCollege(student.getCollege());
        vo.setMajor(student.getMajor());

        // 证件号：优先用登录时传入的，没有的话再查有效卡
        if (StringUtils.hasText(cardNo)) {
            vo.setCardNo(cardNo);
        } else {
            // 登录时未携带证件号，根据学号查一张有效的借书证
            Libcard card = libcardMapper.findActiveBySno(sno);
            if (card != null) {
                vo.setCardNo(card.getCardNo());
            }
        }
        return vo;
    }

    /**
     * 修改密码。
     * 需要验证原密码正确，且新密码不能与原密码相同。
     *
     * @param sno 学号
     * @param dto 包含旧密码和新密码的请求体
     */
    @Transactional
    public void changePassword(String sno, ChangePasswordDTO dto) {
        // 查询学生记录
        Student student = studentMapper.findBySno(sno);
        if (student == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "学生不存在");
        }
        // 校验原密码是否正确
        if (!student.getPassword().equals(dto.getOldPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "原密码不正确");
        }
        // 新密码不能与旧密码相同
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "新密码不能与原密码相同");
        }
        // 持久化新密码
        studentMapper.updatePassword(sno, dto.getNewPassword());
    }
}
