package com.example.bankDemo.security.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.security.entity.User;
import com.example.bankDemo.security.model.PassChangRequest;


public interface UserService {

    boolean existsByUsername(String username);
    void save(User user);
    ApiResponse<Object> changePassword(String username, PassChangRequest passChangRequest);
    ApiResponse<Object> findByUsername(String name);
}
