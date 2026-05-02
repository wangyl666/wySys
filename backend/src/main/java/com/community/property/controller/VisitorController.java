package com.community.property.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.property.common.Result;
import com.community.property.entity.User;
import com.community.property.entity.Visitor;
import com.community.property.service.UserService;
import com.community.property.service.VisitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "访客预约接口")
@RestController
@RequestMapping("/api/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;
    private final UserService userService;

    @ApiOperation("分页查询访客预约列表（居民端）")
    @GetMapping("/page/my")
    public Result<Page<Visitor>> pageMy(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String visitorName) {
        Long userId = userService.getCurrentUserId();
        Page<Visitor> page = visitorService.pageByCondition(current, size, userId, status, visitorName);
        return Result.success(page);
    }

    @ApiOperation("分页查询访客预约列表（物业端）")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Page<Visitor>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String visitorName) {
        Page<Visitor> page = visitorService.pageByCondition(current, size, userId, status, visitorName);
        return Result.success(page);
    }

    @ApiOperation("根据ID获取访客预约详情")
    @GetMapping("/{id}")
    public Result<Visitor> getById(@PathVariable Long id) {
        Visitor visitor = visitorService.getById(id);
        return Result.success(visitor);
    }

    @ApiOperation("提交访客预约申请")
    @PostMapping
    public Result<Boolean> submit(@RequestBody Visitor visitor) {
        Long userId = userService.getCurrentUserId();
        visitor.setUserId(userId);
        return Result.success(visitorService.submitVisitor(visitor));
    }

    @ApiOperation("审核通过访客预约")
    @PutMapping("/approve/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> approve(
            @PathVariable Long id,
            @RequestParam(required = false) String auditComment) {
        User currentUser = userService.getCurrentUser();
        return Result.success(visitorService.approveVisitor(id, currentUser.getId(), currentUser.getRealName(), auditComment));
    }

    @ApiOperation("审核拒绝访客预约")
    @PutMapping("/reject/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> reject(
            @PathVariable Long id,
            @RequestParam String auditComment) {
        User currentUser = userService.getCurrentUser();
        return Result.success(visitorService.rejectVisitor(id, currentUser.getId(), currentUser.getRealName(), auditComment));
    }

    @ApiOperation("访客签到")
    @PutMapping("/checkin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> checkIn(@PathVariable Long id) {
        return Result.success(visitorService.checkIn(id));
    }

    @ApiOperation("访客签退")
    @PutMapping("/checkout/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> checkOut(@PathVariable Long id) {
        return Result.success(visitorService.checkOut(id));
    }

    @ApiOperation("取消访客预约")
    @PutMapping("/cancel/{id}")
    public Result<Boolean> cancel(@PathVariable Long id) {
        return Result.success(visitorService.cancelVisitor(id));
    }
}