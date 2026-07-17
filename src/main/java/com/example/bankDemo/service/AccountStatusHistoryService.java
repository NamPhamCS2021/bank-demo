package com.example.bankDemo.service;


import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.accountstatushistory.AccountStatusHistorySearchDTO;
import com.example.bankDemo.dto.accountstatushistory.AccountStatusHistoryUserSearchDTO;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;

public interface AccountStatusHistoryService {

    ApiResponse<Object> findByAccountNumber(String accountNumber, Pageable pageable);
    ApiResponse<Object> findBetweenByAccount(String accountNumber, LocalDateTime start, LocalDateTime end, Pageable pageable);
    ApiResponse<Object> search(AccountStatusHistorySearchDTO accountStatusHistorySearchDTO, Pageable pageable);
    ApiResponse<Object> selfSearch(String accountNumber, AccountStatusHistoryUserSearchDTO accountStatusHistoryUserSearchDTO, Pageable pageable);

}
