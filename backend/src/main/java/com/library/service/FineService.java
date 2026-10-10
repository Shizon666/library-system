package com.library.service;

import com.library.common.BusinessException;
import com.library.common.CardConstants;
import com.library.common.ResultCode;
import com.library.dto.FinePayDTO;
import com.library.entity.Borrowrec;
import com.library.entity.FinePayment;
import com.library.entity.Libcard;
import com.library.entity.Payrec;
import com.library.mapper.BorrowrecMapper;
import com.library.mapper.FineMapper;
import com.library.mapper.LibcardMapper;
import com.library.mapper.PayrecMapper;
import com.library.vo.FineVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 罚款业务服务。
 * 提供罚款列表查询（按角色区分可见范围）和个人缴费记录查询。
 * 批量缴费在同一事务内完成，保证数据一致性。
 */
@Service
@RequiredArgsConstructor
public class FineService {

    /** 罚款数据访问（借阅记录中的罚款字段） */
    private final FineMapper fineMapper;

    /** 缴费记录数据访问 */
    private final PayrecMapper payrecMapper;

    /** 借书证数据访问 */
    private final LibcardMapper libcardMapper;

    /** 借阅记录数据访问 */
    private final BorrowrecMapper borrowrecMapper;

    /**
     * 查询罚款列表，角色感知。
     * 学生只能看到自己的罚款，管理员可以查看全部罚款。
     *
     * @param payStatus 缴费状态筛选，可为 null 表示查全部
     * @param role      当前登录用户角色
     * @param sno       学号，学生角色时必传
     * @return 罚款视图列表
     */
    public List<FineVO> listFines(Integer payStatus, String role, String sno) {
        // 学生角色：强制只查自己的罚款
        if ("student".equalsIgnoreCase(role)) {
            // 学号为空则参数错误
            if (!StringUtils.hasText(sno)) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "缺少学号信息");
            }
            // 按学号 + 状态筛选
            return fineMapper.findAllBySno(sno, payStatus);
        }
        // 管理员角色：查看所有罚款
        return fineMapper.findAll(payStatus);
    }

    /**
     * 查询学生个人的全部罚款记录（不分状态）。
     *
     * @param sno 学号
     * @return 该学生所有罚款视图列表
     */
    public List<FineVO> myFines(String sno) {
        return fineMapper.findBySno(sno);
    }

    /**
     * 查询学生的缴费历史记录。
     *
     * @param sno 学号
     * @return 缴费记录列表
     */
    public List<FinePayment> myPayments(String sno) {
        return payrecMapper.findBySno(sno);
    }

    /**
     * 批量缴费：一次性缴纳多笔罚款。
     * 所有罚款必须属于同一学生，且均为未缴状态。
     * 先逐笔校验有效性并累加金额，再生成一条缴费记录，
     * 最后批量更新借阅记录的罚款状态为已缴。
     *
     * @param dto 包含证件号、罚款ID列表、操作人等信息
     * @return 本次缴费的汇总信息
     */
    @Transactional
    public FinePayment payFines(FinePayDTO dto) {
        // 根据证件号查询借书证，确认学生身份
        Libcard card = libcardMapper.findByCardNo(dto.getCardNo());
        if (card == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 逐笔校验：存在性、未缴状态、归属一致性，同时累加金额
        BigDecimal total = BigDecimal.ZERO;
        for (Integer fineId : dto.getFineIds()) {
            // 查询该笔罚款对应的借阅记录
            Borrowrec rec = fineMapper.findBySerNum(fineId);
            // 罚款不存在或金额无效
            if (rec == null || rec.getFineMoney() == null || rec.getFineMoney().signum() <= 0) {
                throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "罚款不存在: " + fineId);
            }
            // 已缴过的罚款不能重复缴纳
            if (CardConstants.FINE_PAID.equals(rec.getFineStatus())) {
                throw new BusinessException(ResultCode.FINE_ALREADY_PAID);
            }
            // 确保所有罚款都属于同一学生，防止张冠李戴
            if (!card.getSno().equals(rec.getSno())) {
                throw new BusinessException(ResultCode.FAIL.getCode(),
                        "罚款" + fineId + "不属于该学生");
            }
            // 累加本次罚款金额
            total = total.add(rec.getFineMoney());
        }

        // 构建缴费记录并持久化
        Payrec payrec = new Payrec();
        payrec.setSno(card.getSno());
        payrec.setPayAmount(total);
        payrec.setPayDate(LocalDate.now());
        payrecMapper.insert(payrec);

        // 一次性把这批罚款标记为已缴，并关联到本次缴费记录
        borrowrecMapper.updateFinePaid(dto.getFineIds(), payrec.getSerNum());

        // 组装返回的缴费汇总对象
        FinePayment payment = new FinePayment();
        payment.setPaymentId(payrec.getSerNum());
        payment.setCardNo(dto.getCardNo());
        payment.setSno(card.getSno());
        payment.setPaymentAmount(total);
        payment.setPaymentTime(payrec.getPayDate());
        payment.setOperator(dto.getOperator());
        payment.setRemark(dto.getRemark());
        return payment;
    }
}
