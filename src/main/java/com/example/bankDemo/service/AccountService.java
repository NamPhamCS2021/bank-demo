package com.example.bankDemo.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.account.*;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AccountService {
    ApiResponse<Object> createAccount(AccountCreateDTO accountCreateDTO);
    ApiResponse<Object> updateAccountStatus(String accountNumber, AccountUpdateStatusDTO accountUpdate);
    ApiResponse<Object> updateAccountLimit(String accountNumber, AccountUpdateLimitDTO accountUpdate);
    ApiResponse<Object> getAccountByCustomerId(UUID customerPublicId, Pageable pageable);
    ApiResponse<Object> getAccountById(Long Id);
    ApiResponse<Object> getAllAccounts(Pageable pageable);
    ApiResponse<Object> search(AccountSearchDTO accountSearchDTO, Pageable pageable);
    ApiResponse<Object> selfSearch(UUID customerPublicId, AccountUserSearchDTO accountUserSearchDTO, Pageable pageable);
    ApiResponse<Object> getAccountsByEmail(String email, Pageable pageable);
    Long findAccountIdByAccountNumber(String accountNumber);

    ApiResponse<Object> getAccountByAccountNumber(String accountNumber);
    ApiResponse<Object> getReceiver(String accountNumber);
}
