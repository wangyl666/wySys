package com.community.property.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.property.entity.House;
import com.community.property.entity.Repair;
import com.community.property.entity.User;
import com.community.property.mapper.HouseMapper;
import com.community.property.mapper.RepairMapper;
import com.community.property.mapper.UserMapper;
import com.community.property.service.RepairService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairServiceImpl extends ServiceImpl<RepairMapper, Repair> implements RepairService {

    private final UserMapper userMapper;
    private final HouseMapper houseMapper;

    @Override
    public Page<Repair> pageByCondition(Integer current, Integer size, Long userId, String status, String type) {
        LambdaQueryWrapper<Repair> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Repair::getUserId, userId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Repair::getStatus, status);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Repair::getType, type);
        }
        wrapper.orderByDesc(Repair::getCreateTime);
        
        Page<Repair> page = page(new Page<>(current, size), wrapper);
        fillUserInfo(page.getRecords());
        return page;
    }

    @Override
    public Page<Repair> pageByStaff(Integer current, Integer size, Long staffId, String status) {
        LambdaQueryWrapper<Repair> wrapper = new LambdaQueryWrapper<>();
        if (staffId != null) {
            wrapper.eq(Repair::getStaffId, staffId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Repair::getStatus, status);
        }
        wrapper.orderByDesc(Repair::getCreateTime);
        
        Page<Repair> page = page(new Page<>(current, size), wrapper);
        fillUserInfo(page.getRecords());
        return page;
    }

    @Override
    public Page<Repair> pageByConditionWithBuildingAccess(Integer current, Integer size, Long userId, String status, String type, List<String> allowedBuildingNos) {
        if (allowedBuildingNos == null || allowedBuildingNos.isEmpty()) {
            return new Page<>(current, size);
        }
        
        List<House> houses = houseMapper.selectList(
            new LambdaQueryWrapper<House>()
                .in(House::getBuildingNo, allowedBuildingNos)
        );
        
        if (houses.isEmpty()) {
            return new Page<>(current, size);
        }
        
        List<Long> userIdsFromHouses = houses.stream()
            .map(House::getUserId)
            .distinct()
            .collect(Collectors.toList());
        
        LambdaQueryWrapper<Repair> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            if (userIdsFromHouses.contains(userId)) {
                wrapper.eq(Repair::getUserId, userId);
            } else {
                wrapper.eq(Repair::getId, -1);
            }
        } else {
            wrapper.in(Repair::getUserId, userIdsFromHouses);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Repair::getStatus, status);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Repair::getType, type);
        }
        wrapper.orderByDesc(Repair::getCreateTime);
        
        Page<Repair> page = page(new Page<>(current, size), wrapper);
        fillUserInfo(page.getRecords());
        return page;
    }

    private void fillUserInfo(List<Repair> repairs) {
        for (Repair repair : repairs) {
            if (repair.getImages() != null && !repair.getImages().isEmpty()) {
                try {
                    repair.setImageList(JSON.parseArray(repair.getImages(), String.class));
                } catch (Exception e) {
                    log.warn("解析报修图片失败：{}", repair.getImages());
                }
            }
            if (repair.getUserId() != null) {
                User user = userMapper.selectById(repair.getUserId());
                if (user != null) {
                    repair.setRealName(user.getRealName());
                    repair.setPhone(user.getPhone());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitRepair(Repair repair) {
        repair.setStatus("PENDING");
        if (repair.getImageList() != null && !repair.getImageList().isEmpty()) {
            repair.setImages(JSON.toJSONString(repair.getImageList()));
        }
        return save(repair);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRepair(Long id, Long staffId, String staffName, String staffPhone) {
        Repair repair = getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        if (!"PENDING".equals(repair.getStatus())) {
            throw new RuntimeException("当前状态不支持派单");
        }
        
        Repair update = new Repair();
        update.setId(id);
        update.setStaffId(staffId);
        update.setStaffName(staffName);
        update.setStaffPhone(staffPhone);
        update.setStatus("PROCESSING");
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processRepair(Long id, String processContent) {
        Repair repair = getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        if (!"PROCESSING".equals(repair.getStatus())) {
            throw new RuntimeException("当前状态不支持处理");
        }
        
        Repair update = new Repair();
        update.setId(id);
        update.setProcessContent(processContent);
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeRepair(Long id) {
        Repair repair = getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        if (!"PROCESSING".equals(repair.getStatus())) {
            throw new RuntimeException("当前状态不支持完成");
        }
        
        Repair update = new Repair();
        update.setId(id);
        update.setStatus("COMPLETED");
        update.setProcessTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rateRepair(Long id, Integer rating, String comment) {
        Repair repair = getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        if (!"COMPLETED".equals(repair.getStatus())) {
            throw new RuntimeException("只有已完成的报修才能评价");
        }
        
        Repair update = new Repair();
        update.setId(id);
        update.setRating(rating);
        update.setComment(comment);
        update.setCommentTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRepair(Long id) {
        Repair repair = getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        if (!"PENDING".equals(repair.getStatus()) && !"PROCESSING".equals(repair.getStatus())) {
            throw new RuntimeException("当前状态不支持取消");
        }
        
        Repair update = new Repair();
        update.setId(id);
        update.setStatus("CANCELLED");
        return updateById(update);
    }
}