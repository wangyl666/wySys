package com.community.property.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.BuildingStaff;

import java.util.List;

public interface BuildingStaffService extends IService<BuildingStaff> {
    
    List<BuildingStaff> getByBuildingId(Long buildingId);
    
    List<BuildingStaff> getByStaffId(Long staffId);
    
    void deleteByBuildingId(Long buildingId);
    
    void deleteByStaffId(Long staffId);
}
