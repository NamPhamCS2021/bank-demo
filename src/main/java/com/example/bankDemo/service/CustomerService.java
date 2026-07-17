package com.example.bankDemo.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.customer.*;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    ApiResponse<Object> getCustomerByEmail(String email);
    ApiResponse<Object> createCustomer(CustomerCreateDTO customerCreateDTO);
    ApiResponse<Object> updateCustomer(UUID publicId, CustomerUpdateDTO customerUpdateDTO);
    ApiResponse<Object> getAll(Pageable pageable);
    ApiResponse<Object> getCustomerByPublicId(UUID publicId);
    ApiResponse<Object> search(CustomerSearchDTO dto, Pageable pageable);

}