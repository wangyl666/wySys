package com.community.property.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.property.common.Result;
import com.community.property.entity.Building;
import com.community.property.entity.User;
import com.community.property.service.BuildingService;
import com.community.property.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "楼栋管理接口")
@RestController
@RequestMapping("/api/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;
    private final UserService userService;

    @ApiOperation("分页查询楼栋列表（管理员）")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<Page<Building>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String buildingNo,
            @RequestParam(required = false) String buildingName,
            @RequestParam(required = false) Integer status) {
        Page<Building> page = buildingService.pageByCondition(current, size, buildingNo, buildingName, status);
        return Result.success(page);
    }

    @ApiOperation("获取所有楼栋列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<List<Building>> list() {
        List<Building> list = buildingService.list(new LambdaQueryWrapper<Building>()
            .eq(Building::getStatus, 1)
            .orderByAsc(Building::getBuildingNo));
        return Result.success(list);
    }

    @ApiOperation("获取当前物业人员负责的楼栋列表")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<List<Building>> getMyBuildings() {
        Long userId = userService.getCurrentUserId();
        User user = userService.getCurrentUser();
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if ("ADMIN".equals(user.getRole())) {
            List<Building> allBuildings = buildingService.list(new LambdaQueryWrapper<Building>()
                .eq(Building::getStatus, 1)
                .orderByAsc(Building::getBuildingNo));
            return Result.success(allBuildings);
        }
        
        List<Building> myBuildings = buildingService.getByStaffId(userId);
        return Result.success(myBuildings);
    }

    @ApiOperation("根据ID获取楼栋信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Building> getById(@PathVariable Long id) {
        Building building = buildingService.getById(id);
        return Result.success(building);
    }

    @ApiOperation("新增楼栋")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> add(@RequestBody Building building) {
        return Result.success(buildingService.addBuilding(building));
    }

    @ApiOperation("更新楼栋信息")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> update(@RequestBody Building building) {
        return Result.success(buildingService.updateBuilding(building));
    }

    @ApiOperation("更新楼栋状态")
    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Building building = new Building();
        building.setId(id);
        building.setStatus(status);
        return Result.success(buildingService.updateById(building));
    }

    @ApiOperation("删除楼栋")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(buildingService.removeById(id));
    }

    @ApiOperation("分配楼栋给物业人员")
    @PostMapping("/assign/{buildingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> assignStaff(
            @PathVariable Long buildingId,
            @RequestBody Long[] staffIds) {
        return Result.success(buildingService.assignStaff(buildingId, staffIds));
    }
}
