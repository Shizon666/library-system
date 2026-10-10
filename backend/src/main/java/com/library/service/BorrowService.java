package com.library.service;

import com.library.common.BusinessException;
import com.library.common.CardConstants;
import com.library.common.ResultCode;
import com.library.config.LibraryProperties;
import com.library.dto.BorrowDTO;
import com.library.dto.ReportLossDTO;
import com.library.dto.ReturnDTO;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.vo.BorrowingVO;
import com.library.vo.CopySearchResultVO;
import com.library.vo.HistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借阅服务：负责借书、还书、副本检索、遗失赔偿等核心借阅业务。
 * <p>
 * 核心业务规则：
 * - 借书采用四关校验链：证件状态 → 罚款限额 → 借阅上限 → 副本可用性
 * - 还书时自动判断逾期，逾期罚款按天计算，罚款记录由学生后续缴纳
 * - 遗失赔偿按图书定价执行，赔偿后副本自动注销不可再借
 * - 研究生与本科生的借阅上限不同，通过配置文件灵活控制
 */
@Service
@RequiredArgsConstructor
public class BorrowService {

    // 副本状态码 → 中文描述的映射表，用于错误提示中展示可读状态
    private static final Map<Integer, String> COPY_STATUS_TEXT = Map.of(
            0, "借出", 1, "可借", 2, "注销");

    private final LibcardMapper libcardMapper;
    private final StudentMapper studentMapper;
    private final BookcopyMapper bookcopyMapper;
    private final BookMapper bookMapper;
    private final BorrowrecMapper borrowrecMapper;
    private final FineMapper fineMapper;
    private final PayrecMapper payrecMapper;
    private final LibraryProperties props;

    /**
     * 借书主流程，校验链路：证件状态 → 未缴罚款上限 → 借阅数量上限 → 副本可借状态。
     * 支持批量借阅（dto.barCodes 可含多条条码），每本独立校验，任一失败则整体回滚。
     *
     * @param dto         借阅请求，包含借书证号（可选）和条码列表
     * @param loginCardNo 当前登录用户的证件号，dto 未传时作为兜底
     * @return 包含借阅记录号、应还日期、本次借阅数量的汇总信息
     */
    @Transactional
    public Map<String, Object> borrow(BorrowDTO dto, String loginCardNo) {
        // 优先用请求体里的证件号，没传则用登录态的证件号
        String cardNo = StringUtils.hasText(dto.getCardNo()) ? dto.getCardNo() : loginCardNo;
        if (!StringUtils.hasText(cardNo)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请提供借书证号");
        }

        // 查找证件，不存在直接报错
        Libcard card = libcardMapper.findByCardNo(cardNo);
        if (card == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(),
                    "借书证号 [" + cardNo + "] 不存在，请检查输入");
        }
        // 第一关：证件必须是正常状态，挂失/注销都不能借
        if (CardConstants.STATUS_LOST.equals(card.getCardStatus())) {
            throw new BusinessException(ResultCode.CARD_NOT_ACTIVE.getCode(),
                    "借书证 [" + cardNo + "] 处于挂失状态，无法借书，请先办理解挂");
        }
        if (CardConstants.STATUS_CANCELLED.equals(card.getCardStatus())) {
            throw new BusinessException(ResultCode.CARD_NOT_ACTIVE.getCode(),
                    "借书证 [" + cardNo + "] 已被注销，无法借书");
        }
        // 兜底校验：非正常状态一律拦截（防止新增状态类型时漏判）
        if (!CardConstants.STATUS_NORMAL.equals(card.getCardStatus())) {
            throw new BusinessException(ResultCode.CARD_NOT_ACTIVE);
        }

        // 查关联学生信息，用于后续借阅上限判断
        Student student = studentMapper.findBySno(card.getSno());
        if (student == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 第二关：累计未缴罚款超过上限（默认50元）不允许继续借，督促学生及时缴费
        BigDecimal unpaid = fineMapper.sumUnpaidBySno(card.getSno());
        if (unpaid.compareTo(props.getMaxUnpaidFine()) > 0) {
            throw new BusinessException(ResultCode.UNPAID_FINE_LIMIT.getCode(),
                    "累计未缴罚款 " + unpaid + " 元，超过 50 元上限，请先缴纳罚款后再借书");
        }

        // 第三关：研究生和本科生借阅上限不同（默认研究生10本/本科生5本）
        int limit = CardConstants.isGradStudent(student.getType())
                ? props.getGradBorrowLimit() : props.getUndergradBorrowLimit();
        // 统计该学生当前在借数量
        long current = borrowrecMapper.countActiveBySno(card.getSno());
        int toBorrow = dto.getBarCodes().size();
        // 加上本次要借的数量后不能超限
        if (current + toBorrow > limit) {
            String typeLabel = CardConstants.isGradStudent(student.getType()) ? "研究生10本" : "本科生5本";
            throw new BusinessException(ResultCode.BORROW_LIMIT.getCode(),
                    "当前已借 " + current + " 本，超出借阅上限（" + typeLabel + "），无法继续借书");
        }

        LocalDate now = LocalDate.now();
        // 借阅期限默认30天，可在 application.yml 里调整 borrow-days
        LocalDate due = now.plusDays(props.getBorrowDays());
        // 收集本次生成的所有借阅记录号，返回给前端
        List<Integer> recNos = new java.util.ArrayList<>();

        // 第四关：逐本处理，每本副本独立校验通过后才写入数据库
        for (String barCode : dto.getBarCodes()) {
            // 按条码查找副本，不存在则终止
            Bookcopy copy = bookcopyMapper.findByBarCode(barCode);
            if (copy == null) {
                throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "副本不存在: " + barCode);
            }
            // 副本必须是"可借"状态（status=1），借出/注销的都不行
            if (copy.getStatus() != 1) {
                String statusText = COPY_STATUS_TEXT.getOrDefault(copy.getStatus(), "未知");
                throw new BusinessException(ResultCode.COPY_NOT_AVAILABLE.getCode(),
                        "副本 [" + barCode + "] 当前状态为 [" + statusText + "]，无法借出");
            }
            // 额外校验：书目本身是否已下架，下架书不允许借出
            Book book = bookMapper.findByIsbn(copy.getISBN());
            if (book == null || book.getBookStatus() != 1) {
                throw new BusinessException(ResultCode.BOOK_NOT_ON_SHELF);
            }

            // 构建借阅记录：设置学生、条码、借阅日期、应还日期等
            Borrowrec rec = new Borrowrec();
            rec.setSno(card.getSno());
            rec.setBarCode(barCode);
            rec.setBorDate(now);
            rec.setRetDate(due);
            rec.setRetStatus(CardConstants.RET_UNRETURNED);  // 初始状态：未归还
            rec.setFineStatus(CardConstants.FINE_UNPAID);     // 罚款状态：未缴
            borrowrecMapper.insert(rec);
            // 副本状态改为"借出"（status=0）
            bookcopyMapper.updateStatus(barCode, 0);
            // 证件借阅次数 +1
            libcardMapper.updateTimes(cardNo, 1);
            recNos.add(rec.getSerNum());
        }

        // 组装返回结果：记录号列表 + 应还日期 + 本次借阅数量
        Map<String, Object> result = new HashMap<>();
        result.put("recNos", recNos);
        result.put("dueDate", due);
        result.put("count", recNos.size());
        return result;
    }

    /**
     * 还书流程。如果逾期则自动计算罚款：逾期天数 × 每日罚款金额（默认1元/天）。
     * 逾期记录会写入罚款表，由学生后续在罚款管理页面缴纳。
     *
     * @param dto 还书请求，包含副本条码
     * @return 包含借阅记录详情、是否逾期、罚款金额等信息
     */
    @Transactional
    public Map<String, Object> returnBook(ReturnDTO dto) {
        // 按条码查找在借记录，未找到说明该副本未被借出或已归还
        Borrowrec rec = borrowrecMapper.findActiveByBarCode(dto.getBarCode());
        if (rec == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "未找到在借记录");
        }

        LocalDate now = LocalDate.now();
        // 逾期判定：当前日期超过应还日期即为逾期，不足1天按1天算
        boolean overdue = now.isAfter(rec.getRetDate());
        long days = 0;
        BigDecimal amount = BigDecimal.ZERO;
        String retStatus = CardConstants.RET_ON_TIME;  // 默认按时归还

        if (overdue) {
            // 计算逾期天数：应还日期到今天的间隔
            days = java.time.temporal.ChronoUnit.DAYS.between(rec.getRetDate(), now);
            if (days < 1) {
                days = 1;  // 不足1天按1天算，避免0天罚款
            }
            // 罚款 = 逾期天数 × 每日罚款金额（默认1元/天，可在配置文件调整）
            amount = props.getFinePerDay().multiply(BigDecimal.valueOf(days));
            retStatus = CardConstants.RET_OVERDUE;  // 标记为逾期归还
        }

        // 更新借阅记录：归还状态、实际归还日期、逾期天数、罚款金额
        borrowrecMapper.returnBook(rec.getSerNum(), retStatus, now, (int) days, amount,
                overdue ? CardConstants.FINE_UNPAID : CardConstants.FINE_PAID);
        // 副本恢复为可借状态（status=1）
        bookcopyMapper.updateStatus(dto.getBarCode(), 1);

        // 还书后证件借阅次数减1，释放借阅额度
        Libcard card = libcardMapper.findActiveBySno(rec.getSno());
        if (card != null) {
            libcardMapper.updateTimes(card.getCardNo(), -1);
        }

        // 查询关联的书目和学生信息，组装完整的还书结果
        Bookcopy returnedCopy = bookcopyMapper.findByBarCode(dto.getBarCode());
        Book book = returnedCopy != null ? bookMapper.findByIsbn(returnedCopy.getISBN()) : null;
        Student student = studentMapper.findBySno(rec.getSno());

        // 组装返回结果：基础信息 + 逾期信息（如有）
        Map<String, Object> result = new HashMap<>();
        result.put("recNo", rec.getSerNum());
        result.put("barCode", dto.getBarCode());
        result.put("borDate", rec.getBorDate());
        result.put("retDate", rec.getRetDate());
        result.put("studentName", student != null ? student.getUsername() : rec.getSno());
        if (book != null) {
            result.put("bookName", book.getBname());
            result.put("author", book.getAuthor());
            result.put("ISBN", book.getISBN());
        }
        result.put("overdue", overdue);
        // 逾期时额外返回罚款相关字段，供前端展示罚款提示
        if (overdue) {
            result.put("overdueDays", days);
            result.put("fineAmount", amount);
            result.put("fineId", rec.getSerNum());
        }
        return result;
    }

    /**
     * 查询副本详情：按条码查找副本，返回副本状态和所属书目信息。
     * 前端用于借书前确认副本信息。
     *
     * @param barCode 副本条码
     * @return 副本详情（条码、馆藏地点、状态、书名、作者等）
     */
    public Map<String, Object> getCopyInfo(String barCode) {
        // 查找副本，不存在则报错
        Bookcopy copy = bookcopyMapper.findByBarCode(barCode);
        if (copy == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "副本不存在: " + barCode);
        }
        // 查找所属书目信息
        Book book = bookMapper.findByIsbn(copy.getISBN());
        // 组装副本详情
        Map<String, Object> info = new HashMap<>();
        info.put("barCode", copy.getBarCode());
        info.put("place", copy.getPlace());
        info.put("status", copy.getStatus());
        info.put("statusText", COPY_STATUS_TEXT.getOrDefault(copy.getStatus(), "未知"));
        if (book != null) {
            info.put("bookName", book.getBname());
            info.put("author", book.getAuthor());
            info.put("ISBN", book.getISBN());
        }
        return info;
    }

    /**
     * 查询学生当前在借列表：返回所有未归还的借阅记录。
     *
     * @param sno 学号
     * @return 在借记录列表
     */
    public List<BorrowingVO> myBorrowings(String sno) {
        return borrowrecMapper.findActiveBySno(sno);
    }

    /**
     * 查询学生借阅历史：返回所有已完成的借阅记录（含已还、遗失等）。
     *
     * @param sno 学号
     * @return 历史借阅记录列表
     */
    public List<HistoryVO> myHistory(String sno) {
        return borrowrecMapper.findHistoryBySno(sno);
    }

    /**
     * 副本检索：支持按书名/作者/ISBN模糊搜索，返回所有匹配副本及可借状态。
     * 前端借书页面用这个接口展示可借副本列表。
     *
     * @param keyword 搜索关键词（书名/作者/ISBN）
     * @return 匹配结果，包含副本列表和可借状态
     */
    public CopySearchResultVO searchCopyForBorrow(String keyword) {
        // 关键词不能为空
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请输入检索信息");
        }
        // 模糊搜索：按书名/作者/ISBN匹配，返回所有副本（含已借出的）
        List<CopySearchResultVO.CopyBorrowVO> matches =
                bookcopyMapper.searchCopiesForBorrow(keyword.trim());
        CopySearchResultVO result = new CopySearchResultVO();
        if (matches.isEmpty()) {
            result.setFound(false);
            result.setMessage("不存在此书籍");
            return result;
        }
        // 返回所有副本（含借出、可借），方便前端展示完整状态
        result.setFound(true);
        result.setCopies(matches);
        // 兼容旧接口：顺手设置第一个可借副本，方便直接取用
        matches.stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .findFirst()
                .ifPresent(result::setCopy);
        // 所有副本都被借出时设置标记，前端据此显示"全部借出"提示
        if (result.getCopy() == null) {
            result.setAllBorrowed(true);
            result.setMessage("该书目前所有副本已被借出");
        }
        return result;
    }

    /**
     * 遗失赔偿：按图书定价赔偿（定价为空或0则默认50元），赔偿后副本自动注销。
     * 赔偿流程：生成赔偿记录 → 标记借阅记录为遗失 → 副本注销 → 释放借阅额度。
     *
     * @param dto 遗失报告，包含副本条码和可选的借书证号
     * @return 赔偿结果详情（书名、赔偿金额、学生姓名等）
     */
    @Transactional
    public Map<String, Object> reportLostBook(ReportLossDTO dto) {
        // 查找在借记录，必须存在才能报失
        Borrowrec rec = borrowrecMapper.findActiveByBarCode(dto.getBarCode());
        if (rec == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "未找到在借记录");
        }
        // 如果前端传了证件号，校验归属——防止操作别人的书
        if (StringUtils.hasText(dto.getCardNo())) {
            Libcard card = libcardMapper.findByCardNo(dto.getCardNo());
            if (card == null || !card.getSno().equals(rec.getSno())) {
                throw new BusinessException(ResultCode.FAIL.getCode(), "该图书不属于此借书证");
            }
        }

        // 查找副本信息，用于获取ISBN和定价
        Bookcopy copy = bookcopyMapper.findByBarCode(dto.getBarCode());
        if (copy == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "副本不存在");
        }
        // 赔偿金额 = 图书定价，定价缺失时兜底50元（避免赔偿0元）
        Book book = bookMapper.findByIsbn(copy.getISBN());
        BigDecimal price = book != null && book.getPrice() != null && book.getPrice().signum() > 0
                ? book.getPrice() : new BigDecimal("50.00");

        // 生成赔偿支付记录，记录赔偿金额和日期
        LocalDate now = LocalDate.now();
        Payrec payrec = new Payrec();
        payrec.setSno(rec.getSno());
        payrec.setPayAmount(price);
        payrec.setPayDate(now);
        payrecMapper.insert(payrec);

        // 标记借阅记录为"遗失已赔"，罚款直接设为已缴（赔偿即缴费）
        borrowrecMapper.returnBook(rec.getSerNum(), CardConstants.RET_LOST, now, 0, price,
                CardConstants.FINE_PAID);
        // 关联罚款支付记录，便于财务对账
        borrowrecMapper.updateFinePaid(List.of(rec.getSerNum()), payrec.getSerNum());
        // 副本状态改为注销（status=2），永久不可再借
        bookcopyMapper.discardCopy(dto.getBarCode(), copy.getStatus());

        // 释放借阅额度：证件借阅次数减1
        Libcard card = libcardMapper.findActiveBySno(rec.getSno());
        if (card != null) {
            libcardMapper.updateTimes(card.getCardNo(), -1);
        }

        // 组装赔偿结果返回给前端
        Student student = studentMapper.findBySno(rec.getSno());
        Map<String, Object> result = new HashMap<>();
        result.put("recNo", rec.getSerNum());
        result.put("barCode", dto.getBarCode());
        result.put("bookName", book != null ? book.getBname() : "");
        result.put("author", book != null ? book.getAuthor() : "");
        result.put("ISBN", book != null ? book.getISBN() : "");
        result.put("compensationAmount", price);
        result.put("studentName", student != null ? student.getUsername() : rec.getSno());
        return result;
    }
}
