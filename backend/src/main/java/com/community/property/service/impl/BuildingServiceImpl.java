package com.community.property.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.entity.Building;
import com.community.property.entity.BuildingStaff;
import com.community.property.entity.User;
import com.community.property.mapper.BuildingMapper;
import com.community.property.mapper.BuildingStaffMapper;
import com.community.property.mapper.UserMapper;
import com.community.property.service.BuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements BuildingService {

    private final BuildingStaffMapper buildingStaffMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Building> pageByCondition(Integer current, Integer size, String buildingNo, String buildingName, Integer status) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(buildingNo)) {
            wrapper.like(Building::getBuildingNo, buildingNo);
        }
        if (StringUtils.hasText(buildingName)) {
            wrapper.like(Building::getBuildingName, buildingName);
        }
        if (status != null) {
            wrapper.eq(Building::getStatus, status);
        }
        wrapper.orderByAsc(Building::getBuildingNo);
        
        Page<Building> page = page(new Page<>(current, size), wrapper);
        
        for (Building building : page.getRecords()) {
            List<BuildingStaff> staffRelations = buildingStaffMapper.selectList(
                new LambdaQueryWrapper<BuildingStaff>()
                    .eq(BuildingStaff::getBuildingId, building.getId())
            );
            
            List<User> staffList = new ArrayList<>();
            Long[] staffIds = new Long[staffRelations.size()];
            
            for (int i = 0; i < staffRelations.size(); i++) {
                BuildingStaff staff = staffRelations.get(i);
                staffIds[i] = staff.getStaffId();
                User user = userMapper.selectById(staff.getStaffId());
                if (user != null) {
                    user.setPassword(null);
                    staffList.add(user);
                }
            }
            
            building.setStaffList(staffList);
            building.setStaffIds(staffIds);
        }
        
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBuilding(Building building) {
        Building exist = getOne(new LambdaQueryWrapper<Building>()
            .eq(Building::getBuildingNo, building.getBuildingNo()));
        if (exist != null) {
            throw new RuntimeException("楼栋号已存在");
        }
        building.setStatus(1);
        return save(building);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBuilding(Building building) {
        if (building.getBuildingNo() != null) {
            Building exist = getOne(new LambdaQueryWrapper<Building>()
                .eq(Building::getBuildingNo, building.getBuildingNo())
                .ne(Building::getId, building.getId()));
            if (exist != null) {
                throw new RuntimeException("楼栋号已存在");
            }
        }
        return updateById(building);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignStaff(Long buildingId, Long[] staffIds) {
        Building building = getById(buildingId);
        if (building == null) {
            throw new RuntimeException("楼栋不存在");
        }
        
        buildingStaffMapper.delete(new LambdaQueryWrapper<BuildingStaff>()
            .eq(BuildingStaff::getBuildingId, buildingId));
        
        if (staffIds != null && staffIds.length > 0) {
            for (Long staffId : staffIds) {
                User user = userMapper.selectById(staffId);
                if (user != null && "PROPERTY".equals(user.getRole())) {
                    BuildingStaff buildingStaff = new BuildingStaff();
                    buildingStaff.setBuildingId(buildingId);
                    buildingStaff.setBuildingNo(building.getBuildingNo());
                    buildingStaff.setStaffId(staffId);
                    buildingStaff.setStaffName(user.getRealName());
                    buildingStaff.setAssignTime(LocalDateTime.now());
                    buildingStaffMapper.insert(buildingStaff);
                }
            }
        }
        
        return true;
    }

    @Override
    public List<Building> getByStaffId(Long staffId) {
        List<BuildingStaff> staffRelations = buildingStaffMapper.selectList(
            new LambdaQueryWrapper<BuildingStaff>()
                .eq(BuildingStaff::getStaffId, staffId)
        );
        
        if (staffRelations.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> buildingIds = staffRelations.stream()
            .map(BuildingStaff::getBuildingId)
            .collect(Collectors.toList());
        
        return list(new LambdaQueryWrapper<Building>()
            .in(Building::getId, buildingIds)
            .eq(Building::getStatus, 1)
            .orderByAsc(Building::getBuildingNo));
    }

    @Override
    public List<String> getBuildingNosByStaffId(Long staffId) {
        List<BuildingStaff> staffRelations = buildingStaffMapper.selectList(
            new LambdaQueryWrapper<BuildingStaff>()
                .eq(BuildingStaff::getStaffId, staffId)
        );
        
        return staffRelations.stream()
            .map(BuildingStaff::getBuildingNo)
            .collect(Collectors.toList());
    }
}
