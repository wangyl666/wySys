package com.community.property.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.property.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}