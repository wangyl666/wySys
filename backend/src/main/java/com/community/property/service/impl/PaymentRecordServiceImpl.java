package com.community.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.entity.PaymentRecord;
import com.community.property.mapper.PaymentRecordMapper;
import com.community.property.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentRecordService {

    @Override
    public Page<PaymentRecord> pageByCondition(Integer current, Integer size, Long userId, Long billId, String status) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(PaymentRecord::getUserId, userId);
        }
        if (billId != null) {
            wrapper.eq(PaymentRecord::getBillId, billId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PaymentRecord::getStatus, status);
        }
        wrapper.orderByDesc(PaymentRecord::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public Page<PaymentRecord> pageMyRecords(Integer current, Integer size, Long userId) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getUserId, userId);
        wrapper.orderByDesc(PaymentRecord::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }
}