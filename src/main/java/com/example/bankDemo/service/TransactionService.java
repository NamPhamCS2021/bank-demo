package com.example.bankDemo.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.transaction.*;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {

    ApiResponse<Object> deposit(TransactionCreateDTO transactionCreateDTO);

    ApiResponse<Object> withdraw(TransactionCreateDTO transactionCreateDTO);

    ApiResponse<Object> transfer(TransactionCreateDTO transactionCreateDTO);

    ApiResponse<Object> getTransaction(UUID publicId);

//    ApiResponse<Object> getTransactionsByAccountId(Long accountId, Pageable pageable);
//
//    ApiResponse<Object> getTransactionsByType(TransactionType type, Pageable pageable);
//    ApiResponse<Object> getTransactionsByAccountIdAndType(Long accountId, TransactionType type, Pageable pageable);

    ApiResponse<Object> countTransactionsByLocation();

    ApiResponse<Object> search(TransactionSearchDTO dto, Pageable pageable);

    ApiResponse<Object> selfSearch(Long id, TransactionUserSearchDTO dto, Pageable pageable);

    ApiResponse<Object> selfSearchByAccountNumber(String accountNumber, TransactionUserSearchDTO dto, Pageable pageable);

    ApiResponse<Object> depositByAccountNumber(TransactionCreateANDTO transactionCreateDTO);

    ApiResponse<Object> withdrawByAccountNumber(TransactionCreateANDTO transactionCreateDTO);

    ApiResponse<Object> transferByAccountNumber(TransactionCreateANDTO transactionCreateDTO);
}
