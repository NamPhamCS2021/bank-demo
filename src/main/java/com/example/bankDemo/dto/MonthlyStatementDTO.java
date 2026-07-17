package com.example.bankDemo.dto;

import com.example.bankDemo.dto.account.AccountResponseDTO;
import com.example.bankDemo.dto.transaction.TransactionResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatementDTO {

    private int year;
    private int month;

    private BigDecimal openingBalance;
    private BigDecimal closingBalance;

    private BigDecimal totalCredits;  // deposits + transfers in
    private BigDecimal totalDebits;   // withdrawals + transfers out

    private List<AccountResponseDTO> accounts;
    private List<TransactionResponseDTO> transactions;
}
