package com.community.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.Notice;

public interface NoticeService extends IService<Notice> {
    
    Page<Notice> pageByCondition(Integer current, Integer size, String title, String type, Integer status);
    
    Page<Notice> pagePublished(Integer current, Integer size, String type);
    
    boolean publishNotice(Notice notice, Long publisherId, String publisherName);
    
    boolean markAsRead(Long noticeId, Long userId);
    
    boolean isRead(Long noticeId, Long userId);
    
    Notice getDetail(Long id, Long userId);
}