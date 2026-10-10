package com.library.controller;

import com.library.common.Result;
import com.library.dto.CardCreateDTO;
import com.library.dto.CardOpDTO;
import com.library.service.CardService;
import com.library.vo.BorrowCheckVO;
import com.library.vo.CardDetailVO;
import com.library.vo.CardRecordVO;
import com.library.vo.StudentCardQueryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 借书证管理控制器。
 * <p>
 * 覆盖借书证的全生命周期：新办、挂失、补办、注销，以及借阅前的资格校验。
 * 同时提供证件操作记录查询，方便管理员追溯办证历史。
 * </p>
 */
@Tag(name = "借书证管理")
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    /** 借书证服务，处理办证、挂失、补办、注销等业务逻辑 */
    private final CardService cardService;

    /**
     * 新办借书证。
     * <p>
     * 根据学生信息创建新的借书证，返回新证号。
     * 证号规则为学号+三位序号，由系统自动生成。
     * </p>
     *
     * @param dto 包含学生学号、姓名等办证信息的请求体
     * @return 新办的借书证号
     */
    @Operation(summary = "新办借书证")
    @PostMapping
    public Result<Map<String, String>> create(@Valid @RequestBody CardCreateDTO dto) {
        return Result.success(cardService.createCard(dto));
    }

    /**
     * 预览下一个可用证号。
     * <p>
     * 根据学号查询该学生当前最大证号序号，返回下一个可用证号。
     * 前端在办证表单中展示，供管理员确认。
     * </p>
     *
     * @param sno 学生学号
     * @return 预览的下一个证号
     */
    @Operation(summary = "预览新办下一证号（学号+三位序号）")
    @GetMapping("/next-cardno")
    public Result<Map<String, String>> nextCardNo(@RequestParam String sno) {
        // 将预览证号包装为 Map 返回，保持接口格式统一
        return Result.success(Map.of("cardNo", cardService.previewNextCardNo(sno)));
    }

    /**
     * 预览补办新证号。
     * <p>
     * 在原证号末三位序号基础上加 1，生成补办后的新证号。
     * 用于补办前让管理员确认新证号是否正确。
     * </p>
     *
     * @param cardNo 原借书证号
     * @return 补办后的新证号预览
     */
    @Operation(summary = "预览补办新证号（原证号末三位+1）")
    @GetMapping("/{cardNo}/replace-preview")
    public Result<Map<String, String>> replacePreview(@PathVariable String cardNo) {
        return Result.success(Map.of("cardNo", cardService.previewReplaceCardNo(cardNo)));
    }

    /**
     * 按学号查询学生及其借书证信息。
     * <p>
     * 返回学生基本信息和名下所有借书证的状态，用于办证前的身份核实。
     * </p>
     *
     * @param sno 学生学号
     * @return 学生信息及关联的借书证列表
     */
    @Operation(summary = "按学号查询学生及借书证信息")
    @GetMapping("/student-query")
    public Result<StudentCardQueryVO> studentQuery(@RequestParam String sno) {
        return Result.success(cardService.queryStudentCard(sno));
    }

    /**
     * 按学号或证号模糊查询借书证。
     * <p>
     * 支持同时输入学号或证号进行搜索，返回匹配的借书证详情。
     * </p>
     *
     * @param keyword 学号或证号关键字
     * @return 匹配的借书证详情
     */
    @Operation(summary = "按学号或证号查询借书证")
    @GetMapping("/query")
    public Result<CardDetailVO> query(@RequestParam String keyword) {
        return Result.success(cardService.queryCard(keyword));
    }

    /**
     * 查询借书证详情。
     * <p>
     * 根据证号精确查询，返回该证的完整信息，包括持证人、当前状态、在借数量等。
     * </p>
     *
     * @param cardNo 借书证号
     * @return 借书证详情
     */
    @Operation(summary = "查询借书证详情")
    @GetMapping("/detail/{cardNo}")
    public Result<CardDetailVO> get(@PathVariable String cardNo) {
        return Result.success(cardService.getCardDetail(cardNo));
    }

    /**
     * 借书前资格校验。
     * <p>
     * 扫描借书证后调用此接口，返回该证是否可借、剩余额度、是否有未缴罚款等信息。
     * 前端据此决定是否允许继续借书操作。
     * </p>
     *
     * @param cardNo 借书证号
     * @return 借阅资格校验结果，包含可借状态、剩余额度、未缴罚款等
     */
    @Operation(summary = "借书前校验（是否可借、剩余额度）")
    @GetMapping("/{cardNo}/borrow-check")
    public Result<BorrowCheckVO> borrowCheck(@PathVariable String cardNo) {
        return Result.success(cardService.checkBorrowEligibility(cardNo));
    }

    /**
     * 挂失借书证。
     * <p>
     * 将指定证号标记为挂失状态，挂失后该证无法再借书。
     * 已借出的图书仍需正常归还。
     * </p>
     *
     * @param cardNo 要挂失的借书证号
     * @return 操作成功返回空数据
     */
    @Operation(summary = "挂失借书证")
    @PutMapping("/{cardNo}/loss")
    public Result<Void> loss(@PathVariable String cardNo) {
        cardService.reportLoss(cardNo);
        return Result.success();
    }

    /**
     * 补办借书证。
     * <p>
     * 对已挂失的借书证进行补办，生成新证号并继承原证的借阅记录。
     * 原证自动失效。
     * </p>
     *
     * @param cardNo 原借书证号（已挂失状态）
     * @param dto    补办操作的附加信息
     * @return 补办后的新证号
     */
    @Operation(summary = "补办借书证")
    @PutMapping("/{cardNo}/replace")
    public Result<Map<String, String>> replace(@PathVariable String cardNo, @RequestBody CardOpDTO dto) {
        return Result.success(cardService.replace(cardNo, dto));
    }

    /**
     * 注销借书证。
     * <p>
     * 永久注销指定借书证，注销前应确保无在借图书和未缴罚款。
     * 注销后该证号不可恢复。
     * </p>
     *
     * @param cardNo 要注销的借书证号
     * @return 操作成功返回空数据
     */
    @Operation(summary = "注销借书证")
    @DeleteMapping("/{cardNo}")
    public Result<Void> cancel(@PathVariable String cardNo) {
        cardService.cancel(cardNo);
        return Result.success();
    }

    /**
     * 按学号查询该学生的所有证件操作记录。
     * <p>
     * 返回办证、挂失、补办、注销等操作的历史流水。
     * </p>
     *
     * @param sno 学生学号
     * @return 证件操作记录列表
     */
    @Operation(summary = "证件操作记录（按学号）")
    @GetMapping("/records")
    public Result<List<CardRecordVO>> records(@RequestParam String sno) {
        return Result.success(cardService.listRecords(sno));
    }

    /**
     * 查询全部办证记录（管理员专用）。
     * <p>
     * 支持按操作类型和学号筛选，方便管理员查看所有学生的办证流水。
     * </p>
     *
     * @param opType 可选的操作类型筛选（1-办证 2-挂失 3-补办 4-注销）
     * @param sno    可选的学号筛选条件
     * @return 符合条件的全部办证记录
     */
    @Operation(summary = "全部办证记录（管理员）")
    @GetMapping("/records/all")
    public Result<List<CardRecordVO>> allRecords(
            @RequestParam(required = false) Integer opType,
            @RequestParam(required = false) String sno) {
        // 两个筛选条件均为可选，不传时返回全部记录
        return Result.success(cardService.listAllRecords(opType, sno));
    }
}
