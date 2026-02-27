package com.huzhu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huzhu.entity.User;

public interface UserService extends IService<User> {
    User login(String username, String password);
}