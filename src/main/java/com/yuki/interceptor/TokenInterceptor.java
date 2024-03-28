package com.yuki.interceptor;


import com.yuki.Utils.JwtUtils;
import com.yuki.Utils.RedisUtils;
import com.yuki.entity.Result;
import com.yuki.entity.User;
import com.yuki.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final UserService userService;
    public TokenInterceptor(UserService userService) {
        this.userService = userService;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authHeader = request.getHeader("Authorization");
        String token = "";
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        try {
            Map<String, Object> claims = JwtUtils.parseTokenToGetUserInfo(token);
            User user = userService.getUserInfoById((Integer) claims.get("userId"));
            request.setAttribute("user",user);
            request.setAttribute("token",token);
        }catch (Exception e) {
            request.setAttribute("user",null);
        }
        return true;
    }
}
