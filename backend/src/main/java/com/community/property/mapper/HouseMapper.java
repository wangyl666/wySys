package com.community.property.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.property.entity.House;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HouseMapper extends BaseMapper<House> {
}