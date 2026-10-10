package com.library.controller;

import com.library.common.Result;
import com.library.dto.ChangePasswordDTO;
import com.library.entity.FinePayment;
import com.library.service.BorrowService;
import com.library.service.FineService;
import com.library.service.MeService;
import com.library.vo.BorrowingVO;
import com.library.vo.FineVO;
import com.library.vo.HistoryVO;
import com.library.vo.ProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生自助端控制器。
 * <p>
 * 提供学生个人信息查看、密码修改，以及在借图书、借阅历史、
 * 罚款记录等只读查询功能。所有接口均需学生身份登录后访问。
 * </p>
 */
@Tag(name = "个人查询")
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    /** 借阅服务，提供在借和历史记录查询 */
    private final BorrowService borrowService;

    /** 罚款服务，提供罚款和缴费记录查询 */
    private final FineService fineService;

    /** 个人中心服务，提供个人信息和密码修改 */
    private final MeService meService;

    /**
     * 查询当前学生的个人信息。
     * <p>
     * 通过请求头中的学号和证号查询学生基本信息，包括姓名、学院、
     * 借书证状态、在借数量等。
     * </p>
     *
     * @param sno    请求头中的学生学号
     * @param cardNo 请求头中的借书证号
     * @return 学生个人信息概览
     */
    @Operation(summary = "个人信息")
    @GetMapping("/profile")
    public Result<ProfileVO> profile(
            @RequestHeader(value = "X-Sno", required = false) String sno,
            @RequestHeader(value = "X-Card-No", required = false) String cardNo) {
        // 两个参数均从请求头获取，由网关或登录拦截器注入
        return Result.success(meService.getProfile(sno, cardNo));
    }

    /**
     * 修改登录密码。
     * <p>
     * 学生自助修改密码，需提供旧密码进行验证。
     * 修改成功后前端通常引导重新登录。
     * </p>
     *
     * @param sno 请求头中的学生学号，用于定位账户
     * @param dto 包含旧密码和新密码的请求体
     * @return 操作成功返回空数据
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(
            @RequestHeader(value = "X-Sno", required = false) String sno,
            @Valid @RequestBody ChangePasswordDTO dto) {
        // Service 层先校验旧密码，再更新为新密码
        meService.changePassword(sno, dto);
        return Result.success();
    }

    /**
     * 查询当前在借图书。
     * <p>
     * 返回指定学生当前尚未归还的借阅记录，包含书名、借出日期、应还日期等。
     * </p>
     *
     * @param sno 学生学号
     * @return 在借图书列表
     */
    @Operation(summary = "当前在借图书")
    @GetMapping("/borrowings")
    public Result<List<BorrowingVO>> borrowings(@RequestParam String sno) {
        return Result.success(borrowService.myBorrowings(sno));
    }

    /**
     * 查询借阅历史。
     * <p>
     * 返回指定学生的所有历史借还记录，包含已归还和当前在借的图书。
     * </p>
     *
     * @param sno 学生学号
     * @return 借阅历史记录列表
     */
    @Operation(summary = "借书历史")
    @GetMapping("/history")
    public Result<List<HistoryVO>> history(@RequestParam String sno) {
        return Result.success(borrowService.myHistory(sno));
    }

    /**
     * 查询罚款情况。
     * <p>
     * 返回指定学生名下所有罚款记录，包含未缴和已缴的罚款。
     * </p>
     *
     * @param sno 学生学号
     * @return 罚款记录列表
     */
    @Operation(summary = "罚款情况")
    @GetMapping("/fines")
    public Result<List<FineVO>> fines(@RequestParam String sno) {
        return Result.success(fineService.myFines(sno));
    }

    /**
     * 查询罚款缴纳记录。
     * <p>
     * 返回指定学生的历史缴费凭证，包含缴费时间、金额、流水号等。
     * </p>
     *
     * @param sno 学生学号
     * @return 缴费凭证记录列表
     */
    @Operation(summary = "罚款缴纳记录")
    @GetMapping("/payments")
    public Result<List<FinePayment>> payments(@RequestParam String sno) {
        return Result.success(fineService.myPayments(sno));
    }
}
