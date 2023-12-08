package com.kob.matchingsystem.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(CsrfConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(new IpAddressMatcher("127.0.0.1", "/error")).permitAll();  // 放行报错页面
                    auth.requestMatchers(new IpAddressMatcher("127.0.0.1", "/matching/add/")).permitAll();
                    auth.requestMatchers(new IpAddressMatcher("127.0.0.1", "/matching/remove/")).permitAll();
                    auth.requestMatchers(HttpMethod.OPTIONS).permitAll();
                    auth.anyRequest().authenticated();
                });

        return http.build();
    }

    private record IpAddressMatcher(String remoteAddr, String servletPath) implements RequestMatcher {
        @Override
        public boolean matches(HttpServletRequest request) {
            // 放行来自http://remoteAddr/servletPath的请求
            return remoteAddr.equals(request.getRemoteAddr()) && servletPath.equals(request.getServletPath());
        }
    }
}
