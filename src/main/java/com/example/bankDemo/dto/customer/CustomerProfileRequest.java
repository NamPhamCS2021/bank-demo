package com.example.bankDemo.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// DTO for customer profile requests
public  class CustomerProfileRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String type;
}