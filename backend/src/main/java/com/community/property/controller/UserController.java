package com.community.property.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.property.common.Result;
import com.community.property.entity.User;
import com.community.property.service.BuildingService;
import com.community.property.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "用户管理接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BuildingService buildingService;

    @ApiOperation("分页查询用户列表")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Page<User>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        User currentUser = userService.getCurrentUser();
        
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        
        if ("ADMIN".equals(currentUser.getRole())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(w -> w
                        .like(User::getUsername, keyword)
                        .or().like(User::getRealName, keyword)
                        .or().like(User::getPhone, keyword));
            }
            if (role != null && !role.isEmpty()) {
                wrapper.eq(User::getRole, role);
            }
            if (status != null) {
                wrapper.eq(User::getStatus, status);
            }
            wrapper.orderByDesc(User::getCreateTime);
            Page<User> page = userService.page(new Page<>(current, size), wrapper);
            page.getRecords().forEach(u -> u.setPassword(null));
            return Result.success(page);
        } else {
            List<String> allowedBuildingNos = buildingService.getBuildingNosByStaffId(currentUser.getId());
            Page<User> page = userService.pageByBuildingAccess(current, size, keyword, role, status, allowedBuildingNos);
            return Result.success(page);
        }
    }

    @ApiOperation("根据ID获取用户信息")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @ApiOperation("新增用户")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> add(@RequestBody User user) {
        return Result.success(userService.register(user));
    }

    @ApiOperation("更新用户信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody User user) {
        user.setPassword(null);
        user.setUsername(null);
        return Result.success(userService.updateById(user));
    }

    @ApiOperation("更新用户状态")
    @PutMapping("/status/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        return Result.success(userService.updateById(user));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }
}