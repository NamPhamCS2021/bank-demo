package com.example.bankDemo.service;


import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.accountstatushistory.AccountStatusHistoryResponseDTO;
import com.example.bankDemo.dto.accountstatushistory.AccountStatusHistorySearchDTO;
import com.example.bankDemo.dto.accountstatushistory.AccountStatusHistoryUserSearchDTO;
import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.AccountStatusHistory;
import com.example.bankDemo.enums.ReturnMessage;
import com.example.bankDemo.repository.AccountRepository;
import com.example.bankDemo.repository.AccountStatusHistoryRepository;
import com.example.bankDemo.specification.AccountStatusHistorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountStatusHistoryServiceImpl implements AccountStatusHistoryService {


    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    private final AccountRepository accountRepository;




    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> findByAccountNumber(String accountNumber, Pageable pageable){
        try{
            Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
            if(accountOptional.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Page<AccountStatusHistory> accountStatusHistoryPage = accountStatusHistoryRepository.findByAccountNumber(accountNumber, pageable);
            return new ApiResponse<>(accountStatusHistoryPage.map(this::toAccountStatusHistoryDTO), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> findBetweenByAccount(String accountNumber, LocalDateTime start, LocalDateTime end, Pageable pageable){
        try{
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Page<AccountStatusHistory> accountStatusHistoryPage = accountStatusHistoryRepository.findBetweenByAccountNumber(accountNumber, start, end, pageable);
            return new ApiResponse<>(accountStatusHistoryPage.map(this::toAccountStatusHistoryDTO), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }

    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> search(AccountStatusHistorySearchDTO dto, Pageable pageable) {
        try{

            if(dto == null) {
                return new ApiResponse<>(ReturnMessage.NULL_VALUE.getCode(), ReturnMessage.NULL_VALUE.getMessage());
            }

            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(dto.getAccountNumber());
            if(optionalAccount.isEmpty())
            {
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Account account = optionalAccount.get();
            if((dto.getStart() != null && dto.getEnd() != null && dto.getStart().isAfter(dto.getEnd())))
            {
                return new ApiResponse<>(ReturnMessage.INVALID_ARGUMENTS.getCode(), ReturnMessage.INVALID_ARGUMENTS.getMessage());
            }
            Specification<AccountStatusHistory> spec = (root, query, builder) -> builder.conjunction(); // base

            spec = spec.and(AccountStatusHistorySpecification.hasAccount(account.getId()));
            spec = spec.and(AccountStatusHistorySpecification.hasStatus(dto.getStatus()));
            spec = spec.and(AccountStatusHistorySpecification.createdBefore(dto.getEnd()));
            spec = spec.and(AccountStatusHistorySpecification.createdAfter(dto.getStart()));
            Page<AccountStatusHistory> accountStatusHistoryPage = accountStatusHistoryRepository.findAll(spec, pageable);
            Page<AccountStatusHistoryResponseDTO> accountStatusHistoryResponseDTOPage = accountStatusHistoryPage.map(this::toAccountStatusHistoryDTO);
            return new ApiResponse<>(accountStatusHistoryResponseDTOPage, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> selfSearch(String accountNumber, AccountStatusHistoryUserSearchDTO dto, Pageable pageable) {
        try{

            if(dto == null) {
                return new ApiResponse<>(ReturnMessage.NULL_VALUE.getCode(), ReturnMessage.NULL_VALUE.getMessage());
            }

            if((dto.getEnd() != null && dto.getStart() != null && dto.getStart().isAfter(dto.getEnd())))
            {
                return new ApiResponse<>(ReturnMessage.INVALID_ARGUMENTS.getCode(), ReturnMessage.INVALID_ARGUMENTS.getMessage());
            }
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Account account = optionalAccount.get();
            Specification<AccountStatusHistory> spec = (root, query, builder) -> builder.conjunction(); // base

            spec = spec.and(AccountStatusHistorySpecification.hasAccount(account.getId()));
            spec = spec.and(AccountStatusHistorySpecification.hasStatus(dto.getStatus()));
            spec = spec.and(AccountStatusHistorySpecification.createdBefore(dto.getEnd()));
            spec = spec.and(AccountStatusHistorySpecification.createdAfter(dto.getStart()));
            Page<AccountStatusHistory> accountStatusHistoryPage = accountStatusHistoryRepository.findAll(spec, pageable);
            Page<AccountStatusHistoryResponseDTO> accountStatusHistoryResponseDTOPage = accountStatusHistoryPage.map(this::toAccountStatusHistoryDTO);
            return new ApiResponse<>(accountStatusHistoryResponseDTOPage, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    //helper
    private AccountStatusHistoryResponseDTO toAccountStatusHistoryDTO(AccountStatusHistory accountStatusHistory) {
        return AccountStatusHistoryResponseDTO.builder()
                .accountNumber(accountStatusHistory.getAccount().getAccountNumber())
                .status(accountStatusHistory.getStatus())
                .timestamp(accountStatusHistory.getTimestamp())
                .build();
    }


}
