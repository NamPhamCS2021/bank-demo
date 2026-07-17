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
public class TransactionCreateANDTO {
    public interface OnWithdraw{}
    public interface OnTransfer{}

    @NotNull(groups = {TransactionCreateANDTO.OnWithdraw.class, TransactionCreateANDTO.OnTransfer.class})
    private String accountNumber;
    @NotNull(groups = {TransactionCreateANDTO.OnTransfer.class})
    private String receiverAccountNumber;
    @PositiveOrZero
    @NotNull(groups = {TransactionCreateANDTO.OnWithdraw.class, TransactionCreateANDTO.OnTransfer.class})
    private BigDecimal amount;
    @NotNull(groups = {TransactionCreateANDTO.OnWithdraw.class, TransactionCreateANDTO.OnTransfer.class})
    private String location;
}
