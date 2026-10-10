package com.library.service;

import com.library.common.BusinessException;
import com.library.common.CardConstants;
import com.library.common.ResultCode;
import com.library.dto.LoginDTO;
import com.library.entity.Admin;
import com.library.entity.Libcard;
import com.library.entity.Student;
import com.library.mapper.AdminMapper;
import com.library.mapper.LibcardMapper;
import com.library.mapper.StudentMapper;
import com.library.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录认证服务。
 * 支持双角色登录：管理员通过用户名登录，学生通过借书证号登录。
 * 学生登录时会校验证件状态，注销和挂失的证件将被拒绝。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /** 管理员数据访问 */
    private final AdminMapper adminMapper;

    /** 借书证数据访问 */
    private final LibcardMapper libcardMapper;

    /** 学生数据访问 */
    private final StudentMapper studentMapper;

    /**
     * 统一登录入口，根据角色分发到不同的认证逻辑。
     * 管理员：用户名 + 密码直接匹配管理员表。
     * 学生：借书证号查卡 -> 卡上 sno 查学生 -> 校验密码 -> 校验证件状态。
     *
     * @param dto 登录请求，包含角色、用户名/借书证号、密码
     * @return 登录成功后的用户信息（角色、显示名、证件号等）
     */
    public LoginVO login(LoginDTO dto) {
        // ========== 管理员分支：用用户名直接匹配 ==========
        if ("admin".equalsIgnoreCase(dto.getRole())) {
            // 根据用户名查询管理员
            Admin admin = adminMapper.findByUsername(dto.getUsername());
            // 管理员不存在或密码不匹配
            if (admin == null || !admin.getPassword().equals(dto.getPassword())) {
                throw new BusinessException(ResultCode.LOGIN_FAIL);
            }
            // 构建管理员登录响应
            LoginVO vo = new LoginVO();
            vo.setRole("admin");
            vo.setUsername(admin.getUsername());
            vo.setDisplayName(admin.getUsername());
            return vo;
        }

        // ========== 学生分支：用借书证号登录（注意不是学号） ==========
        if ("student".equalsIgnoreCase(dto.getRole())) {
            // 先用借书证号查卡
            Libcard card = libcardMapper.findByCardNo(dto.getUsername());
            if (card == null) {
                throw new BusinessException(ResultCode.LOGIN_FAIL);
            }
            // 通过卡上的 sno 关联查询学生信息
            Student student = studentMapper.findBySno(card.getSno());
            // 学生不存在或密码不匹配
            if (student == null || !student.getPassword().equals(dto.getPassword())) {
                throw new BusinessException(ResultCode.LOGIN_FAIL);
            }

            // 挂失和注销的证件不允许登录，给明确的提示信息
            if (CardConstants.STATUS_CANCELLED.equals(card.getCardStatus())) {
                throw new BusinessException(ResultCode.LOGIN_FAIL.getCode(),
                        "该借书证已被注销，无法登录");
            }
            if (CardConstants.STATUS_LOST.equals(card.getCardStatus())) {
                throw new BusinessException(ResultCode.LOGIN_FAIL.getCode(),
                        "该借书证已挂失，请先解挂或补办");
            }
            // 兜底：如果卡状态不是正常（比如冻结等），一律拒绝
            if (!CardConstants.STATUS_NORMAL.equals(card.getCardStatus())) {
                throw new BusinessException(ResultCode.LOGIN_FAIL);
            }

            // 构建学生登录响应，携带证件号和学号供后续使用
            LoginVO vo = new LoginVO();
            vo.setRole("student");
            vo.setUsername(card.getCardNo());
            vo.setCardNo(card.getCardNo());
            vo.setSno(card.getSno());
            vo.setDisplayName(student.getUsername());
            return vo;
        }

        // 角色既不是 admin 也不是 student，参数非法
        throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "角色必须是 admin 或 student");
    }
}
