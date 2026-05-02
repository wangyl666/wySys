package com.community.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.FeeType;

import java.util.List;

public interface FeeTypeService extends IService<FeeType> {
    
    Page<FeeType> pageByCondition(Integer current, Integer size, String typeName, Integer status);
    
    List<FeeType> getActiveList();
}