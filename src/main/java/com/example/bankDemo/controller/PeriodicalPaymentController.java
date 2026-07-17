package com.example.bankDemo.controller;

import com.example.bankDemo.dto.ApiResponse;

import com.example.bankDemo.dto.periodicallypayment.PeriodicalPaymentSearchDTO;
import com.example.bankDemo.dto.periodicallypayment.PeriodicalPaymentUserSearchDTO;
import com.example.bankDemo.dto.periodicallypayment.PeriodicallyPaymentUpdateDTO;
import com.example.bankDemo.service.PeriodicalPaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PeriodicalPaymentController {

    private final PeriodicalPaymentService periodicalPaymentService;

    @PreAuthorize("@authSecurity.isOwnerOfPayment(#id)")
    @GetMapping("/{id}")
    public ApiResponse<Object> findById(@PathVariable UUID id){
        return periodicalPaymentService.getPeriodicalPaymentById(id);
    }

    @PreAuthorize("@authSecurity.isOwnerOfAccountByAccountNumber(#accountNumber)")
    @GetMapping("/account/{accountNumber}")
    public ApiResponse<Object> findByAccountNumber(@PathVariable String accountNumber,
                                               @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.ASC) Pageable pageable){
        return periodicalPaymentService.getPeriodicalPaymentByAccountNumber(accountNumber, pageable);
    }


    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/search")
    public ApiResponse<Object> search(@Valid @RequestBody PeriodicalPaymentSearchDTO periodicalPaymentSearchDTO,
                                      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return periodicalPaymentService.search(periodicalPaymentSearchDTO, pageable);
    }

    @PreAuthorize("@authSecurity.isOwnerOfAccountByAccountNumber(#accountNumber)")
    @PostMapping("/search/{accountNumber}")
    public ApiResponse<Object> selfSearch(@PathVariable String accountNumber, @Valid @RequestBody PeriodicalPaymentUserSearchDTO dto,
                                                           @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return periodicalPaymentService.selfSearch(accountNumber, dto, pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<Object> update(@PathVariable UUID id, @Valid @RequestBody PeriodicallyPaymentUpdateDTO payment){
        return periodicalPaymentService.updatePeriodicalPayment(id, payment);
    }
}
