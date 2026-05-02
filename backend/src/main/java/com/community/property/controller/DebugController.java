package com.community.property.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.property.common.Result;
import com.community.property.entity.User;
import com.community.property.mapper.UserMapper;
import com.community.property.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "调试接口（仅限开发环境使用）")
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @ApiOperation("查看所有用户信息（调试用）")
    @GetMapping("/users")
    public Result<List<User>> listAllUsers() {
        log.info("调试接口：查看所有用户");
        List<User> users = userService.list(
            new LambdaQueryWrapper<User>()
                .select(User::getId, User::getUsername, User::getRealName, 
                        User::getPhone, User::getRole, User::getStatus, 
                        User::getCreateTime)
                .orderByAsc(User::getId)
        );
        return Result.success(users);
    }

    @ApiOperation("重置用户密码（调试用）")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(
            @ApiParam("用户名") @RequestParam String username,
            @ApiParam("新密码") @RequestParam String newPassword) {
        
        log.info("调试接口：重置用户密码，用户名: {}", username);
        
        User user = userService.getByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        String encodedPassword = passwordEncoder.encode(newPassword);
        log.info("原始密码: {}, 加密后: {}", newPassword, encodedPassword);
        
        user.setPassword(encodedPassword);
        boolean success = userService.updateById(user);
        
        if (success) {
            return Result.success("密码已重置为: " + newPassword + 
                " （加密后: " + encodedPassword + "）");
        } else {
            return Result.error("密码重置失败");
        }
    }

    @ApiOperation("修改用户角色（调试用）")
    @PostMapping("/update-role")
    public Result<String> updateRole(
            @ApiParam("用户名") @RequestParam String username,
            @ApiParam("角色(ADMIN/PROPERTY/RESIDENT)") @RequestParam String role) {
        
        log.info("调试接口：修改用户角色，用户名: {}, 新角色: {}", username, role);
        
        if (!role.equals("ADMIN") && !role.equals("PROPERTY") && !role.equals("RESIDENT")) {
            return Result.error("无效的角色，有效值：ADMIN, PROPERTY, RESIDENT");
        }
        
        User user = userService.getByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        user.setRole(role);
        boolean success = userService.updateById(user);
        
        if (success) {
            return Result.success("用户 " + username + " 角色已修改为: " + role);
        } else {
            return Result.error("角色修改失败");
        }
    }

    @ApiOperation("创建管理员账号（调试用）")
    @PostMapping("/create-admin")
    public Result<String> createAdmin(
            @ApiParam("用户名") @RequestParam(defaultValue = "admin") String username,
            @ApiParam("密码") @RequestParam(defaultValue = "admin123") String password,
            @ApiParam("真实姓名") @RequestParam(defaultValue = "系统管理员") String realName) {
        
        log.info("调试接口：创建管理员账号，用户名: {}", username);
        
        User existUser = userService.getByUsername(username);
        if (existUser != null) {
            log.info("用户已存在，更新密码和角色");
            existUser.setPassword(passwordEncoder.encode(password));
            existUser.setRole("ADMIN");
            existUser.setStatus(1);
            userService.updateById(existUser);
            return Result.success("管理员账号已更新！用户名: " + username + ", 密码: " + password);
        }
        
        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRealName(realName);
        admin.setPhone("13800000000");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        
        boolean success = userService.save(admin);
        
        if (success) {
            log.info("管理员账号创建成功！");
            return Result.success("管理员账号创建成功！用户名: " + username + ", 密码: " + password);
        } else {
            return Result.error("管理员账号创建失败");
        }
    }

    @ApiOperation("验证密码（调试用）")
    @PostMapping("/verify-password")
    public Result<String> verifyPassword(
            @ApiParam("用户名") @RequestParam String username,
            @ApiParam("密码") @RequestParam String password) {
        
        log.info("调试接口：验证密码，用户名: {}", username);
        
        User user = userService.getByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        log.info("数据库中的密码哈希: {}", user.getPassword());
        log.info("验证的明文密码: {}", password);
        
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        log.info("密码匹配结果: {}", matches);
        
        if (matches) {
            return Result.success("密码验证通过！密码: " + password + " 是正确的");
        } else {
            String newEncoded = passwordEncoder.encode(password);
            return Result.error("密码验证失败！" +
                " 数据库中的密码哈希: " + user.getPassword() +
                " 重新加密后的哈希: " + newEncoded);
        }
    }
}