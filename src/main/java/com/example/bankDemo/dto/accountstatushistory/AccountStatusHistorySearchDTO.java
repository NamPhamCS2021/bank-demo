package com.example.bankDemo.dto.accountstatushistory;


import com.example.bankDemo.enums.AccountStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class AccountStatusHistorySearchDTO {
    private String accountNumber;
    private AccountStatus status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime start;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime end;
}
