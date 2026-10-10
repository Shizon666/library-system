package com.library.controller;

import com.library.common.Result;
import com.library.dto.BorrowDTO;
import com.library.dto.ReportLossDTO;
import com.library.dto.ReturnDTO;
import com.library.service.BorrowService;
import com.library.vo.CopySearchResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 借还书控制器。
 * <p>
 * 覆盖图书借阅、归还、副本检索和挂失赔偿等核心流通业务。
 * 借书支持批量扫描条码一次性借阅，还书时自动计算超期罚款金额。
 * </p>
 */
@Tag(name = "借还书")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BorrowController {

    /** 借还服务，处理借书、还书、挂失等流通业务逻辑 */
    private final BorrowService borrowService;

    /**
     * 批量借书接口。
     * <p>
     * 前端扫描借书证号和多本图书条码后统一提交，后端逐本校验并创建借阅记录。
     * 请求头 X-CardNo 用于标识当前登录用户的借书证号（可选，管理端代借时使用）。
     * </p>
     *
     * @param dto         包含借书证号和条码列表的请求体
     * @param loginCardNo 请求头中的登录用户证号，用于权限校验
     * @return 借阅结果，包含成功借出的册数和详情
     */
    @Operation(summary = "借书（扫描证号+多本条码）")
    @PostMapping("/borrow")
    public Result<Map<String, Object>> borrow(
            @Valid @RequestBody BorrowDTO dto,
            @RequestHeader(value = "X-CardNo", required = false) String loginCardNo) {
        // 委托服务层逐本校验可借状态并创建借阅记录
        return Result.success(borrowService.borrow(dto, loginCardNo));
    }

    /**
     * 还书接口。
     * <p>
     * 扫描副本条码提交归还，后端自动判断是否超期。
     * 若超期则生成罚款记录并在返回值中包含应缴金额，方便前端提示读者。
     * </p>
     *
     * @param dto 包含副本条码的还书请求体
     * @return 还书结果，超期时附带罚款金额信息
     */
    @Operation(summary = "还书（扫描副本条码，自动计算超期罚款）")
    @PostMapping("/return")
    public Result<Map<String, Object>> returnBook(@Valid @RequestBody ReturnDTO dto) {
        // Service 层处理归还逻辑，超期自动生成罚款记录
        return Result.success(borrowService.returnBook(dto));
    }

    /**
     * 检索可借副本。
     * <p>
     * 支持按书名、ISBN、条码、作者、出版社等多维度搜索，返回当前可借的副本列表。
     * 借书前台用于快速定位要借的图书。
     * </p>
     *
     * @param keyword 搜索关键字
     * @return 匹配的可借副本列表及书目信息
     */
    @Operation(summary = "检索可借副本（书名/ISBN/条码/作者/出版社）")
    @GetMapping("/copies/search")
    public Result<CopySearchResultVO> searchCopy(@RequestParam String keyword) {
        return Result.success(borrowService.searchCopyForBorrow(keyword));
    }

    /**
     * 挂失赔偿接口。
     * <p>
     * 读者遗失图书后提交挂失申请，后端将该副本标记为遗失状态，
     * 并按书目定价自动生成赔偿罚款记录。
     * </p>
     *
     * @param dto 包含副本条码和挂失原因的请求体
     * @return 挂失结果及赔偿金额信息
     */
    @Operation(summary = "挂失书籍（按定价赔偿）")
    @PostMapping("/borrow/report-loss")
    public Result<Map<String, Object>> reportLoss(@Valid @RequestBody ReportLossDTO dto) {
        // Service 层标记副本遗失状态并生成对应罚款记录
        return Result.success(borrowService.reportLostBook(dto));
    }

    /**
     * 查询单个副本的详细信息。
     * <p>
     * 借书前扫描条码调用此接口，预览副本的书目信息、在借状态等，
     * 帮助前台工作人员确认副本是否可借。
     * </p>
     *
     * @param barCode 副本条码
     * @return 副本详情，包含书目信息和当前借阅状态
     */
    @Operation(summary = "查询副本信息（借书前校验）")
    @GetMapping("/copies/{barCode}")
    public Result<Map<String, Object>> copyInfo(@PathVariable String barCode) {
        return Result.success(borrowService.getCopyInfo(barCode));
    }
}
