package com.community.property.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.entity.House;
import com.community.property.entity.User;
import com.community.property.mapper.HouseMapper;
import com.community.property.mapper.UserMapper;
import com.community.property.service.UserService;
import com.community.property.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final HouseMapper houseMapper;

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public User login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        user.setToken(token);
        user.setPassword(null);
        return user;
    }

    @Override
    public boolean register(User user) {
        if (StrUtil.isBlank(user.getUsername())) {
            throw new RuntimeException("用户名不能为空");
        }
        if (StrUtil.isBlank(user.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }
        User existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("RESIDENT");
        user.setStatus(1);
        return save(user);
    }

    @Override
    public User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                return getByUsername(userDetails.getUsername());
            }
        } catch (Exception e) {
            log.error("获取当前用户失败：", e);
        }
        return null;
    }

    @Override
    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    @Override
    public Page<User> pageByBuildingAccess(Integer current, Integer size, String keyword, String role, Integer status, List<String> allowedBuildingNos) {
        if (allowedBuildingNos == null || allowedBuildingNos.isEmpty()) {
            Page<User> emptyPage = new Page<>(current, size);
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }
        
        List<House> houses = houseMapper.selectList(
            new LambdaQueryWrapper<House>()
                .in(House::getBuildingNo, allowedBuildingNos)
        );
        
        if (houses.isEmpty()) {
            Page<User> emptyPage = new Page<>(current, size);
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }
        
        List<Long> userIdsFromHouses = houses.stream()
            .map(House::getUserId)
            .distinct()
            .collect(Collectors.toList());
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getId, userIdsFromHouses);
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or().like(User::getRealName, keyword)
                .or().like(User::getPhone, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> page = page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(u -> u.setPassword(null));
        return page;
    }
}