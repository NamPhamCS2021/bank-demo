package com.example.bankDemo.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.alert.AlertSearchDTO;
import com.example.bankDemo.dto.alert.AlertUserSearchDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AlertService {
    void detectAbnormalTransactions();
    ApiResponse<Object> getAll(Pageable pageable);
    ApiResponse<Object> getByTransactionId(UUID transactionPublicId, Pageable pageable);
    ApiResponse<Object> getByAccountNumber(String accountNumber, Pageable pageable);
    ApiResponse<Object> search(AlertSearchDTO dto, Pageable pageable);
    ApiResponse<Object> selfSearch(UUID publicTransactionId, AlertUserSearchDTO dto, Pageable pageable);
}
