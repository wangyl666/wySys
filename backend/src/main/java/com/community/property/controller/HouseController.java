package com.community.property.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.property.common.Result;
import com.community.property.entity.House;
import com.community.property.entity.User;
import com.community.property.service.BuildingService;
import com.community.property.service.HouseService;
import com.community.property.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "房屋管理接口")
@RestController
@RequestMapping("/api/house")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;
    private final UserService userService;
    private final BuildingService buildingService;

    @ApiOperation("分页查询房屋列表")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Page<House>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String buildingNo,
            @RequestParam(required = false) String unitNo) {
        User currentUser = userService.getCurrentUser();
        
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        
        if ("ADMIN".equals(currentUser.getRole())) {
            Page<House> page = houseService.pageByCondition(current, size, userId, buildingNo, unitNo);
            return Result.success(page);
        } else {
            List<String> allowedBuildingNos = buildingService.getBuildingNosByStaffId(currentUser.getId());
            Page<House> page = houseService.pageByConditionWithBuildingAccess(current, size, userId, buildingNo, unitNo, allowedBuildingNos);
            return Result.success(page);
        }
    }

    @ApiOperation("获取当前用户的房屋列表")
    @GetMapping("/my")
    public Result<List<House>> getMyHouses() {
        Long userId = userService.getCurrentUserId();
        List<House> houses = houseService.getByUserId(userId);
        return Result.success(houses);
    }

    @ApiOperation("根据ID获取房屋信息")
    @GetMapping("/{id}")
    public Result<House> getById(@PathVariable Long id) {
        House house = houseService.getById(id);
        return Result.success(house);
    }

    @ApiOperation("新增房屋")
    @PostMapping
    public Result<Boolean> add(@RequestBody House house) {
        Long userId = userService.getCurrentUserId();
        house.setUserId(userId);
        return Result.success(houseService.save(house));
    }

    @ApiOperation("更新房屋信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody House house) {
        return Result.success(houseService.updateById(house));
    }

    @ApiOperation("删除房屋")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(houseService.removeById(id));
    }
}