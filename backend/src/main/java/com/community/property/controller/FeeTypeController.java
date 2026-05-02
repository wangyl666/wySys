package com.community.property.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.property.common.Result;
import com.community.property.entity.FeeType;
import com.community.property.service.FeeTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "费用类型接口")
@RestController
@RequestMapping("/api/fee-type")
@RequiredArgsConstructor
public class FeeTypeController {

    private final FeeTypeService feeTypeService;

    @ApiOperation("获取所有启用的费用类型")
    @GetMapping("/list")
    public Result<List<FeeType>> listActive() {
        List<FeeType> list = feeTypeService.getActiveList();
        return Result.success(list);
    }

    @ApiOperation("分页查询费用类型列表")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Page<FeeType>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) Integer status) {
        Page<FeeType> page = feeTypeService.pageByCondition(current, size, typeName, status);
        return Result.success(page);
    }

    @ApiOperation("根据ID获取费用类型详情")
    @GetMapping("/{id}")
    public Result<FeeType> getById(@PathVariable Long id) {
        FeeType feeType = feeTypeService.getById(id);
        return Result.success(feeType);
    }

    @ApiOperation("新增费用类型")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> add(@RequestBody FeeType feeType) {
        feeType.setStatus(1);
        return Result.success(feeTypeService.save(feeType));
    }

    @ApiOperation("更新费用类型")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    public Result<Boolean> update(@RequestBody FeeType feeType) {
        return Result.success(feeTypeService.updateById(feeType));
    }

    @ApiOperation("删除费用类型")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(feeTypeService.removeById(id));
    }
}