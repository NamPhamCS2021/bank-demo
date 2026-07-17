package com.example.bankDemo.dto.customer;

import com.example.bankDemo.enums.CustomerType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerSearchDTO {
    private UUID publicId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private CustomerType type;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;
}
