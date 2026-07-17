package com.example.bankDemo.dto.customer;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CustomerSummaryDTO {
    private UUID publicId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}
