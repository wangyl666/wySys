package com.community.property.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.property.common.Result;
import com.community.property.entity.Repair;
import com.community.property.entity.User;
import com.community.property.service.BuildingService;
import com.community.property.service.RepairService;
import com.community.property.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "物业报修接口")
@RestController
@RequestMapping("/api/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;
    private final UserService userService;
    private final BuildingService buildingService;

    @ApiOperation("分页查询报修列表（居民端）")
    @GetMapping("/page/my")
    public Result<Page<Repair>> pageMy(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        Long userId = userService.getCurrentUserId();
        Page<Repair> page = repairService.pageByCondition(current, size, userId, status, type);
        return Result.success(page);
    }

    @ApiOperation("分页查询报修列表（物业端）")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Page<Repair>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        User currentUser = userService.getCurrentUser();
        
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        
        if ("ADMIN".equals(currentUser.getRole())) {
            Page<Repair> page = repairService.pageByCondition(current, size, userId, status, type);
            return Result.success(page);
        } else {
            List<String> allowedBuildingNos = buildingService.getBuildingNosByStaffId(currentUser.getId());
            Page<Repair> page = repairService.pageByConditionWithBuildingAccess(current, size, userId, status, type, allowedBuildingNos);
            return Result.success(page);
        }
    }

    @ApiOperation("根据ID获取报修详情")
    @GetMapping("/{id}")
    public Result<Repair> getById(@PathVariable Long id) {
        Repair repair = repairService.getById(id);
        return Result.success(repair);
    }

    @ApiOperation("提交报修申请")
    @PostMapping
    public Result<Boolean> submit(@RequestBody Repair repair) {
        Long userId = userService.getCurrentUserId();
        repair.setUserId(userId);
        return Result.success(repairService.submitRepair(repair));
    }

    @ApiOperation("派单给维修人员")
    @PutMapping("/assign/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> assign(
            @PathVariable Long id,
            @RequestParam Long staffId,
            @RequestParam String staffName,
            @RequestParam(required = false) String staffPhone) {
        return Result.success(repairService.assignRepair(id, staffId, staffName, staffPhone));
    }

    @ApiOperation("处理报修（添加处理内容）")
    @PutMapping("/process/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> process(
            @PathVariable Long id,
            @RequestParam String processContent) {
        return Result.success(repairService.processRepair(id, processContent));
    }

    @ApiOperation("完成报修")
    @PutMapping("/complete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> complete(@PathVariable Long id) {
        return Result.success(repairService.completeRepair(id));
    }

    @ApiOperation("评价报修")
    @PutMapping("/rate/{id}")
    public Result<Boolean> rate(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment) {
        return Result.success(repairService.rateRepair(id, rating, comment));
    }

    @ApiOperation("取消报修")
    @PutMapping("/cancel/{id}")
    public Result<Boolean> cancel(@PathVariable Long id) {
        return Result.success(repairService.cancelRepair(id));
    }
}