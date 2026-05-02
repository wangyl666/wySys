package com.community.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.entity.House;
import com.community.property.mapper.HouseMapper;
import com.community.property.service.HouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {

    @Override
    public Page<House> pageByCondition(Integer current, Integer size, Long userId, String buildingNo, String unitNo) {
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(House::getUserId, userId);
        }
        if (StringUtils.hasText(buildingNo)) {
            wrapper.eq(House::getBuildingNo, buildingNo);
        }
        if (StringUtils.hasText(unitNo)) {
            wrapper.eq(House::getUnitNo, unitNo);
        }
        wrapper.orderByDesc(House::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public Page<House> pageByConditionWithBuildingAccess(Integer current, Integer size, Long userId, String buildingNo, String unitNo, List<String> allowedBuildingNos) {
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        
        if (allowedBuildingNos != null && !allowedBuildingNos.isEmpty()) {
            wrapper.in(House::getBuildingNo, allowedBuildingNos);
        } else {
            wrapper.eq(House::getId, -1);
        }
        
        if (userId != null) {
            wrapper.eq(House::getUserId, userId);
        }
        if (StringUtils.hasText(buildingNo)) {
            wrapper.eq(House::getBuildingNo, buildingNo);
        }
        if (StringUtils.hasText(unitNo)) {
            wrapper.eq(House::getUnitNo, unitNo);
        }
        wrapper.orderByDesc(House::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public List<House> getByUserId(Long userId) {
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getUserId, userId);
        wrapper.orderByDesc(House::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<House> getByBuildingNos(List<String> buildingNos) {
        if (buildingNos == null || buildingNos.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(House::getBuildingNo, buildingNos);
        wrapper.orderByAsc(House::getBuildingNo, House::getUnitNo, House::getRoomNo);
        return list(wrapper);
    }
}