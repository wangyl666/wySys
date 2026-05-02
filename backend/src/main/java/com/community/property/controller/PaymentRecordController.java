package com.community.property.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.property.common.Result;
import com.community.property.entity.PaymentRecord;
import com.community.property.service.PaymentRecordService;
import com.community.property.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "缴费记录接口")
@RestController
@RequestMapping("/api/payment-record")
@RequiredArgsConstructor
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;
    private final UserService userService;

    @ApiOperation("分页查询我的缴费记录（居民端）")
    @GetMapping("/page/my")
    public Result<Page<PaymentRecord>> pageMy(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = userService.getCurrentUserId();
        Page<PaymentRecord> page = paymentRecordService.pageMyRecords(current, size, userId);
        return Result.success(page);
    }

    @ApiOperation("分页查询缴费记录（物业端）")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Page<PaymentRecord>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long billId,
            @RequestParam(required = false) String status) {
        Page<PaymentRecord> page = paymentRecordService.pageByCondition(current, size, userId, billId, status);
        return Result.success(page);
    }

    @ApiOperation("根据ID获取缴费记录详情")
    @GetMapping("/{id}")
    public Result<PaymentRecord> getById(@PathVariable Long id) {
        PaymentRecord record = paymentRecordService.getById(id);
        return Result.success(record);
    }
}