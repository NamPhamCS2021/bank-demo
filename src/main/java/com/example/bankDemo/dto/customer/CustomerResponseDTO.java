package com.example.bankDemo.dto.customer;

import com.example.bankDemo.entity.Account;
import com.example.bankDemo.enums.CustomerType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDTO {
    private UUID publicId;
    private String firstName;
    private String lastName;
    private String email;
    private CustomerType type;
    private String phoneNumber;
    private LocalDateTime createDate;
    private List<Account> accounts;

}
