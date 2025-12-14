package com.starwar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT不需要CSRF保护）
            .csrf().disable()
            // 启用CORS
            .cors()
            .and()
            // 配置会话管理（无状态，使用JWT）
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置请求授权
            .authorizeRequests()
                // 允许访问的公开接口
                .antMatchers("/api/v1/auth/**").permitAll()
                .antMatchers("/api/v1/game/leaderboard").permitAll()
                .antMatchers("/api/v1/post/list").permitAll()
                .antMatchers("/api/v1/post/detail/**").permitAll()
                .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            .and()
            // 禁用默认的登录页面和表单登录
            .formLogin().disable()
            // 禁用HTTP Basic认证
            .httpBasic().disable();
        
        return http.build();
    }

    /**
     * CORS配置源
     * 配置允许的跨域请求
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的前端地址
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8081",
            "http://localhost:3000",
            "http://localhost:8080"
        ));
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        // 允许携带凭证（Cookie等）
        configuration.setAllowCredentials(true);
        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
