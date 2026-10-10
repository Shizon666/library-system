package com.library.controller;

import com.library.common.Result;
import com.library.dto.BookAcquireDTO;
import com.library.dto.BookUpdateDTO;
import com.library.dto.CopyBatchAddDTO;
import com.library.dto.CopyUpdateDTO;
import com.library.service.BookService;
import com.library.vo.BookSearchVO;
import com.library.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图书检索与采编控制器。
 * <p>
 * 面向读者提供公开搜索（仅上架书目），面向管理员提供完整的编目管理能力，
 * 包括书目信息维护、副本增删改、上下架操作等。
 * </p>
 */
@Tag(name = "图书检索与采编")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    /** 图书服务，处理书目和副本的增删改查业务逻辑 */
    private final BookService bookService;

    /**
     * 读者公开搜索接口。
     * <p>
     * 支持按书名、作者、ISBN、出版社等关键字模糊搜索，仅返回处于上架状态的书目。
     * </p>
     *
     * @param keyword 搜索关键字，匹配书名/作者/ISBN/出版社
     * @return 匹配的上架书目列表
     */
    @Operation(summary = "图书检索（书名/作者/ISBN/出版社，仅上架书目）")
    @GetMapping("/search")
    public Result<List<BookSearchVO>> search(@RequestParam String keyword) {
        // 委托服务层执行模糊搜索，自动过滤下架书目
        return Result.success(bookService.search(keyword));
    }

    /**
     * 管理员编目列表。
     * <p>
     * 包含下架书目，支持分页和关键字筛选，默认按入库时间倒序排列。
     * 管理员可借此查看全部馆藏书目及上下架状态。
     * </p>
     *
     * @param keyword 可选的关键字筛选条件
     * @param page    页码，默认第 1 页
     * @param size    每页条数，默认 10 条
     * @return 分页后的书目列表
     */
    @Operation(summary = "编目列表（管理员，含下架书目，默认按入库时间倒序）")
    @GetMapping("/catalog")
    public Result<PageVO<BookSearchVO>> catalog(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // keyword 为 null 时返回全部书目，否则按关键字过滤
        return Result.success(bookService.listCatalog(keyword, page, size));
    }

    /**
     * 图书详情接口。
     * <p>
     * 根据 ISBN 查询书目详细信息，包含该书目下所有副本的馆藏状态。
     * </p>
     *
     * @param isbn 国际标准书号，作为书目的唯一标识
     * @return 书目详情及全部副本列表
     */
    @Operation(summary = "图书详情（含全部副本）")
    @GetMapping("/{isbn}")
    public Result<BookSearchVO> detail(@PathVariable String isbn) {
        return Result.success(bookService.getDetail(isbn));
    }

    /**
     * 更新书目信息。
     * <p>
     * 修改已有书目的元数据（书名、作者、出版社、定价等），不影响副本数据。
     * </p>
     *
     * @param isbn 要更新的书目 ISBN
     * @param dto  包含待更新字段的请求体
     * @return 操作成功返回空数据
     */
    @Operation(summary = "更新书目信息")
    @PutMapping("/{isbn}")
    public Result<Void> update(@PathVariable String isbn, @Valid @RequestBody BookUpdateDTO dto) {
        bookService.updateBook(isbn, dto);
        return Result.success();
    }

    /**
     * 图书采编入库。
     * <p>
     * 兼容两种场景：ISBN 不存在时新建书目并创建副本；ISBN 已存在时仅追加新副本。
     * 前端扫描新书时统一调用此接口，无需区分是新书还是补充副本。
     * </p>
     *
     * @param dto 包含 ISBN、书目信息及待创建的副本条码列表
     * @return 操作成功返回空数据
     */
    @Operation(summary = "图书采编入库（新书或新增副本）")
    @PostMapping("/acquire")
    public Result<Void> acquire(@Valid @RequestBody BookAcquireDTO dto) {
        // Service 层内部判断 ISBN 是否已存在，决定走新建还是追加逻辑
        bookService.acquire(dto);
        return Result.success();
    }

    /**
     * 为已有书目批量新增副本。
     * <p>
     * 指定 ISBN 后，一次性创建多个副本记录，每个副本拥有唯一条码。
     * </p>
     *
     * @param isbn  目标书目的 ISBN
     * @param dto   包含待创建副本信息（条码、馆藏位置等）的列表
     * @return 操作成功返回空数据
     */
    @Operation(summary = "为已有书目新增副本")
    @PostMapping("/{isbn}/copies")
    public Result<Void> addCopies(@PathVariable String isbn, @Valid @RequestBody CopyBatchAddDTO dto) {
        bookService.addCopies(isbn, dto.getCopies());
        return Result.success();
    }

    /**
     * 更新单个副本的馆藏信息。
     * <p>
     * 根据条码定位副本，修改其馆藏位置等属性。
     * </p>
     *
     * @param barCode 副本的唯一条码
     * @param dto     包含待更新字段（如馆藏位置）的请求体
     * @return 操作成功返回空数据
     */
    @Operation(summary = "更新副本馆藏位置")
    @PutMapping("/copies/{barCode}")
    public Result<Void> updateCopy(@PathVariable String barCode, @Valid @RequestBody CopyUpdateDTO dto) {
        bookService.updateCopy(barCode, dto);
        return Result.success();
    }

    /**
     * 副本注销。
     * <p>
     * 将指定条码的副本标记为已注销，该副本不再参与借还流程。
     * 注销前应确认该副本当前无在借记录。
     * </p>
     *
     * @param barCode 要注销的副本条码
     * @return 操作成功返回空数据
     */
    @Operation(summary = "副本注销")
    @DeleteMapping("/copies/{barCode}")
    public Result<Void> discardCopy(@PathVariable String barCode) {
        bookService.discardCopy(barCode);
        return Result.success();
    }

    /**
     * 整书下架。
     * <p>
     * 将指定 ISBN 下的所有副本统一标记为下架状态，读者端搜索将不再展示该书目。
     * 下架不影响已借出副本的归还流程。
     * </p>
     *
     * @param isbn 要下架的书目 ISBN
     * @return 操作成功返回空数据
     */
    @Operation(summary = "整书下架（全部副本）")
    @PutMapping("/{isbn}/off-shelf")
    public Result<Void> offShelf(@PathVariable String isbn) {
        // 遍历该 ISBN 下所有副本，统一设置下架标志
        bookService.offShelf(isbn);
        return Result.success();
    }
}
