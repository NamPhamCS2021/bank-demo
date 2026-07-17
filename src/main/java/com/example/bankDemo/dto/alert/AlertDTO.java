package com.example.bankDemo.dto.alert;

import com.example.bankDemo.enums.AlertStatus;
import com.example.bankDemo.enums.AlertType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDTO {
    private String accountNumber;
    private UUID transactionPublicId;
    private String description;
    private AlertType type;
    private AlertStatus status;
    private LocalDateTime timestamp;
}
