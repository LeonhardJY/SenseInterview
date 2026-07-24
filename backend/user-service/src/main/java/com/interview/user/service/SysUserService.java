package com.interview.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.user.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    SysUser findByUsername(String username);

    SysUser findByEmail(String email);
}