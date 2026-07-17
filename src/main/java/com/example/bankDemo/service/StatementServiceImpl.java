package com.example.bankDemo.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.MonthlyStatementDTO;
import com.example.bankDemo.dto.account.AccountResponseDTO;
import com.example.bankDemo.dto.transaction.TransactionResponseDTO;
import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.Customer;
import com.example.bankDemo.entity.Transaction;
import com.example.bankDemo.enums.ReturnMessage;
import com.example.bankDemo.enums.TransactionType;
import com.example.bankDemo.repository.AccountRepository;
import com.example.bankDemo.repository.CustomerRepository;
import com.example.bankDemo.repository.TransactionRepository;
import com.example.bankDemo.specification.AccountSpecification;
import com.example.bankDemo.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    public final CustomerRepository customerRepository;

    @Override
    public ApiResponse<Object> getMonthlyStatement(UUID customerPublicId, int year, int month, Pageable pageable) {
        try{

            LocalDateTime from = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime to = from.plusMonths(1).minusNanos(1);
            Optional<Customer> optionalCustomer = customerRepository.findByPublicId(customerPublicId);
            if(optionalCustomer.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Customer customer = optionalCustomer.get();

            Specification<Account> cusSpec = (root, query, builder) -> builder.conjunction();
            cusSpec = cusSpec.and(AccountSpecification.hasCustomer(customer.getId()));


            List<Account> accounts = accountRepository.findAll(cusSpec);

            List<AccountResponseDTO> accountResponseDTOS = accounts.stream()
                    .map(this::toAccountResponseDTO).toList();
            List<TransactionResponseDTO> transactions = transactionRepository.findTransactionByCustomerPublicId(customerPublicId, pageable).stream().
                    map(this::toTransactionResponseDTO).toList();

            BigDecimal totalOpeningBalance = BigDecimal.ZERO;
            BigDecimal totalClosingBalance = BigDecimal.ZERO;
            BigDecimal totalCredit = BigDecimal.ZERO;
            BigDecimal totalDebit = BigDecimal.ZERO;

            for(Account account : accounts) {
                totalOpeningBalance = totalOpeningBalance.add(getBalanceAt(account.getAccountNumber(), from));
                totalClosingBalance = totalClosingBalance.add(getBalanceAt(account.getAccountNumber(), to));
                Specification<Transaction> spec = (root, query, builder) -> builder.conjunction();
                spec = spec.and(TransactionSpecification.hasAccountId(account.getId())
                        .or(TransactionSpecification.hasReceiverId(account.getId())));

                spec = spec.and(TransactionSpecification.occurredBefore(to));
                spec = spec.and(TransactionSpecification.occurredAfter(from));

                List<Transaction> transactionsOfAccounts = transactionRepository.findAll(spec);
                for (Transaction transaction : transactionsOfAccounts) {
                    if(transaction.getType().equals(TransactionType.DEPOSIT)
                            || (transaction.getType().equals(TransactionType.TRANSFER) && transaction.getReceiver().getId().equals((account.getId())))){
                        totalCredit = totalCredit.add(transaction.getAmount());
                    }

                    if(transaction.getType().equals(TransactionType.WITHDRAWAL)
                            || ((transaction.getType().equals(TransactionType.TRANSFER) && transaction.getAccount().getId().equals(account.getId())))){
                        totalDebit = totalDebit.add(transaction.getAmount());
                    }
                }
            }

            MonthlyStatementDTO dto = MonthlyStatementDTO.builder()
                    .openingBalance(totalOpeningBalance)
                    .closingBalance(totalClosingBalance)
                    .totalCredits(totalCredit)
                    .totalDebits(totalDebit)
                    .month(month)
                    .accounts(accountResponseDTOS)
                    .transactions(transactions)
                    .year(year).build();
            return new ApiResponse<>(dto, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());


        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    private BigDecimal getBalanceAt(String accountNumber, LocalDateTime time) {
        Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
        if(optionalAccount.isEmpty()){
            return BigDecimal.ZERO;
        }
        Account account = optionalAccount.get();

        BigDecimal balance = account.getBalance();
        Specification<Transaction> spec = (root, query, builder) -> builder.conjunction();
        spec = spec.and(TransactionSpecification.hasAccountId(account.getId()).or(TransactionSpecification.hasReceiverId(account.getId())));
        spec = spec.and(TransactionSpecification.occurredAfter(time));

        List<Transaction> transactions = transactionRepository.findAll(spec);

        for (Transaction transaction : transactions) {
            if(transaction.getType().equals(TransactionType.DEPOSIT)
                    || (transaction.getType().equals(TransactionType.TRANSFER) && transaction.getReceiver().getId().equals((account.getId())))){
                balance = balance.subtract(transaction.getAmount());
            }

            if(transaction.getType().equals(TransactionType.WITHDRAWAL)
                    || ((transaction.getType().equals(TransactionType.TRANSFER) && transaction.getAccount().getId().equals(account.getId())))){
                balance = balance.add(transaction.getAmount());
            }
        }
        return balance;
    }

    private AccountResponseDTO toAccountResponseDTO(Account account) {
        return AccountResponseDTO.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .accountLimit(account.getAccountLimit())
                .openingDate(account.getOpeningDate())
                .customerName(account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName()).build();
    }
    private TransactionResponseDTO toTransactionResponseDTO(Transaction transaction){
        return TransactionResponseDTO.builder()
                .accountNumber(transaction.getAccount().getAccountNumber())
                .receiverNumber(transaction.getReceiver() == null ? null : transaction.getReceiver().getAccountNumber())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .timestamp(transaction.getCreatedAt())
                .location(transaction.getLocation())
                .build();
    }

}

