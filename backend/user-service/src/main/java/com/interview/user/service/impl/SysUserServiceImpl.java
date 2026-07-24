package com.interview.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.common.exception.BusinessException;
import com.interview.common.utils.JwtUtils;
import com.interview.user.dto.LoginRequest;
import com.interview.user.dto.LoginResponse;
import com.interview.user.dto.RegisterRequest;
import com.interview.user.entity.SysUser;
import com.interview.user.mapper.SysUserMapper;
import com.interview.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser findByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public SysUser findByEmail(String email) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, email);
        return getOne(wrapper);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public void register(RegisterRequest request) {
        if (findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        if (findByEmail(request.getEmail()) != null) {
            throw new BusinessException("邮箱已被注册");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        save(user);
    }
}