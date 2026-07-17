package com.example.bankDemo.controller;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.alert.AlertSearchDTO;
import com.example.bankDemo.dto.alert.AlertUserSearchDTO;
import com.example.bankDemo.service.AlertService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/alerts")
@RestController
public class AlertController {

    public final AlertService alertService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public ApiResponse<Object> getAll(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return alertService.getAll(pageable);
    }

    @PreAuthorize("@authSecurity.isOwnerOfTransaction(#publicTransactionId)")
    @GetMapping("/transaction/id/{publicTransactionId}")
    public ApiResponse<Object> getByTransactionId(@PathVariable UUID publicTransactionId,
                                                  @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
         return alertService.getByTransactionId(publicTransactionId, pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/search")
    public ApiResponse<Object> search(@Valid @RequestBody AlertSearchDTO dto,
                                                          @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return alertService.search(dto, pageable);
    }

    @PreAuthorize("@authSecurity.isOwnerOfTransaction(#publicTransactionId)")
    @PostMapping("/search/{publicTransactionId}")
    public ApiResponse<Object> selfSearch(@PathVariable UUID publicTransactionId, @Valid @RequestBody AlertUserSearchDTO dto,
                                          @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return alertService.selfSearch(publicTransactionId, dto, pageable);
    }
}
