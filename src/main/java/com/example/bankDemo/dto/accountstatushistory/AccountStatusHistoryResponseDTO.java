package com.example.bankDemo.dto.accountstatushistory;


import com.example.bankDemo.enums.AccountStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatusHistoryResponseDTO {
    private String accountNumber;
    private AccountStatus status;
    private LocalDateTime timestamp;
}
