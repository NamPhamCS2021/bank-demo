package com.example.bankDemo.service;

import com.example.bankDemo.dto.ApiResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StatementService {
    ApiResponse<Object> getMonthlyStatement(UUID customerPublicId, int year, int month, Pageable pageable);
}
