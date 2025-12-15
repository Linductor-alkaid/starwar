package com.starwar.filter;

import com.starwar.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT token并验证
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 获取Authorization请求头
        String authHeader = request.getHeader("Authorization");
        
        // 如果请求头存在且以Bearer开头
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                // 提取token（去掉"Bearer "前缀）
                String token = authHeader.substring(7);
                
                // 解析token
                Claims claims = JwtUtil.parseToken(token);
                
                // 获取用户ID和用户名
                Long userId = claims.get("userId", Long.class);
                String username = claims.get("username", String.class);
                
                // 创建认证对象
                // 这里使用简单的角色，可以根据实际需求扩展
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userId, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                
                // 将用户信息存储到请求属性中，方便Controller使用
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                
                // 设置到Spring Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
            } catch (Exception e) {
                // Token解析失败，清除认证信息
                SecurityContextHolder.clearContext();
                // 不抛出异常，让请求继续，由Spring Security的授权机制处理
            }
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
}

