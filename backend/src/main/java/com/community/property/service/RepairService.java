package com.community.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.Repair;

public interface RepairService extends IService<Repair> {
    
    Page<Repair> pageByCondition(Integer current, Integer size, Long userId, String status, String type);
    
    Page<Repair> pageByStaff(Integer current, Integer size, Long staffId, String status);
    
    boolean submitRepair(Repair repair);
    
    boolean assignRepair(Long id, Long staffId, String staffName, String staffPhone);
    
    boolean processRepair(Long id, String processContent);
    
    boolean completeRepair(Long id);
    
    boolean rateRepair(Long id, Integer rating, String comment);
    
    boolean cancelRepair(Long id);
}