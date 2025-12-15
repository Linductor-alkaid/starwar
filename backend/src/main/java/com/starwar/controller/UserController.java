package com.starwar.controller;

import com.starwar.common.Result;
import com.starwar.service.UserService;
import com.starwar.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        // 从请求属性中获取userId（由JwtAuthenticationFilter设置）
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未找到用户信息");
        }
        UserVO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }
}


