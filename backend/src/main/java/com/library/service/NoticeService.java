package com.library.service;

import com.library.common.BusinessException;
import com.library.common.ResultCode;
import com.library.dto.NoticeSaveDTO;
import com.library.entity.Notice;
import com.library.mapper.NoticeMapper;
import com.library.vo.NoticeVO;
import com.library.vo.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务：公开查询 + 管理员发布/编辑/删除。
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public List<NoticeVO> listLatest(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return noticeMapper.findLatest(safeLimit).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    public PageVO<NoticeVO> listPage(String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;

        long total = noticeMapper.count(keyword);
        List<NoticeVO> list = noticeMapper.list(keyword, offset, safeSize).stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        PageVO<NoticeVO> pageVO = new PageVO<>();
        pageVO.setList(list);
        pageVO.setTotal(total);
        pageVO.setPage(safePage);
        pageVO.setSize(safeSize);
        return pageVO;
    }

    public NoticeVO getById(int id) {
        Notice notice = noticeMapper.findById(id);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toVO(notice);
    }

    public NoticeVO create(NoticeSaveDTO dto, String role, String author) {
        requireAdmin(role);
        if (!StringUtils.hasText(author)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Notice notice = new Notice();
        notice.setTopic(dto.getTopic().trim());
        notice.setContent(dto.getContent().trim());
        notice.setAuthor(author.trim());
        notice.setCreateDate(LocalDateTime.now());
        noticeMapper.insert(notice);
        return getById(notice.getId());
    }

    public NoticeVO update(int id, NoticeSaveDTO dto, String role) {
        requireAdmin(role);
        Notice existing = noticeMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        Notice notice = new Notice();
        notice.setId(id);
        notice.setTopic(dto.getTopic().trim());
        notice.setContent(dto.getContent().trim());
        noticeMapper.update(notice);
        return getById(id);
    }

    public void delete(int id, String role) {
        requireAdmin(role);
        if (noticeMapper.findById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        noticeMapper.deleteById(id);
    }

    private void requireAdmin(String role) {
        if (!"admin".equalsIgnoreCase(role)) {
            throw new BusinessException(ResultCode.INVALID_OP);
        }
    }

    private NoticeVO toVO(Notice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTopic(notice.getTopic());
        vo.setContent(notice.getContent());
        vo.setAuthor(notice.getAuthor());
        vo.setCreateDate(notice.getCreateDate());
        return vo;
    }
}
