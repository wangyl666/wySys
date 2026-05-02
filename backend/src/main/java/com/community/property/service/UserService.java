package com.community.property.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.property.entity.User;

public interface UserService extends IService<User> {
    
    User getByUsername(String username);
    
    User login(String username, String password);
    
    boolean register(User user);
    
    User getCurrentUser();
    
    Long getCurrentUserId();
}