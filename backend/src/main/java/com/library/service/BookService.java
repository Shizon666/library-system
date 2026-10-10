package com.library.service;

import com.library.common.BusinessException;
import com.library.common.ResultCode;
import com.library.dto.BookAcquireDTO;
import com.library.dto.BookUpdateDTO;
import com.library.dto.CopyUpdateDTO;
import com.library.entity.Book;
import com.library.entity.Bookcopy;
import com.library.mapper.BookMapper;
import com.library.mapper.BookcopyMapper;
import com.library.vo.BookSearchVO;
import com.library.vo.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 图书服务：管理书目信息和馆藏副本，包括采购入库、信息修改、下架注销等操作。
 * <p>
 * 核心业务规则：
 * - ISBN 统一存纯数字格式（去掉连字符），仅接受10位或13位
 * - 馆藏地点格式为 X区-XX架-XX层（如 A区-01架-01层），入库时严格校验
 * - 下架整书前必须归还所有副本，有在借副本则拒绝下架
 * - 报废副本时保存旧状态，便于审计追溯
 */
@Service
@RequiredArgsConstructor
public class BookService {

    // ISBN 只接受10位或13位纯数字（入库时会去掉连字符）
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(\\d{10}|\\d{13})$");
    // 馆藏地点格式：字母区-两位数架-两位数层，如 A区-01架-01层
    private static final Pattern PLACE_PATTERN = Pattern.compile("^[A-Z]区-\\d{2}架-\\d{2}层$");

    private final BookMapper bookMapper;
    private final BookcopyMapper bookcopyMapper;

    /**
     * 图书搜索：按关键词模糊匹配书名/作者/ISBN，返回书目列表。
     * 读者端使用，会过滤掉已注销副本。
     *
     * @param keyword 搜索关键词
     * @return 匹配的书目列表（含副本统计）
     */
    public List<BookSearchVO> search(String keyword) {
        // 空关键词直接返回空列表，不查库
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        List<Book> books = bookMapper.search(keyword.trim());
        // hideDiscardedCopies=true：读者端不展示已注销副本
        return toSearchVOList(books, true);
    }

    /**
     * 书目目录分页查询：管理员端使用，支持关键词筛选和分页。
     * 保留所有副本（含已注销），方便管理员管理。
     *
     * @param keyword 搜索关键词（可选）
     * @param page    页码（从1开始）
     * @param size    每页条数（1-100）
     * @return 分页结果
     */
    public PageVO<BookSearchVO> listCatalog(String keyword, int page, int size) {
        // 清理参数：关键词去空格，页码最小1，每页限制1-100条
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        // 计算分页偏移量
        int offset = (safePage - 1) * safeSize;
        // 查询总数和当前页数据
        long total = bookMapper.countCatalog(kw);
        List<Book> books = bookMapper.listCatalog(kw, offset, safeSize);
        // 组装分页结果
        PageVO<BookSearchVO> pageVO = new PageVO<>();
        pageVO.setList(toSearchVOList(books, false));
        pageVO.setTotal(total);
        pageVO.setPage(safePage);
        pageVO.setSize(safeSize);
        return pageVO;
    }

    /**
     * 获取书目详情：按ISBN查询完整书目信息。
     *
     * @param isbn ISBN编号
     * @return 书目详情（含副本统计）
     */
    public BookSearchVO getDetail(String isbn) {
        // 查找书目，不存在则报错
        Book book = bookMapper.findByIsbn(isbn);
        if (book == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // hideDiscardedCopies=false：管理员端保留全部副本
        return toSearchVO(book, false);
    }

    /**
     * 图书采购入库。如果 ISBN 在库中不存在则新建书目，已存在则只追加副本。
     * 所以 dto.copies 为空 + 书目已存在 = 报错（没有新书也没有新副本，操作无意义）。
     *
     * @param dto 采购入库请求，包含书目信息和副本列表
     */
    @Transactional
    public void acquire(BookAcquireDTO dto) {
        // 先校验ISBN格式、必填字段等
        validateAcquire(dto);

        // 先查一下库里有没有这本书，决定是新建书目还是追加副本
        Book existing = bookMapper.findByIsbn(dto.getISBN());
        if (existing == null) {
            // ISBN不存在：新建书目记录
            Book book = new Book();
            book.setISBN(dto.getISBN());
            book.setBname(dto.getBookName());
            book.setAuthor(dto.getAuthor());
            book.setPublisher(dto.getPublisher());
            book.setPubDate(dto.getPubDate());
            book.setIntroduction(dto.getIntroduction());
            book.setClcNum(dto.getCategory());
            book.setBookStatus(1);  // 新书默认上架状态
            book.setPrice(dto.getPrice() != null ? dto.getPrice() : new java.math.BigDecimal("50.00"));
            bookMapper.insert(book);
        }
        // 副本列表为空时：新书可直接返回（只有书目），已存在的书报错（操作无意义）
        if (dto.getCopies() == null || dto.getCopies().isEmpty()) {
            if (existing == null) {
                return;  // 新书无副本，只创建书目即可
            }
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请至少添加一个副本");
        }
        // 逐个插入副本：校验条码唯一性、馆藏地点格式
        for (BookAcquireDTO.CopyItem item : dto.getCopies()) {
            // 条码和馆藏地点都不能为空
            if (!StringUtils.hasText(item.getBarCode()) || !StringUtils.hasText(item.getPlace())) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "副本条码与馆藏地点不能为空");
            }
            // 校验馆藏地点格式：X区-XX架-XX层
            if (!PLACE_PATTERN.matcher(item.getPlace()).matches()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                        "馆藏地点格式错误，应为 X区-XX架-XX层（如 A区-01架-01层）");
            }
            // 条码不能重复，防止重复入库
            if (bookcopyMapper.findByBarCode(item.getBarCode()) != null) {
                throw new BusinessException(ResultCode.FAIL.getCode(), "条码已存在: " + item.getBarCode());
            }
            // 构建副本记录并插入
            Bookcopy copy = new Bookcopy();
            copy.setBarCode(item.getBarCode());
            copy.setISBN(dto.getISBN());
            copy.setPlace(item.getPlace());
            copy.setStatus(1);  // 新副本默认可借状态
            bookcopyMapper.insert(copy);
        }
    }

    /**
     * 修改书目信息：更新书名、作者、出版社等基本信息。
     * ISBN作为主键不可修改。
     *
     * @param isbn 书目ISBN（路径参数，用于定位）
     * @param dto  更新内容
     */
    @Transactional
    public void updateBook(String isbn, BookUpdateDTO dto) {
        // 查找书目，不存在则报错
        Book book = bookMapper.findByIsbn(isbn);
        if (book == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 校验必填字段：书名、作者、出版社不能为空
        if (!StringUtils.hasText(dto.getBookName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "书名不能为空");
        }
        if (!StringUtils.hasText(dto.getAuthor())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "作者不能为空");
        }
        if (!StringUtils.hasText(dto.getPublisher())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "出版社不能为空");
        }
        // 更新书目字段
        book.setBname(dto.getBookName());
        book.setAuthor(dto.getAuthor());
        book.setPublisher(dto.getPublisher());
        book.setPubDate(dto.getPubDate());
        book.setIntroduction(dto.getIntroduction());
        book.setClcNum(dto.getCategory());
        // 价格可选更新，未传则保持原值
        if (dto.getPrice() != null) {
            book.setPrice(dto.getPrice());
        }
        bookMapper.update(book);
    }

    /**
     * 修改副本馆藏地点。已注销的副本不允许编辑。
     *
     * @param barCode 副本条码
     * @param dto     更新内容（新馆藏地点）
     */
    @Transactional
    public void updateCopy(String barCode, CopyUpdateDTO dto) {
        // 查找副本，不存在则报错
        Bookcopy copy = bookcopyMapper.findByBarCode(barCode);
        if (copy == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 注销副本不允许修改，借出中的可以改（比如调整位置信息）
        if (copy.getStatus() == 2) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "副本已注销，无法编辑");
        }
        // 校验馆藏地点不能为空且格式正确
        if (!StringUtils.hasText(dto.getPlace())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "馆藏地点不能为空");
        }
        if (!PLACE_PATTERN.matcher(dto.getPlace()).matches()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "馆藏地点格式错误，应为 X区-XX架-XX层（如 A区-01架-01层）");
        }
        // 更新馆藏地点
        bookcopyMapper.updatePlace(barCode, dto.getPlace());
    }

    /**
     * 给已有书目追加副本，逻辑和 acquire 的副本插入部分一样，
     * 但前提条件不同：书目必须存在且未下架。
     *
     * @param isbn   书目ISBN
     * @param copies 要追加的副本列表
     */
    @Transactional
    public void addCopies(String isbn, List<BookAcquireDTO.CopyItem> copies) {
        // 书目必须存在
        Book book = bookMapper.findByIsbn(isbn);
        if (book == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 下架书目不能追加副本（bookStatus=1为上架）
        if (book.getBookStatus() != 1) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "书目已下架，无法新增副本");
        }
        // 副本列表不能为空
        if (copies == null || copies.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请至少添加一个副本");
        }
        // 复用acquire的DTO结构，组装完整信息
        BookAcquireDTO dto = new BookAcquireDTO();
        dto.setISBN(isbn);
        dto.setBookName(book.getBname());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPubDate(book.getPubDate());
        dto.setCopies(copies);
        // 逐个校验并插入副本，逻辑与acquire一致
        for (BookAcquireDTO.CopyItem item : copies) {
            if (!StringUtils.hasText(item.getBarCode()) || !StringUtils.hasText(item.getPlace())) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "副本条码与馆藏地点不能为空");
            }
            if (!PLACE_PATTERN.matcher(item.getPlace()).matches()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                        "馆藏地点格式错误，应为 X区-XX架-XX层（如 A区-01架-01层）");
            }
            if (bookcopyMapper.findByBarCode(item.getBarCode()) != null) {
                throw new BusinessException(ResultCode.FAIL.getCode(), "条码已存在: " + item.getBarCode());
            }
            Bookcopy copy = new Bookcopy();
            copy.setBarCode(item.getBarCode());
            copy.setISBN(isbn);
            copy.setPlace(item.getPlace());
            copy.setStatus(1);  // 新副本默认可借
            bookcopyMapper.insert(copy);
        }
    }

    /**
     * 注销单个副本。借出中的不能注销（得先还），已经注销的也不能重复操作。
     * 注销时保存旧状态到数据库，便于审计追溯。
     *
     * @param barCode 要注销的副本条码
     */
    @Transactional
    public void discardCopy(String barCode) {
        // 查找副本，不存在则报错
        Bookcopy copy = bookcopyMapper.findByBarCode(barCode);
        if (copy == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 借出中（status=0）的副本不能注销，必须先归还
        if (copy.getStatus() == 0) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "副本已借出，无法注销");
        }
        // 已注销（status=2）的不能重复操作
        if (copy.getStatus() == 2) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "副本已注销");
        }
        // 注销副本：状态改为2，保存旧状态供审计
        bookcopyMapper.discardCopy(barCode, copy.getStatus());
    }

    /**
     * 整书下架：书目状态设为下架（0），所有副本状态改为注销（2）。
     * 前置条件：该书所有副本都必须归还，有在借副本则拒绝下架。
     *
     * @param isbn 要下架的书目ISBN
     */
    @Transactional
    public void offShelf(String isbn) {
        // 书目必须存在
        Book book = bookMapper.findByIsbn(isbn);
        if (book == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 只要有一本副本还在借出状态（status=0），就不允许下架
        List<Bookcopy> copies = bookcopyMapper.findByIsbn(isbn);
        boolean hasBorrowed = copies.stream().anyMatch(c -> c.getStatus() == 0);
        if (hasBorrowed) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "存在已借出副本，无法整书下架");
        }
        // 书目状态改为下架（0）
        bookMapper.updateStatus(isbn, 0);
        // 所有副本状态改为注销（2），统一处理
        bookcopyMapper.updateAllByIsbn(isbn, 2);
    }

    /**
     * 采购入库参数校验：ISBN 格式、必填字段等。
     * ISBN 会去掉连字符后校验，统一存纯数字格式。
     *
     * @param dto 采购入库请求
     */
    private void validateAcquire(BookAcquireDTO dto) {
        // ISBN不能为空
        if (!StringUtils.hasText(dto.getISBN())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "ISBN 不能为空");
        }
        // 用户输入可能带连字符（如 978-7-xxx），入库前统一去掉
        String isbn = dto.getISBN().replace("-", "");
        // 校验ISBN格式：必须是10位或13位纯数字
        if (!ISBN_PATTERN.matcher(isbn).matches()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "ISBN 格式错误，应为10位或13位数字");
        }
        // 回写处理后的纯数字ISBN
        dto.setISBN(isbn);
        // 书名、作者、出版社、出版日期都是必填项
        if (!StringUtils.hasText(dto.getBookName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "书名不能为空");
        }
        if (!StringUtils.hasText(dto.getAuthor())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "作者不能为空");
        }
        if (!StringUtils.hasText(dto.getPublisher())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "出版社不能为空");
        }
        if (dto.getPubDate() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "出版日期不能为空");
        }
    }

    /**
     * 批量转换书目列表为VO列表。
     *
     * @param books               书目实体列表
     * @param hideDiscardedCopies 是否隐藏已注销副本（读者端true，管理员端false）
     * @return VO列表
     */
    private List<BookSearchVO> toSearchVOList(List<Book> books, boolean hideDiscardedCopies) {
        List<BookSearchVO> result = new ArrayList<>();
        for (Book book : books) {
            BookSearchVO vo = toSearchVO(book, hideDiscardedCopies);
            result.add(vo);
        }
        return result;
    }

    /**
     * 把 Book 实体转成前端展示用的 VO，同时统计副本数量。
     * hideDiscardedCopies=true 时过滤掉已注销副本（读者搜索场景），
     * false 时保留全部（管理员管理场景）。
     *
     * @param book                书目实体
     * @param hideDiscardedCopies 是否隐藏已注销副本
     * @return 书目VO（含副本统计）
     */
    private BookSearchVO toSearchVO(Book book, boolean hideDiscardedCopies) {
        BookSearchVO vo = new BookSearchVO();
        // 基本字段映射
        vo.setISBN(book.getISBN());
        vo.setBookName(book.getBname());
        vo.setAuthor(book.getAuthor());
        vo.setPublisher(book.getPublisher());
        vo.setPubDate(book.getPubDate());
        vo.setIntroduction(book.getIntroduction());
        vo.setCategory(book.getClcNum());
        vo.setStatus(book.getBookStatus());
        // 查询该ISBN下的所有副本
        List<Bookcopy> copies = bookcopyMapper.findByIsbn(book.getISBN());
        // 读者端搜索时隐藏已注销副本，管理员端保留全部
        if (hideDiscardedCopies) {
            copies = copies.stream().filter(c -> c.getStatus() != 2).toList();
        }
        vo.setCopies(copies);
        // 统计副本数量：总数、可借数、已借出数
        int total = copies.size();
        int available = (int) copies.stream().filter(c -> c.getStatus() == 1).count();
        int borrowed = (int) copies.stream().filter(c -> c.getStatus() == 0).count();
        vo.setTotalCopies(total);
        vo.setAvailableCopies(available);
        vo.setBorrowedCopies(borrowed);
        return vo;
    }

    /**
     * 默认转换：不隐藏已注销副本（管理员视角）。
     *
     * @param book 书目实体
     * @return 书目VO
     */
    private BookSearchVO toSearchVO(Book book) {
        return toSearchVO(book, false);
    }
}
