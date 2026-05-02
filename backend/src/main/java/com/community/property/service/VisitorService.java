package com.community.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.Visitor;

public interface VisitorService extends IService<Visitor> {
    
    Page<Visitor> pageByCondition(Integer current, Integer size, Long userId, String status, String visitorName);
    
    boolean submitVisitor(Visitor visitor);
    
    boolean approveVisitor(Long id, Long auditId, String auditName, String auditComment);
    
    boolean rejectVisitor(Long id, Long auditId, String auditName, String auditComment);
    
    boolean checkIn(Long id);
    
    boolean checkOut(Long id);
    
    boolean cancelVisitor(Long id);
}