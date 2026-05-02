package com.community.property.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.entity.FeeBill;
import com.community.property.entity.House;
import com.community.property.entity.PaymentRecord;
import com.community.property.entity.User;
import com.community.property.mapper.FeeBillMapper;
import com.community.property.mapper.HouseMapper;
import com.community.property.mapper.PaymentRecordMapper;
import com.community.property.mapper.UserMapper;
import com.community.property.service.FeeBillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeBillServiceImpl extends ServiceImpl<FeeBillMapper, FeeBill> implements FeeBillService {

    private final UserMapper userMapper;
    private final HouseMapper houseMapper;
    private final PaymentRecordMapper paymentRecordMapper;

    @Override
    public Page<FeeBill> pageByCondition(Integer current, Integer size, Long userId, String billMonth, String status, Long feeTypeId) {
        LambdaQueryWrapper<FeeBill> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(FeeBill::getUserId, userId);
        }
        if (StringUtils.hasText(billMonth)) {
            wrapper.eq(FeeBill::getBillMonth, billMonth);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(FeeBill::getStatus, status);
        }
        if (feeTypeId != null) {
            wrapper.eq(FeeBill::getFeeTypeId, feeTypeId);
        }
        wrapper.orderByDesc(FeeBill::getCreateTime);
        
        Page<FeeBill> page = page(new Page<>(current, size), wrapper);
        fillExtraInfo(page.getRecords());
        return page;
    }

    @Override
    public Page<FeeBill> pageMyBills(Integer current, Integer size, Long userId, String status) {
        LambdaQueryWrapper<FeeBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeBill::getUserId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(FeeBill::getStatus, status);
        }
        wrapper.orderByDesc(FeeBill::getCreateTime);
        
        Page<FeeBill> page = page(new Page<>(current, size), wrapper);
        fillExtraInfo(page.getRecords());
        return page;
    }

    @Override
    public List<FeeBill> getPendingBills(Long userId) {
        LambdaQueryWrapper<FeeBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeBill::getUserId, userId);
        wrapper.in(FeeBill::getStatus, "UNPAID", "PARTIAL", "OVERDUE");
        wrapper.orderByAsc(FeeBill::getPayableDate);
        return list(wrapper);
    }

    @Override
    public BigDecimal calculateTotalAmount(List<Long> billIds) {
        BigDecimal total = BigDecimal.ZERO;
        for (Long billId : billIds) {
            FeeBill bill = getById(billId);
            if (bill != null && ("UNPAID".equals(bill.getStatus()) || "PARTIAL".equals(bill.getStatus()) || "OVERDUE".equals(bill.getStatus()))) {
                total = total.add(bill.getTotalAmount().subtract(bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount()));
            }
        }
        return total;
    }

    private void fillExtraInfo(List<FeeBill> bills) {
        for (FeeBill bill : bills) {
            if (bill.getUserId() != null) {
                User user = userMapper.selectById(bill.getUserId());
                if (user != null) {
                    bill.setRealName(user.getRealName());
                    bill.setPhone(user.getPhone());
                }
            }
            if (bill.getHouseId() != null) {
                House house = houseMapper.selectById(bill.getHouseId());
                if (house != null) {
                    bill.setBuildingNo(house.getBuildingNo());
                    bill.setUnitNo(house.getUnitNo());
                    bill.setRoomNo(house.getRoomNo());
                }
            }
        }
    }

    @Override
    public String generateBillNo() {
        String dateStr = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        int random = (int) (Math.random() * 9000 + 1000);
        return "FB" + dateStr + random;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createBill(FeeBill bill) {
        bill.setBillNo(generateBillNo());
        bill.setStatus("UNPAID");
        bill.setPaidAmount(BigDecimal.ZERO);
        if (bill.getLateFee() == null) {
            bill.setLateFee(BigDecimal.ZERO);
        }
        if (bill.getTotalAmount() == null) {
            bill.setTotalAmount(bill.getAmount().add(bill.getLateFee()));
        }
        if (bill.getPayableDate() == null) {
            bill.setPayableDate(LocalDate.now().plusMonths(1));
        }
        return save(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payBill(Long billId, String paymentMethod, String transactionNo, BigDecimal amount) {
        FeeBill bill = getById(billId);
        if (bill == null) {
            throw new RuntimeException("账单不存在");
        }
        if ("PAID".equals(bill.getStatus())) {
            throw new RuntimeException("账单已支付");
        }
        
        BigDecimal paidAmount = bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount();
        BigDecimal totalAmount = bill.getTotalAmount();
        BigDecimal remaining = totalAmount.subtract(paidAmount);
        
        if (amount.compareTo(remaining) > 0) {
            throw new RuntimeException("支付金额不能大于待付金额");
        }
        
        PaymentRecord record = new PaymentRecord();
        record.setBillId(billId);
        record.setUserId(bill.getUserId());
        record.setAmount(amount);
        record.setPaymentMethod(paymentMethod);
        record.setTransactionNo(transactionNo);
        record.setStatus("SUCCESS");
        record.setPaymentTime(LocalDateTime.now());
        paymentRecordMapper.insert(record);
        
        BigDecimal newPaidAmount = paidAmount.add(amount);
        FeeBill update = new FeeBill();
        update.setId(billId);
        update.setPaidAmount(newPaidAmount);
        
        if (newPaidAmount.compareTo(totalAmount) >= 0) {
            update.setStatus("PAID");
            update.setPaidTime(LocalDateTime.now());
            update.setPaymentMethod(paymentMethod);
            update.setTransactionNo(transactionNo);
        } else {
            update.setStatus("PARTIAL");
        }
        
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchPay(List<Long> billIds, String paymentMethod, String transactionNo) {
        for (Long billId : billIds) {
            FeeBill bill = getById(billId);
            if (bill != null && !"PAID".equals(bill.getStatus())) {
                BigDecimal remaining = bill.getTotalAmount().subtract(bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount());
                payBill(billId, paymentMethod, transactionNo + "_" + billId, remaining);
            }
        }
        return true;
    }
}