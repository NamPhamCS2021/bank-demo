package com.example.bankDemo.controller;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.account.*;
import com.example.bankDemo.service.AccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PreAuthorize("@authSecurity.isSelfCustomer(#accountCreateDTO.customerPublicId)")
    @PostMapping
    public ApiResponse<Object> createAccount(@Valid @RequestBody AccountCreateDTO accountCreateDTO){
        return accountService.createAccount(accountCreateDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PutMapping("status/{accountNumber}")
    public ApiResponse<Object> updateAccountStatus(@PathVariable String accountNumber, @Valid @RequestBody AccountUpdateStatusDTO accountUpdateStatusDTO){
        return accountService.updateAccountStatus(accountNumber, accountUpdateStatusDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PutMapping("accountLimit/{accountNumber}")
    public ApiResponse<Object> updateAccountLimit(@PathVariable String accountNumber, @Valid @RequestBody AccountUpdateLimitDTO accountUpdateLimitDTO){
        return accountService.updateAccountLimit(accountNumber, accountUpdateLimitDTO);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @GetMapping("/{id}")
    public ApiResponse<Object> getAccountById(@PathVariable Long id){
        return accountService.getAccountById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public ApiResponse<Object> getAllAccount(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return accountService.getAllAccounts(pageable);
    }

    @PreAuthorize("@authSecurity.isSelfCustomer(#customerId)")
    @GetMapping("customer/{customerId}")
    public ApiResponse<Object> getAccountsByCustomerId(@PathVariable UUID customerId,
                                                                       @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)Pageable pageable){
        return accountService.getAccountByCustomerId(customerId, pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/search")
    public ApiResponse<Object> searchAccounts(@Valid @RequestBody AccountSearchDTO accountSearchDTO, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return accountService.search(accountSearchDTO, pageable);
    }

    @PreAuthorize("@authSecurity.isSelfCustomer(#id)")
    @PostMapping("/search/{id}")
    public ApiResponse<Object> searchSelfAccount(@PathVariable UUID id, @Valid @RequestBody AccountUserSearchDTO dto, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return accountService.selfSearch(id, dto, pageable);
    }

    @PreAuthorize("@authSecurity.isOwnerOfAccountByAccountNumber(#accountNumber)")
    @GetMapping("/byAccountNumber/{accountNumber}")
    public ApiResponse<Object> getAccountByAccountNumber(@PathVariable String accountNumber) {
        return accountService.getAccountByAccountNumber(accountNumber);
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/byAccountNumber/receiver/{accountNumber}")
    public ApiResponse<Object> getReceiverByAccountNumber(@PathVariable String accountNumber) {
        return accountService.getReceiver(accountNumber);
    }

    @GetMapping("/me")
    public ApiResponse<Object> getMyAccounts(Authentication auth, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)Pageable pageable) {
        String mail = auth.getName();
        return accountService.getAccountsByEmail(mail, pageable);
    }
}
