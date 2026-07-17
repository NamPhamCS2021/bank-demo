package com.example.bankDemo.dto.periodicallypayment;

import com.example.bankDemo.enums.Period;
import com.example.bankDemo.enums.SubscriptionStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PeriodicalPaymentUserSearchDTO {

    private String description;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime startedBefore;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime startedAfter;
    private Period period;
    private SubscriptionStatus status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime endedBefore;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime endedAfter;
}
