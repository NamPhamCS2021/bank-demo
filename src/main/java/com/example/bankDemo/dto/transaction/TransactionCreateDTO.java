package com.example.bankDemo.dto.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCreateDTO {

    public interface OnWithdraw{}
    public interface OnTransfer{}

    @NotNull(groups = {OnWithdraw.class, OnTransfer.class})
    private String accountNumber;
    @NotNull(groups = {OnTransfer.class})
    private String receiverNumber;
    @PositiveOrZero
    @NotNull(groups = {OnWithdraw.class, OnTransfer.class})
    private BigDecimal amount;
    @NotNull(groups = {OnWithdraw.class, OnTransfer.class})
    private String location;
}
