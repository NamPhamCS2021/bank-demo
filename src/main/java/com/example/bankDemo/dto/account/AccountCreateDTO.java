package com.example.bankDemo.dto.account;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCreateDTO {
    @NotNull(message = "Customer ID cannot be null")
    private UUID customerPublicId;
}
