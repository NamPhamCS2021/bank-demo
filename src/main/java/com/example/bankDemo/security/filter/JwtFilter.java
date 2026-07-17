package com.example.bankDemo.security.filter;

import com.example.bankDemo.security.service.UserDetailsServiceImpl;
import com.example.bankDemo.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;

    private final UserDetailsServiceImpl userDetailService;


    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String username = "unathenticated";
        try{

            String authToken =extractToken(request);
            if(StringUtils.hasText(authToken) && jwtUtil.validateJwtToken(authToken)){
                username = jwtUtil.getUserNameFromJwtToken(authToken);

                UserDetails userDetail = userDetailService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        MDC.put("username", username);
        MDC.put("ip", request.getRemoteAddr());
        MDC.put("method", request.getMethod());
        MDC.put("url", request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }

    }

    private String extractToken(HttpServletRequest request) {
        // 1. Try Authorization header
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.toLowerCase().startsWith("bearer ")) {
            return authHeader.substring(7).trim();
        }

        // 2. Try cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (StringUtils.hasText(value)) {
                        return value.trim();
                    }
                }
            }
        }

        // 3. No token found
        return null;
    }


}
