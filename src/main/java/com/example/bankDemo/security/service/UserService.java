package com.example.bankDemo.security.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.security.entity.User;


public interface UserService {

    boolean existsByUsername(String username);
    void save(User user);

    ApiResponse<Object> findByUsername(String name);
}
