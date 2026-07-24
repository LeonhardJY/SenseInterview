package com.interview.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.user.dto.LoginRequest;
import com.interview.user.dto.LoginResponse;
import com.interview.user.dto.RegisterRequest;
import com.interview.user.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    SysUser findByUsername(String username);

    SysUser findByEmail(String email);

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);
}