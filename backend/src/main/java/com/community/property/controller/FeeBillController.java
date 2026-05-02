package com.community.property.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.property.common.Result;
import com.community.property.entity.FeeBill;
import com.community.property.service.FeeBillService;
import com.community.property.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Api(tags = "费用账单接口")
@RestController
@RequestMapping("/api/fee-bill")
@RequiredArgsConstructor
public class FeeBillController {

    private final FeeBillService feeBillService;
    private final UserService userService;

    @ApiOperation("分页查询我的账单（居民端）")
    @GetMapping("/page/my")
    public Result<Page<FeeBill>> pageMy(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        Long userId = userService.getCurrentUserId();
        Page<FeeBill> page = feeBillService.pageMyBills(current, size, userId, status);
        return Result.success(page);
    }

    @ApiOperation("获取我的待缴费账单列表")
    @GetMapping("/pending")
    public Result<List<FeeBill>> getPendingBills() {
        Long userId = userService.getCurrentUserId();
        List<FeeBill> bills = feeBillService.getPendingBills(userId);
        return Result.success(bills);
    }

    @ApiOperation("计算多个账单的总金额")
    @PostMapping("/calculate")
    public Result<BigDecimal> calculateTotal(@RequestBody List<Long> billIds) {
        BigDecimal total = feeBillService.calculateTotalAmount(billIds);
        return Result.success(total);
    }

    @ApiOperation("分页查询账单列表（物业端）")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Page<FeeBill>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String billMonth,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long feeTypeId) {
        Page<FeeBill> page = feeBillService.pageByCondition(current, size, userId, billMonth, status, feeTypeId);
        return Result.success(page);
    }

    @ApiOperation("根据ID获取账单详情")
    @GetMapping("/{id}")
    public Result<FeeBill> getById(@PathVariable Long id) {
        FeeBill feeBill = feeBillService.getById(id);
        return Result.success(feeBill);
    }

    @ApiOperation("创建账单（物业端）")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> create(@RequestBody FeeBill feeBill) {
        return Result.success(feeBillService.createBill(feeBill));
    }

    @ApiOperation("缴费（模拟）")
    @PostMapping("/pay/{id}")
    public Result<Boolean> pay(
            @PathVariable Long id,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) BigDecimal amount) {
        FeeBill bill = feeBillService.getById(id);
        if (bill == null) {
            return Result.error("账单不存在");
        }
        if (amount == null) {
            amount = bill.getTotalAmount().subtract(bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount());
        }
        String transactionNo = "TXN" + System.currentTimeMillis();
        return Result.success(feeBillService.payBill(id, paymentMethod, transactionNo, amount));
    }

    @ApiOperation("批量缴费（模拟）")
    @PostMapping("/batch-pay")
    public Result<Boolean> batchPay(
            @RequestBody List<Long> billIds,
            @RequestParam String paymentMethod) {
        String transactionNo = "TXN" + System.currentTimeMillis();
        return Result.success(feeBillService.batchPay(billIds, paymentMethod, transactionNo));
    }

    @ApiOperation("更新账单信息（物业端）")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> update(@RequestBody FeeBill feeBill) {
        return Result.success(feeBillService.updateById(feeBill));
    }
}