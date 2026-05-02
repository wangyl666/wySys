package com.community.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.Building;

import java.util.List;

public interface BuildingService extends IService<Building> {
    
    Page<Building> pageByCondition(Integer current, Integer size, String buildingNo, String buildingName, Integer status);
    
    boolean addBuilding(Building building);
    
    boolean updateBuilding(Building building);
    
    boolean assignStaff(Long buildingId, Long[] staffIds);
    
    List<Building> getByStaffId(Long staffId);
    
    List<String> getBuildingNosByStaffId(Long staffId);
}
