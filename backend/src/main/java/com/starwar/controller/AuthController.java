package com.starwar.controller;

import com.starwar.common.Result;
import com.starwar.dto.LoginDTO;
import com.starwar.dto.RegisterDTO;
import com.starwar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功", null);
    }
    
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        return Result.success("登录成功", data);
    }
}


