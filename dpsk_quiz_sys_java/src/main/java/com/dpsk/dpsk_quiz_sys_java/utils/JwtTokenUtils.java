package com.dpsk.dpsk_quiz_sys_java.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.User;
import com.dpsk.dpsk_quiz_sys_java.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenUtils {

    private static UserService staticUserService;
    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtils.class);


    @Resource
    private UserService userService;

    @PostConstruct
    public void setUserService(){
        staticUserService=userService;
    }

    public static String genToken(Long userId,String sign){
        return JWT.create().withAudience(String.valueOf(userId))
                .withExpiresAt(DateUtil.offsetHour(new Date(),24))
                .sign(Algorithm.HMAC256(sign));
    }

    public static User getCurrentUser() {
        String token = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            token = request.getHeader("token");

            // 如果 header 中没有 token，尝试从参数中获取
            if (StrUtil.isBlank(token)) {
                token = request.getParameter("token");
            }

            // 如果 token 为空，记录日志
            if (StrUtil.isBlank(token)) {
                log.error("获取当前登陆的token失败, token: {}", token);
                return null;
            }

            // 解码 token 获取 userId
            String userId = JWT.decode(token).getAudience().get(0);

            // 从 staticUserService 获取用户信息，并返回 User 对象
            Optional<User> userOpt = staticUserService.findById(Long.valueOf(userId));

            // 判断 Optional 是否存在值，如果存在则返回，否则返回 null 或抛出异常
            return userOpt.orElse(null);  // 如果找不到用户返回 null，可以抛出自定义异常
        } catch (Exception e) {
            log.error("获取当前登陆的管理员信息失败, token: {}", token, e);
            return null;
        }
    }

}
