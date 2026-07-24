package com.interview.user.controller;

import com.interview.common.result.Result;
import com.interview.user.entity.SysUser;
import com.interview.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "获取用户信息")
    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable("id") Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.success(user);
    }

    @Operation(summary = "根据用户名获取用户")
    @GetMapping("/username/{username}")
    public Result<SysUser> getUserByUsername(@PathVariable("username") String username) {
        SysUser user = sysUserService.findByUsername(username);
        return Result.success(user);
    }
}