package com.example.bankDemo.dto.transaction;

import com.example.bankDemo.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {
    private String accountNumber;
    private String receiverNumber;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime timestamp;
    private String location;
}
