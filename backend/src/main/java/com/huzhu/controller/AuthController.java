package com.huzhu.controller;

import com.huzhu.common.ApiResponse;
import com.huzhu.entity.User;
import com.huzhu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody User user) {
        User loginUser = userService.login(user.getUsername(), user.getPassword());
        if (loginUser == null) {
            return ApiResponse.fail("用户名或密码错误");
        }
        loginUser.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("token", "huzhu-token-" + loginUser.getId());
        data.put("user", loginUser);
        return ApiResponse.success("登录成功", data);
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            return ApiResponse.fail("用户名和密码不能为空");
        }
        User exists = userService.lambdaQuery().eq(User::getUsername, user.getUsername()).one();
        if (exists != null) {
            return ApiResponse.fail("用户名已存在");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        boolean saved = userService.save(user);
        if (!saved) {
            return ApiResponse.fail("注册失败");
        }

        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("token", "huzhu-token-" + user.getId());
        data.put("user", user);
        return ApiResponse.success("注册成功", data);
    }
}
