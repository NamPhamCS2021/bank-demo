package com.example.bankDemo.dto.customer;


import com.example.bankDemo.enums.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreateDTO {
    @NotBlank(message = "first name is required")
    private String firstName;

    @NotBlank(message = "last name is required")
    private String lastName;

    @NotBlank(message = "email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "phone number is required")
    private String phoneNumber;

    @NotNull(message = "Customer type is required")
    private CustomerType type;
}
