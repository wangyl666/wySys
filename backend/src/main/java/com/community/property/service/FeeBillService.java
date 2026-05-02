package com.community.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.FeeBill;

import java.math.BigDecimal;
import java.util.List;

public interface FeeBillService extends IService<FeeBill> {
    
    Page<FeeBill> pageByCondition(Integer current, Integer size, Long userId, String billMonth, String status, Long feeTypeId);
    
    Page<FeeBill> pageByConditionWithBuildingAccess(Integer current, Integer size, Long userId, String billMonth, String status, Long feeTypeId, List<String> allowedBuildingNos);
    
    Page<FeeBill> pageMyBills(Integer current, Integer size, Long userId, String status);
    
    List<FeeBill> getPendingBills(Long userId);
    
    BigDecimal calculateTotalAmount(List<Long> billIds);
    
    String generateBillNo();
    
    boolean createBill(FeeBill bill);
    
    boolean payBill(Long billId, String paymentMethod, String transactionNo, BigDecimal amount);
    
    boolean batchPay(List<Long> billIds, String paymentMethod, String transactionNo);
}