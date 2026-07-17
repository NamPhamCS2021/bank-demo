package com.example.bankDemo.dto.alert;

import com.example.bankDemo.enums.AlertStatus;
import com.example.bankDemo.enums.AlertType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class AlertUserSearchDTO {

    private AlertType type;
    private AlertStatus status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime start;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime end;
}
