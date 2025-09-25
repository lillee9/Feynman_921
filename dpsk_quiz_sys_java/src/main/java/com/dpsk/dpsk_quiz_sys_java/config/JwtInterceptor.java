package com.dpsk.dpsk_quiz_sys_java.config;

import com.dpsk.dpsk_quiz_sys_java.exception.CustomException;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.User;
import com.dpsk.dpsk_quiz_sys_java.service.UserService;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // JWT验证已被注释，直接允许所有请求通过
        /*
        String token = null;
        
        // 1. 优先从Authorization头获取token
        String authHeader = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // 移除"Bearer "前缀
        }
        
        // 2. 如果Authorization头没有token，从Cookie获取
        if (StrUtil.isBlank(token)) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        
        // 3. 如果Cookie也没有，从请求参数获取
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }
        
        // 4. 如果都没有token，抛出异常
        if (StrUtil.isBlank(token)) {
            throw new CustomException("无token, 请重新登录");
        }
        // 2. 解析 Token 获取用户 ID
        String userId;
        try {
            userId = JWT.decode(token).getAudience().get(0);
        } catch (Exception e) {
            log.error("Token 解析失败, token={}", token, e);
            throw new CustomException("token验证失败, 请重新登录");
        }

        // 3. 查询用户是否存在
        User user = userService.findById(Long.parseLong(userId))
                .orElseThrow(() -> new CustomException("用户不存在，请重新登录"));

        // 4. 验证 Token 签名和过期时间
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            verifier.verify(token); // 自动检查 exp 和签名
        } catch (JWTVerificationException e) {
            log.error("Token 验证失败, userId={}", userId, e);
            throw new CustomException("token验证失败，请重新登录");
        }
        */

        return true; // 直接返回true，跳过所有JWT验证
    }
}