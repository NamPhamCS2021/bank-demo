package com.example.bankDemo.security.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.security.model.AuthRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    ApiResponse<Object> login(AuthRequest authRequest, HttpServletResponse response);
    ApiResponse<Object> adminRegister(AuthRequest authRequest);
    ApiResponse<Object> getCurrentUser(Authentication authentication);
}
