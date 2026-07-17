package com.example.bankDemo.dto.account;

import com.example.bankDemo.enums.AccountStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class AccountResponseDTO {
    private String accountNumber;
    private AccountStatus status;
    private BigDecimal balance;
    private BigDecimal accountLimit;
    private LocalDateTime openingDate;
    private String customerName;
}
