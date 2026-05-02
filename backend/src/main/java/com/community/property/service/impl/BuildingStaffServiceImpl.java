package com.community.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.entity.BuildingStaff;
import com.community.property.mapper.BuildingStaffMapper;
import com.community.property.service.BuildingStaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingStaffServiceImpl extends ServiceImpl<BuildingStaffMapper, BuildingStaff> implements BuildingStaffService {

    @Override
    public List<BuildingStaff> getByBuildingId(Long buildingId) {
        return list(new LambdaQueryWrapper<BuildingStaff>()
            .eq(BuildingStaff::getBuildingId, buildingId)
            .orderByAsc(BuildingStaff::getId));
    }

    @Override
    public List<BuildingStaff> getByStaffId(Long staffId) {
        return list(new LambdaQueryWrapper<BuildingStaff>()
            .eq(BuildingStaff::getStaffId, staffId)
            .orderByAsc(BuildingStaff::getId));
    }

    @Override
    public void deleteByBuildingId(Long buildingId) {
        remove(new LambdaQueryWrapper<BuildingStaff>()
            .eq(BuildingStaff::getBuildingId, buildingId));
    }

    @Override
    public void deleteByStaffId(Long staffId) {
        remove(new LambdaQueryWrapper<BuildingStaff>()
            .eq(BuildingStaff::getStaffId, staffId));
    }
}
