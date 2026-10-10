package com.library.service;

import com.library.common.BusinessException;
import com.library.common.CardConstants;
import com.library.common.ResultCode;
import com.library.config.LibraryProperties;
import com.library.dto.CardCreateDTO;
import com.library.dto.CardOpDTO;
import com.library.entity.Cardrec;
import com.library.entity.Libcard;
import com.library.entity.Student;
import com.library.mapper.CardrecMapper;
import com.library.mapper.FineMapper;
import com.library.mapper.LibcardMapper;
import com.library.mapper.StudentMapper;
import com.library.mapper.BorrowrecMapper;
import com.library.vo.BorrowCheckVO;
import com.library.vo.CardDetailVO;
import com.library.vo.CardRecordVO;
import com.library.vo.StudentCardQueryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 借书证生命周期管理：创建、挂失、补办、注销。
 * <p>
 * 核心业务规则：
 * - 证件号 = 学号 + 3位序号（如 202110001001），补办时序号递增
 * - 一个学号同一时间只能有一张正常证件，补办前必须挂失或注销旧证
 * - 注销仅限已毕业学生，防止在读学生无证可用
 * - 补办新证继承旧证借阅次数，对学生透明
 * - 借阅资格预检综合校验证件状态、罚款、额度三关
 */
@Service
@RequiredArgsConstructor
public class CardService {

    // 学号格式：4位入学年 + 1位类别(1本科/2研究生) + 5位序号
    private static final Pattern SNO_PATTERN = Pattern.compile("^\\d{4}[12]\\d{5}$");

    private final StudentMapper studentMapper;
    private final LibcardMapper libcardMapper;
    private final CardrecMapper cardrecMapper;
    private final BorrowrecMapper borrowrecMapper;
    private final FineMapper fineMapper;
    private final LibraryProperties props;

    /**
     * 新建借书证。一个学号同一时间只能有一张正常证件。
     * 证件号自动生成：学号 + 3位序号（001起）。
     *
     * @param dto 创建请求，包含学号和姓名
     * @return 新证件号
     */
    @Transactional
    public Map<String, String> createCard(CardCreateDTO dto) {
        // 校验学号格式：10位数字编码
        validateSno(dto.getSno());
        // 校验学号存在且姓名匹配，防止办错证
        Student student = requireStudentMatch(dto.getSno(), dto.getStudentName());

        // 已有正常证件，不允许重复创建（一个学生同时只能有一张有效证）
        if (libcardMapper.findActiveBySno(dto.getSno()) != null) {
            throw new BusinessException(ResultCode.STUDENT_HAS_CARD);
        }

        // 有历史证件记录但没有挂失的，提示先挂失再办新证
        // （正常流程：旧证挂失/注销 → 才能办新证）
        long cardCount = libcardMapper.countBySno(dto.getSno());
        if (cardCount > 0 && !libcardMapper.existsLostBySno(dto.getSno())) {
            throw new BusinessException(ResultCode.INVALID_OP.getCode(),
                    "该学号已有借书证记录，请先挂失原证后再新办");
        }

        // 自动生成下一个证件号：学号 + 3位递增序号
        String cardNo = generateNextCardNoBySno(dto.getSno());
        // 证件号不能重复（防并发场景）
        if (libcardMapper.findByCardNo(cardNo) != null) {
            throw new BusinessException(ResultCode.CARD_EXISTS);
        }

        // 从学生信息构建证件对象，冗余存储学生信息便于查询
        Libcard card = buildLibcardFromStudent(student, cardNo);
        libcardMapper.insert(card);

        // 写入证件操作记录（创建）
        insertCardrec(student.getSno(), null, cardNo, CardConstants.OP_CREATE);

        // 返回新证件号
        Map<String, String> result = new HashMap<>();
        result.put("cardNo", cardNo);
        return result;
    }

    /**
     * 挂失借书证：只有正常状态的证件才能挂失。
     * 挂失后该证件无法借书，但已借的书仍需归还。
     *
     * @param cardNo 要挂失的证件号
     */
    @Transactional
    public void reportLoss(String cardNo) {
        // 查找证件，不存在则报错
        Libcard card = requireCard(cardNo);
        // 仅正常状态可挂失，挂失/注销的都不能再挂失
        if (!CardConstants.STATUS_NORMAL.equals(card.getCardStatus())) {
            throw new BusinessException(ResultCode.INVALID_OP.getCode(), "仅正常证件可挂失");
        }
        // 更新证件状态为挂失
        libcardMapper.updateCardStatus(cardNo, CardConstants.STATUS_LOST);
        // 写入证件操作记录（挂失）
        insertCardrec(card.getSno(), cardNo, null, CardConstants.OP_LOSS);
    }

    /**
     * 补办：旧证标记为注销，生成新证并继承借阅次数。
     * 挂失和注销状态的证件都可以补办（给注销证件也留了口子，方便特殊场景）。
     *
     * @param cardNo 旧证件号
     * @param dto    补办请求（可选指定新证件号）
     * @return 新证件号
     */
    @Transactional
    public Map<String, String> replace(String cardNo, CardOpDTO dto) {
        // 查找旧证件
        Libcard oldCard = requireCard(cardNo);
        // 仅挂失或注销状态可补办，正常证件不能直接补办
        if (!CardConstants.STATUS_LOST.equals(oldCard.getCardStatus())
                && !CardConstants.STATUS_CANCELLED.equals(oldCard.getCardStatus())) {
            throw new BusinessException(ResultCode.INVALID_OP.getCode(), "仅挂失或注销证件可补办");
        }

        // 新证件号：前端可指定，未指定则自动递增序号
        String newCardNo = StringUtils.hasText(dto.getNewCardNo())
                ? dto.getNewCardNo()
                : incrementCardNo(cardNo);
        // 校验新证号格式：必须是学号+3位数字
        validateCardNoFormat(newCardNo, oldCard.getSno());
        // 新证号不能已存在
        if (libcardMapper.findByCardNo(newCardNo) != null) {
            throw new BusinessException(ResultCode.CARD_EXISTS);
        }
        // 该学生不能已有正常证件（防重复办证）
        if (libcardMapper.findActiveBySno(oldCard.getSno()) != null) {
            throw new BusinessException(ResultCode.STUDENT_HAS_CARD.getCode(), "该学生已有正常借书证");
        }

        // 先把旧证注销，再创建新证（事务保证原子性）
        libcardMapper.updateCardStatus(cardNo, CardConstants.STATUS_CANCELLED);

        // 查找学生信息，构建新证件
        Student student = studentMapper.findBySno(oldCard.getSno());
        Libcard newCard = buildLibcardFromStudent(student, newCardNo);
        // 新证继承旧证的借阅累计次数，这对学生透明（不影响借阅额度）
        newCard.setTimes(oldCard.getTimes() != null ? oldCard.getTimes() : 0);
        libcardMapper.insert(newCard);

        // 写入证件操作记录（补办），记录新旧证件号关联
        insertCardrec(oldCard.getSno(), cardNo, newCardNo, CardConstants.OP_REPLACE);

        // 返回新证件号
        Map<String, String> result = new HashMap<>();
        result.put("cardNo", newCardNo);
        result.put("newCardNo", newCardNo);
        return result;
    }

    /**
     * 注销借书证。只有已毕业学生才能注销（防止在读学生注销后无证可用）。
     * 注销后证件永久失效，不可恢复。
     *
     * @param cardNo 要注销的证件号
     */
    @Transactional
    public void cancel(String cardNo) {
        // 查找证件
        Libcard card = requireCard(cardNo);
        // 已注销的不能重复操作
        if (CardConstants.STATUS_CANCELLED.equals(card.getCardStatus())) {
            throw new BusinessException(ResultCode.INVALID_OP.getCode(), "证件已注销");
        }
        // 注销必须满足已毕业条件，防止在读学生误操作
        Student student = studentMapper.findBySno(card.getSno());
        if (student == null || student.getGraduated() == null || student.getGraduated() != 1) {
            throw new BusinessException(ResultCode.INVALID_OP.getCode(), "该学生尚未毕业，无法注销借书证");
        }
        // 更新证件状态为注销
        libcardMapper.updateCardStatus(cardNo, CardConstants.STATUS_CANCELLED);
        // 写入证件操作记录（注销）
        insertCardrec(card.getSno(), cardNo, null, CardConstants.OP_CANCEL);
    }

    /**
     * 查询学生的证件操作记录列表。
     *
     * @param sno 学号
     * @return 证件操作记录列表
     */
    public List<CardRecordVO> listRecords(String sno) {
        return cardrecMapper.findAllRecords(null, sno);
    }

    /**
     * 查询所有证件操作记录：支持按操作类型和学号筛选。
     * opType: 1创建 2挂失 3补办 4注销，转成常量再查询。
     *
     * @param opType 操作类型（可选，1-4）
     * @param sno    学号（可选）
     * @return 证件操作记录列表
     */
    public List<CardRecordVO> listAllRecords(Integer opType, String sno) {
        // 将前端的操作类型数字转成系统常量
        String opTypeStr = null;
        if (opType != null) {
            opTypeStr = switch (opType) {
                case 1 -> CardConstants.OP_CREATE;
                case 2 -> CardConstants.OP_LOSS;
                case 3 -> CardConstants.OP_REPLACE;
                case 4 -> CardConstants.OP_CANCEL;
                default -> null;
            };
        }
        return cardrecMapper.findAllRecords(opTypeStr, sno);
    }

    /**
     * 查询证件详情：自动识别关键词是学号还是证号。
     * 学号查正常证件，证号查任意状态。
     *
     * @param keyword 学号或证件号
     * @return 证件详情
     */
    public CardDetailVO queryCard(String keyword) {
        // 关键词不能为空
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请输入借书证号或学号");
        }
        String kw = keyword.trim();
        // 学号格式匹配时按学号查正常证件，否则按证件号查任意状态
        Libcard card = SNO_PATTERN.matcher(kw).matches()
                ? libcardMapper.findActiveBySno(kw)
                : libcardMapper.findByCardNo(kw);
        // 学号匹配但无正常证件，给出明确提示
        if (card == null && SNO_PATTERN.matcher(kw).matches()) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "该学号暂无正常借书证");
        }
        if (card == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "未找到借书证");
        }
        // 组装证件详情VO
        return toCardDetail(card);
    }

    /**
     * 按证件号获取证件详情。
     *
     * @param cardNo 证件号
     * @return 证件详情
     */
    public CardDetailVO getCardDetail(String cardNo) {
        return toCardDetail(requireCard(cardNo));
    }

    /**
     * 预览下一个可用证件号：用于前端办证前展示。
     *
     * @param sno 学号
     * @return 预览的证件号
     */
    public String previewNextCardNo(String sno) {
        validateSno(sno);
        return generateNextCardNoBySno(sno);
    }

    /**
     * 预览补办后的证件号：用于前端补办前展示。
     *
     * @param cardNo 当前证件号
     * @return 预览的新证件号
     */
    public String previewReplaceCardNo(String cardNo) {
        Libcard card = requireCard(cardNo);
        return incrementCardNo(card.getCardNo());
    }

    /**
     * 借阅资格预检：在实际借书前调用，告诉前端"这张证能不能借、还能借几本"。
     * 支持传学号或借书证号，学号会自动关联到该学生的正常证件。
     * 校验三关：证件状态 → 未缴罚款 → 借阅额度。
     *
     * @param cardNo 证件号或学号
     * @return 借阅资格检查结果（是否可借、剩余额度、原因等）
     */
    public BorrowCheckVO checkBorrowEligibility(String cardNo) {
        // 支持学号或借书证号：如果是学号格式，自动查找该学号的正常借书证
        String actualCardNo = cardNo;
        if (StringUtils.hasText(cardNo) && SNO_PATTERN.matcher(cardNo.trim()).matches()) {
            Libcard card = libcardMapper.findActiveBySno(cardNo.trim());
            if (card == null) {
                throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "该学号暂无正常借书证");
            }
            actualCardNo = card.getCardNo();
        }
        // 获取证件详情（含在借数、未缴罚款等）
        CardDetailVO detail = getCardDetail(actualCardNo);
        BorrowCheckVO vo = new BorrowCheckVO();
        // 基本信息映射
        vo.setCardNo(detail.getCardNo());
        vo.setSno(detail.getSno());
        vo.setStudentName(detail.getStudentName());
        vo.setBorrowLimit(detail.getBorrowLimit());
        vo.setCurrentBorrowCount(detail.getCurrentBorrowCount());

        // 第一关：证件状态必须正常（status=1），挂失/注销都不能借
        if (detail.getStatus() == null || detail.getStatus() != 1) {
            vo.setCanBorrow(false);
            // 根据状态码给出具体原因提示
            vo.setReason(switch (detail.getStatus() != null ? detail.getStatus() : -1) {
                case 0 -> "借书证已注销，无法借书";
                case 2 -> "借书证已挂失，无法借书";
                default -> "借书证状态异常，无法借书";
            });
            vo.setRemainingSlots(0);
            return vo;
        }

        // 第二关：未缴罚款不能超过上限（默认50元），超过需先缴费
        if (detail.getUnpaidFineAmount() != null
                && detail.getUnpaidFineAmount().compareTo(props.getMaxUnpaidFine()) > 0) {
            vo.setCanBorrow(false);
            vo.setReason("未缴罚款超过 " + props.getMaxUnpaidFine() + " 元，请先缴纳罚款");
            vo.setRemainingSlots(0);
            return vo;
        }

        // 第三关：当前已借数量不能超过借阅上限，计算剩余可借额度
        long current = detail.getCurrentBorrowCount() != null ? detail.getCurrentBorrowCount() : 0;
        int limit = detail.getBorrowLimit() != null ? detail.getBorrowLimit() : props.getUndergradBorrowLimit();
        int remaining = (int) Math.max(0, limit - current);

        // 已达上限，无法继续借书
        if (remaining <= 0) {
            vo.setCanBorrow(false);
            vo.setReason("已达借阅上限（" + limit + " 本），无法继续借书");
            vo.setRemainingSlots(0);
            return vo;
        }

        // 三关全部通过，可以借书，返回剩余可借数量
        vo.setCanBorrow(true);
        vo.setRemainingSlots(remaining);
        return vo;
    }

    /**
     * 查询学生办证资格：返回学生信息 + 证件状态 + 可办操作 + 预览证号。
     * 前端办证页面用此接口展示学生信息和可执行的操作。
     *
     * @param sno 学号
     * @return 学生办证资格信息
     */
    public StudentCardQueryVO queryStudentCard(String sno) {
        // 校验学号格式
        validateSno(sno);
        // 查找学生信息，不存在则报错
        Student student = studentMapper.findBySno(sno);
        if (student == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "学号不存在，请确认学号已在系统中登记");
        }

        // 组装学生基本信息
        StudentCardQueryVO vo = new StudentCardQueryVO();
        vo.setSno(student.getSno());
        vo.setStudentName(student.getUsername());
        vo.setStudentType(student.getType());
        vo.setCollege(student.getCollege());
        vo.setMajor(student.getMajor());
        vo.setGraduated(student.getGraduated() != null ? student.getGraduated() : 0);

        // 优先展示正常证件，没有则展示最近一张（挂失/注销的）
        Libcard activeCard = libcardMapper.findActiveBySno(sno);
        Libcard displayCard = activeCard != null ? activeCard : libcardMapper.findLatestBySno(sno);
        if (displayCard != null) {
            vo.setCardNo(displayCard.getCardNo());
            // 状态码转换：内部存储用字符串，前端展示用数字
            vo.setStatus(CardConstants.cardStatusToCode(displayCard.getCardStatus()));
        }

        // 可办新证条件：无正常证件 且（从未办过 或 有挂失记录）
        boolean hasActive = activeCard != null;
        long cardCount = libcardMapper.countBySno(sno);
        boolean canCreate = !hasActive && (cardCount == 0 || libcardMapper.existsLostBySno(sno));
        vo.setCanCreate(canCreate);
        if (canCreate) {
            // 可办新证：预览自动生成的证件号
            vo.setPreviewCardNo(generateNextCardNoBySno(sno));
        } else if (displayCard != null
                && (CardConstants.STATUS_LOST.equals(displayCard.getCardStatus())
                || CardConstants.STATUS_CANCELLED.equals(displayCard.getCardStatus()))) {
            // 有挂失/注销证件：预览补办后的证件号
            vo.setPreviewCardNo(incrementCardNo(displayCard.getCardNo()));
        }

        return vo;
    }

    /**
     * 从学生信息构建证件对象，做冗余存储方便查询（证件表不再关联学生表）。
     * 这样即使学生信息变更，证件上的历史信息仍可追溯。
     *
     * @param student 学生实体
     * @param cardNo  证件号
     * @return 证件实体
     */
    private Libcard buildLibcardFromStudent(Student student, String cardNo) {
        Libcard card = new Libcard();
        card.setCardNo(cardNo);
        card.setSno(student.getSno());
        // 冗余存储学生信息，避免证件查询时再关联学生表
        card.setSname(student.getUsername());
        card.setType(student.getType());
        card.setCollege(student.getCollege());
        card.setMajor(student.getMajor());
        card.setBirth(student.getBirth());
        card.setOriginPlace(student.getOriginPlace());
        // 新证件默认正常状态，借阅次数为0
        card.setCardStatus(CardConstants.STATUS_NORMAL);
        card.setTimes(0);
        return card;
    }

    /**
     * 组装证件详情VO：关联学生信息和借阅统计。
     * 学生被删时降级用证件表冗余字段，保证查询不报错。
     *
     * @param card 证件实体
     * @return 证件详情VO
     */
    private CardDetailVO toCardDetail(Libcard card) {
        // 查找关联学生信息
        Student student = studentMapper.findBySno(card.getSno());
        CardDetailVO vo = new CardDetailVO();
        vo.setCardNo(card.getCardNo());
        vo.setSno(card.getSno());
        // 状态码转换：字符串→数字，前端用数字展示
        vo.setStatus(CardConstants.cardStatusToCode(card.getCardStatus()));
        if (student != null) {
            // 学生存在：从学生表取最新信息
            vo.setStudentName(student.getUsername());
            vo.setCollege(student.getCollege());
            vo.setMajor(student.getMajor());
            int studentType = CardConstants.isGradStudent(student.getType()) ? 2 : 1;
            vo.setStudentType(studentType);
            // 研究生和本科生借阅上限不同，从配置读取
            int limit = studentType == 2
                    ? props.getGradBorrowLimit() : props.getUndergradBorrowLimit();
            vo.setBorrowLimit(limit);
        } else {
            // 学生被删：降级用证件表冗余字段
            vo.setStudentName(card.getSname());
            vo.setCollege(card.getCollege());
            vo.setMajor(card.getMajor());
        }
        // 统计当前在借数量和未缴罚款金额
        vo.setCurrentBorrowCount(borrowrecMapper.countActiveBySno(card.getSno()));
        vo.setUnpaidFineAmount(fineMapper.sumUnpaidBySno(card.getSno()));
        return vo;
    }

    /**
     * 生成下一个证件号：在学号后追加3位序号，从该学号已有最大序号+1开始。
     * 例如学号 202110001，第一张证 = 202110001001，补办后 = 202110001002，以此类推。
     * 序号最大999，超过则报错（实际场景几乎不会用完）。
     *
     * @param sno 学号
     * @return 生成的证件号
     */
    private String generateNextCardNoBySno(String sno) {
        validateSno(sno);
        // 查询该学号已有的最大证件号
        String max = libcardMapper.findMaxCardNoBySnoPrefix(sno);
        int next = 1;
        // 从最大序号+1开始，解析末3位数字
        if (max != null && max.length() == sno.length() + 3) {
            try {
                next = Integer.parseInt(max.substring(sno.length())) + 1;
            } catch (NumberFormatException ignored) {
                next = 1;  // 解析失败默认从1开始
            }
        }
        // 序号不能超过999
        if (next > 999) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "该学号证号流水号已用尽（最大999）");
        }
        // 拼接：学号 + 3位补零序号
        return sno + String.format("%03d", next);
    }

    /**
     * 递增证件号序号：用于补办时生成新证号。
     * 取末3位数字+1，前面的学号部分不变。
     *
     * @param cardNo 当前证件号
     * @return 递增后的新证件号
     */
    private String incrementCardNo(String cardNo) {
        // 证件号至少4位（学号+序号）
        if (!StringUtils.hasText(cardNo) || cardNo.length() < 4) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "借书证号格式错误");
        }
        // 分离学号前缀和末3位序号
        String prefix = cardNo.substring(0, cardNo.length() - 3);
        String suffix = cardNo.substring(cardNo.length() - 3);
        int seq;
        try {
            seq = Integer.parseInt(suffix);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "借书证号末三位必须为数字");
        }
        // 序号不能超过999
        if (seq >= 999) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "证号流水号已用尽（最大999）");
        }
        // 序号+1，补零到3位
        return prefix + String.format("%03d", seq + 1);
    }

    /**
     * 校验证件号格式：必须以学号开头，末3位为数字。
     *
     * @param cardNo 证件号
     * @param sno    学号
     */
    private void validateCardNoFormat(String cardNo, String sno) {
        // 证件号必须以学号开头，总长度=学号长度+3
        if (!cardNo.startsWith(sno) || cardNo.length() != sno.length() + 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "新证号格式应为学号加三位序号");
        }
        // 末3位必须是数字
        try {
            Integer.parseInt(cardNo.substring(sno.length()));
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "证号末三位必须为数字");
        }
    }

    /**
     * 校验学生存在且姓名匹配：防止办错证。
     *
     * @param sno         学号
     * @param studentName 姓名
     * @return 学生实体
     */
    private Student requireStudentMatch(String sno, String studentName) {
        // 学号必须存在
        Student student = studentMapper.findBySno(sno);
        if (student == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "学号不存在，请确认学号已在系统中登记");
        }
        // 姓名必须匹配（防止办错证），忽略前后空格
        if (!StringUtils.hasText(studentName)
                || !student.getUsername().trim().equals(studentName.trim())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "学号与姓名不匹配");
        }
        return student;
    }

    /**
     * 校验学号格式：10位数字编码，4位入学年+1位类别+5位序号。
     * 入学年范围2000-2099。
     *
     * @param sno 学号
     */
    private void validateSno(String sno) {
        // 基本格式校验：10位数字，第5位为1(本科)或2(研究生)
        if (!StringUtils.hasText(sno) || !SNO_PATTERN.matcher(sno).matches()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "学号格式错误，应为10位编码 YYYY T MM NNN");
        }
        // 入学年份合理性校验
        int year = Integer.parseInt(sno.substring(0, 4));
        if (year < 2000 || year > 2099) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "学号格式错误，应为10位编码 YYYY T MM NNN");
        }
    }

    /**
     * 查找证件，不存在则抛出异常。
     *
     * @param cardNo 证件号
     * @return 证件实体
     */
    private Libcard requireCard(String cardNo) {
        Libcard card = libcardMapper.findByCardNo(cardNo);
        if (card == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return card;
    }

    /**
     * 插入证件操作记录：创建/挂失/补办/注销都会调用。
     * 记录操作类型和新旧证件号关联，便于审计追溯。
     *
     * @param sno      学号
     * @param origin   原证件号（创建时为null）
     * @param newCard  新证件号（挂失/注销时为null）
     * @param opType   操作类型常量
     */
    private void insertCardrec(String sno, String origin, String newCard, String opType) {
        Cardrec rec = new Cardrec();
        rec.setSno(sno);
        rec.setOriginCardNo(origin);
        rec.setNewCardNo(newCard);
        rec.setOpType(opType);
        cardrecMapper.insert(rec);
    }
}
