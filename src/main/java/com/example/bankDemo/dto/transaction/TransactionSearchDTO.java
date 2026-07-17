package com.example.bankDemo.dto.transaction;

import com.example.bankDemo.enums.TransactionType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionSearchDTO {
    private TransactionType type;
    private String accountNumber;
    private String receiverNumber;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    private Boolean checked;
    private String location;
}
