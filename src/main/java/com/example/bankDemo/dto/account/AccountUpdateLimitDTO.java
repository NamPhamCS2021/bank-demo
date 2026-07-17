package com.example.bankDemo.dto.account;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountUpdateLimitDTO {
    @PositiveOrZero
    private BigDecimal accountLimit;
}
