package com.community.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.PaymentRecord;

public interface PaymentRecordService extends IService<PaymentRecord> {
    
    Page<PaymentRecord> pageByCondition(Integer current, Integer size, Long userId, Long billId, String status);
    
    Page<PaymentRecord> pageMyRecords(Integer current, Integer size, Long userId);
}