package com.community.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.entity.Notice;
import com.community.property.mapper.NoticeMapper;
import com.community.property.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<Notice> pageByCondition(Integer current, Integer size, String title, String type, Integer status) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(Notice::getTitle, title);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Notice::getType, type);
        }
        if (status != null) {
            wrapper.eq(Notice::getStatus, status);
        }
        wrapper.orderByDesc(Notice::getIsTop);
        wrapper.orderByDesc(Notice::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public Page<Notice> pagePublished(Integer current, Integer size, String type) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1);
        if (StringUtils.hasText(type)) {
            wrapper.eq(Notice::getType, type);
        }
        wrapper.and(w -> w.isNull(Notice::getEndTime).or().ge(Notice::getEndTime, LocalDateTime.now()));
        wrapper.orderByDesc(Notice::getIsTop);
        wrapper.orderByDesc(Notice::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishNotice(Notice notice, Long publisherId, String publisherName) {
        notice.setPublisherId(publisherId);
        notice.setPublisherName(publisherName);
        notice.setStatus(1);
        if (notice.getIsTop() == null) {
            notice.setIsTop(0);
        }
        if (notice.getViewCount() == null) {
            notice.setViewCount(0);
        }
        return save(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long noticeId, Long userId) {
        try {
            String checkSql = "SELECT id FROM notice_read WHERE notice_id = ? AND user_id = ?";
            List<Long> ids = jdbcTemplate.query(checkSql, (rs, rowNum) -> rs.getLong("id"), noticeId, userId);
            if (ids.isEmpty()) {
                String insertSql = "INSERT INTO notice_read (notice_id, user_id, read_time) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertSql, noticeId, userId, LocalDateTime.now());
            }
            return true;
        } catch (Exception e) {
            log.error("标记阅读失败：", e);
            return false;
        }
    }

    @Override
    public boolean isRead(Long noticeId, Long userId) {
        try {
            String sql = "SELECT COUNT(*) FROM notice_read WHERE notice_id = ? AND user_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, noticeId, userId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("查询阅读状态失败：", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notice getDetail(Long id, Long userId) {
        Notice notice = getById(id);
        if (notice != null) {
            Notice update = new Notice();
            update.setId(id);
            update.setViewCount(notice.getViewCount() == null ? 1 : notice.getViewCount() + 1);
            updateById(update);
            
            if (userId != null) {
                markAsRead(id, userId);
                notice.setIsRead(isRead(id, userId));
            }
        }
        return notice;
    }
}