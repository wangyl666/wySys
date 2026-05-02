package com.community.property.controller;

import com.community.property.common.Result;
import com.community.property.dto.LoginDTO;
import com.community.property.dto.RegisterDTO;
import com.community.property.entity.User;
import com.community.property.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<User> login(@Validated @RequestBody LoginDTO dto) {
        User user = userService.login(dto.getUsername(), dto.getPassword());
        return Result.success(user);
    }

    @ApiOperation("注册")
    @PostMapping("/register")
    public Result<Boolean> register(@Validated @RequestBody RegisterDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        boolean result = userService.register(user);
        return Result.success(result);
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }
}