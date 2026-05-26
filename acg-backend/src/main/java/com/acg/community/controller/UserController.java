package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.Result;
import com.acg.community.dto.LoginDTO;
import com.acg.community.dto.RegisterDTO;
import com.acg.community.dto.UserUpdateDTO;
import com.acg.community.service.UserService;
import com.acg.community.vo.UserVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        UserVO user = userService.login(dto);
        StpUtil.login(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenValue());
        data.put("user", user);
        return Result.success("登录成功", data);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", null);
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userService.getUserInfo(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateProfile(userId, dto);
        return Result.success("更新成功", null);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success("退出成功", null);
    }
}
