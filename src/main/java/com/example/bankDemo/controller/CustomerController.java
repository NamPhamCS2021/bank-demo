package com.example.bankDemo.controller;


import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.customer.CustomerCreateDTO;
import com.example.bankDemo.dto.customer.CustomerSearchDTO;
import com.example.bankDemo.dto.customer.CustomerUpdateDTO;
import com.example.bankDemo.security.repository.UserRepository;
import com.example.bankDemo.service.CustomerService;
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

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    private final UserRepository userRepository;

    @PostMapping
    public ApiResponse<Object> createCustomer(@Valid @RequestBody CustomerCreateDTO customerCreateDTO){
        return customerService.createCustomer(customerCreateDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public ApiResponse<Object> updateCustomer(@PathVariable UUID id, @Valid @RequestBody CustomerUpdateDTO customerUpdateDTO){
        return customerService.updateCustomer(id,customerUpdateDTO);
    }

    @PreAuthorize("@authSecurity.isSelfCustomer(#publicId)")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{publicId}")
    public ApiResponse<Object> getCustomerById(@PathVariable UUID publicId){
        return customerService.getCustomerByPublicId(publicId);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ApiResponse<Object> getAllCustomer
            (@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return customerService.getAll(pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/search")
    public ApiResponse<Object> searchCustomers(@Valid @RequestBody CustomerSearchDTO customerSearchDTO, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return customerService.search(customerSearchDTO, pageable);
    }
    @GetMapping("/profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<Object> getCustomerProfile(Authentication auth) {
        String email = auth.getName();
        return customerService.getCustomerByEmail(email);
    }
}
