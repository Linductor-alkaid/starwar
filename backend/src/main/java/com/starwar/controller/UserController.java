package com.starwar.controller;

import com.starwar.common.Result;
import com.starwar.service.UserService;
import com.starwar.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@RequestHeader("Authorization") String token) {
        token = token.substring(7); // 去除 "Bearer " 前缀
        Long userId = com.starwar.util.JwtUtil.getUserId(token);
        UserVO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }
}


