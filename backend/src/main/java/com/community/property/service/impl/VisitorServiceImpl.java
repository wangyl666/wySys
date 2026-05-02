package com.community.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.entity.User;
import com.community.property.entity.Visitor;
import com.community.property.mapper.UserMapper;
import com.community.property.mapper.VisitorMapper;
import com.community.property.service.VisitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorServiceImpl extends ServiceImpl<VisitorMapper, Visitor> implements VisitorService {

    private final UserMapper userMapper;

    @Override
    public Page<Visitor> pageByCondition(Integer current, Integer size, Long userId, String status, String visitorName) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Visitor::getUserId, userId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Visitor::getStatus, status);
        }
        if (StringUtils.hasText(visitorName)) {
            wrapper.like(Visitor::getVisitorName, visitorName);
        }
        wrapper.orderByDesc(Visitor::getCreateTime);
        
        Page<Visitor> page = page(new Page<>(current, size), wrapper);
        fillUserInfo(page.getRecords());
        return page;
    }

    private void fillUserInfo(List<Visitor> visitors) {
        for (Visitor visitor : visitors) {
            if (visitor.getUserId() != null) {
                User user = userMapper.selectById(visitor.getUserId());
                if (user != null) {
                    visitor.setRealName(user.getRealName());
                    visitor.setPhone(user.getPhone());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitVisitor(Visitor visitor) {
        visitor.setStatus("PENDING");
        if (visitor.getVisitorCount() == null) {
            visitor.setVisitorCount(1);
        }
        return save(visitor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveVisitor(Long id, Long auditId, String auditName, String auditComment) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客预约记录不存在");
        }
        if (!"PENDING".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不支持审核");
        }
        
        Visitor update = new Visitor();
        update.setId(id);
        update.setStatus("APPROVED");
        update.setAuditId(auditId);
        update.setAuditName(auditName);
        update.setAuditComment(auditComment);
        update.setAuditTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectVisitor(Long id, Long auditId, String auditName, String auditComment) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客预约记录不存在");
        }
        if (!"PENDING".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不支持审核");
        }
        
        Visitor update = new Visitor();
        update.setId(id);
        update.setStatus("REJECTED");
        update.setAuditId(auditId);
        update.setAuditName(auditName);
        update.setAuditComment(auditComment);
        update.setAuditTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkIn(Long id) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客预约记录不存在");
        }
        if (!"APPROVED".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不支持签到");
        }
        
        Visitor update = new Visitor();
        update.setId(id);
        update.setStatus("CHECKED_IN");
        update.setCheckInTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOut(Long id) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客预约记录不存在");
        }
        if (!"CHECKED_IN".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不支持签退");
        }
        
        Visitor update = new Visitor();
        update.setId(id);
        update.setStatus("CHECKED_OUT");
        update.setCheckOutTime(LocalDateTime.now());
        update.setLeaveTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelVisitor(Long id) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客预约记录不存在");
        }
        if ("CHECKED_IN".equals(visitor.getStatus()) || 
            "CHECKED_OUT".equals(visitor.getStatus()) || 
            "CANCELLED".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不支持取消");
        }
        
        Visitor update = new Visitor();
        update.setId(id);
        update.setStatus("CANCELLED");
        return updateById(update);
    }
}