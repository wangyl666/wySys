package com.community.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.House;

import java.util.List;

public interface HouseService extends IService<House> {
    
    Page<House> pageByCondition(Integer current, Integer size, Long userId, String buildingNo, String unitNo);
    
    Page<House> pageByConditionWithBuildingAccess(Integer current, Integer size, Long userId, String buildingNo, String unitNo, List<String> allowedBuildingNos);
    
    List<House> getByUserId(Long userId);
    
    List<House> getByBuildingNos(List<String> buildingNos);
}