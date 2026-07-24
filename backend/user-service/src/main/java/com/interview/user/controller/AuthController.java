package com.interview.user.controller;

import com.interview.common.result.Result;
import com.interview.user.dto.LoginRequest;
import com.interview.user.dto.LoginResponse;
import com.interview.user.dto.RegisterRequest;
import com.interview.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "登录注册相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = sysUserService.login(request);
        return Result.success(response);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        sysUserService.register(request);
        return Result.success();
    }

    @Operation(summary = "手机号登录（未实现）")
    @PostMapping("/sms")
    public Result<Void> smsLogin(@RequestParam String phone) {
        return Result.error("该功能暂未上线，敬请期待");
    }

    @Operation(summary = "GitHub登录（未实现）")
    @PostMapping("/github")
    public Result<Void> githubLogin() {
        return Result.error("该功能暂未上线，敬请期待");
    }
}