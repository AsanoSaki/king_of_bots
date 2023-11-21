package com.kob.backend.config.filter;

import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");  // 从请求头中获取名为"Authorization"的字段

        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {  // 这个字段应该包含一个以"Bearer "开头的JWT
            filterChain.doFilter(request, response);  // 将请求传递给下一个过滤器或处理器
            return;
        }

        token = token.substring(7);  // 跳过"Bearer "共7个字符

        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);  // 解析JWT，获取JWT的载荷
            userid = claims.getSubject();  // 从载荷中获取"subject"，这个"subject"应该是用户ID
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        User user = userMapper.selectById(Integer.parseInt(userid));  // 查询数据库获取用户信息

        if (user == null) {
            throw new RuntimeException("用户未登录");
        }

        UserDetailsImpl loginUser = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);  // 将其设置到Spring Security的上下文中

        filterChain.doFilter(request, response);  // 将请求传递给下一个过滤器或处理器
    }
}
