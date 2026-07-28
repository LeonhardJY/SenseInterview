package com.interview.user.controller;

import com.interview.common.result.Result;
import com.interview.user.entity.SysUser;
import com.interview.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // ========== 管理员接口 ==========

    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public Result<List<SysUser>> getUserList() {
        List<SysUser> list = sysUserService.list();
        return Result.success(list);
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/status")
    public Result<Void> updateUserStatus(@RequestBody UpdateStatusRequest request) {
        SysUser user = sysUserService.getById(request.getId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(request.getStatus());
        sysUserService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long id) {
        sysUserService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "获取用户统计")
    @GetMapping("/stats")
    public Result<UserStats> getUserStats() {
        long totalUsers = sysUserService.count();
        long activeUsers = sysUserService.lambdaQuery().eq(SysUser::getStatus, 1).count();
        long adminUsers = sysUserService.lambdaQuery().eq(SysUser::getRole, "ADMIN").count();

        UserStats stats = new UserStats();
        stats.setTotalUsers(totalUsers);
        stats.setActiveUsers(activeUsers);
        stats.setDisabledUsers(totalUsers - activeUsers);
        stats.setAdminUsers(adminUsers);
        return Result.success(stats);
    }

    @lombok.Data
    public static class UpdateStatusRequest {
        private Long id;
        private Integer status;
    }

    @lombok.Data
    public static class UserStats {
        private Long totalUsers;
        private Long activeUsers;
        private Long disabledUsers;
        private Long adminUsers;
    }
}