package com.community.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.entity.FeeType;
import com.community.property.mapper.FeeTypeMapper;
import com.community.property.service.FeeTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeTypeServiceImpl extends ServiceImpl<FeeTypeMapper, FeeType> implements FeeTypeService {

    @Override
    public Page<FeeType> pageByCondition(Integer current, Integer size, String typeName, Integer status) {
        LambdaQueryWrapper<FeeType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(typeName)) {
            wrapper.like(FeeType::getTypeName, typeName);
        }
        if (status != null) {
            wrapper.eq(FeeType::getStatus, status);
        }
        wrapper.orderByDesc(FeeType::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<FeeType> getActiveList() {
        LambdaQueryWrapper<FeeType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeType::getStatus, 1);
        wrapper.orderByAsc(FeeType::getCreateTime);
        return list(wrapper);
    }
}