package com.example.bankDemo.dto.account;

import com.example.bankDemo.enums.AccountStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccountSearchDTO {

    private AccountStatus status;
    private UUID customerPublicId;

    private BigDecimal maxBalance;
    private BigDecimal minBalance;

    private BigDecimal maxLimit;
    private BigDecimal minLimit;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

}
