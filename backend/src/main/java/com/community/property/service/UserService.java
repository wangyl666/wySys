package com.community.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    
    User getByUsername(String username);
    
    User login(String username, String password);
    
    boolean register(User user);
    
    User getCurrentUser();
    
    Long getCurrentUserId();
    
    Page<User> pageByBuildingAccess(Integer current, Integer size, String keyword, String role, Integer status, List<String> allowedBuildingNos);
}