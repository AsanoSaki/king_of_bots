package com.kob.backend.config;

import com.kob.backend.config.filter.JwtAuthenticationTokenFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  // Spring Security用来处理HTTP请求的安全过滤器链
        http.csrf(CsrfConfigurer::disable)  // 禁用了CSRF（跨站请求伪造）保护
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 设置会话创建策略为无状态，意味着Spring Security不会创建或使用HTTP会话
                .authorizeHttpRequests(auth -> {
//                    auth.requestMatchers("/user/account/login/", "/user/account/register/").permitAll();  // 使用这种也行，但是无法通过浏览器地址访问，只能通过前端发出HTTP请求
                    auth.requestMatchers(new IpAddressMatcher("0:0:0:0:0:0:0:1", "/error")).permitAll();  // 0:0:0:0:0:0:0:1是localhost的IPv6版本
                    auth.requestMatchers(new IpAddressMatcher("0:0:0:0:0:0:0:1", "/user/account/login/")).permitAll();
                    auth.requestMatchers(new IpAddressMatcher("0:0:0:0:0:0:0:1", "/user/account/register/")).permitAll();
                    auth.requestMatchers(new IpAddressMatcher("127.0.0.1", "/pk/startgame/")).permitAll();
                    auth.requestMatchers(HttpMethod.OPTIONS).permitAll();
                    auth.anyRequest().authenticated();
                });

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) -> web.ignoring().requestMatchers("/websocket/**");
    }

    private record IpAddressMatcher(String remoteAddr, String servletPath) implements RequestMatcher {  // 自定义请求匹配器
        @Override
        public boolean matches(HttpServletRequest request) {
            // 放行来自http://remoteAddr/servletPath的请求
            return remoteAddr.equals(request.getRemoteAddr()) && servletPath.equals(request.getServletPath());
        }
    }
}
