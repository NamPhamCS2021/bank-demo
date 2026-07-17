package com.example.bankDemo.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUpdateDTO {

    @NotBlank(message = "phone number is required")
    private String phoneNumber;

    @NotBlank(message = "email is required")
    @Email(message = "Please provide a valid email")
    private String email;
}
