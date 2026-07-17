package com.example.bankDemo.dto.periodicallypayment;

import com.example.bankDemo.enums.Period;
import com.example.bankDemo.enums.SubscriptionStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PeriodicalPaymentSearchDTO {

    private UUID publicId;
    private String description;
    private BigDecimal maxAmount;
    private BigDecimal minAmount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startedBefore;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startedAfter;
    private Period period;
    private SubscriptionStatus status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endedBefore;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endedAfter;
    private Long accountId;
}
