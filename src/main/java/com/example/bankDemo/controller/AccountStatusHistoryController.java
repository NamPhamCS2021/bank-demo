package com.example.bankDemo.controller;

import com.example.bankDemo.dto.ApiResponse;

import com.example.bankDemo.dto.accountstatushistory.AccountStatusHistorySearchDTO;
import com.example.bankDemo.dto.accountstatushistory.AccountStatusHistoryUserSearchDTO;
import com.example.bankDemo.service.AccountStatusHistoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/accountStatusHistory")
@Validated
@RequiredArgsConstructor
public class AccountStatusHistoryController {

    private final AccountStatusHistoryService accountStatusHistoryService;


    @PreAuthorize("@authSecurity.isOwnerOfAccountByAccountNumber(#accountNumber)")
    @GetMapping("/account/{accountNumber}")
    public ApiResponse<Object> getAccountStatusHistoryById(@PathVariable String accountNumber,
                                                   @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return accountStatusHistoryService.findByAccountNumber(accountNumber, pageable);
    }

    @PreAuthorize("@authSecurity.isOwnerOfAccountByAccountNumber(#accountNumber)")
    @GetMapping("account/between")
    public ApiResponse<Object> getAccountStatusHistoryByIdAndStatus(@RequestParam String accountNumber,
                                                                    @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime start,
                                                                    @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime end,
                                                                    @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return accountStatusHistoryService.findBetweenByAccount(accountNumber, start, end, pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/search")
    public ApiResponse<Object> searchAccountStatusHistory(@Valid @RequestBody AccountStatusHistorySearchDTO dto,
                                                          @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return accountStatusHistoryService.search(dto, pageable);
    }

    @PreAuthorize("@authSecurity.isOwnerOfAccountByAccountNumber(#accountNumber)")
    @PostMapping("/search/{accountNumber}")
    public ApiResponse<Object> selfSearch(@PathVariable String accountNumber, @Valid @RequestBody AccountStatusHistoryUserSearchDTO dto,
                                          @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return accountStatusHistoryService.selfSearch(accountNumber, dto, pageable);
    }


}
