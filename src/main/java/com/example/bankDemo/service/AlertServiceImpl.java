package com.example.bankDemo.service;


import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.alert.AlertDTO;
import com.example.bankDemo.dto.alert.AlertSearchDTO;
import com.example.bankDemo.dto.alert.AlertUserSearchDTO;
import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.Alert;
import com.example.bankDemo.entity.Transaction;
import com.example.bankDemo.enums.AlertType;
import com.example.bankDemo.enums.ReturnMessage;
import com.example.bankDemo.repository.AccountRepository;
import com.example.bankDemo.repository.AlertRepository;
import com.example.bankDemo.repository.TransactionRepository;
import com.example.bankDemo.specification.AlertSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final Executor virtualExecutor;




    @Override
    @Scheduled(cron = "0 0 0 * * *")
    public void detectAbnormalTransactions() {

        List<Transaction> uncheckedTransactions = transactionRepository.findByCheckedFalse();

        List<CompletableFuture<Boolean>> futures = uncheckedTransactions.stream()
                .map(transaction -> CompletableFuture.supplyAsync(() -> {
                    try {
                        boolean result = processTransaction(transaction);
                        transaction.setChecked(true);
                        return result;
                    } catch (Exception e) {
                        log.error("Error processing transaction with id " + transaction.getPublicId(), e);
                        return false;
                    } finally {
                        transactionRepository.save(transaction);
                        log.info("Transaction with id " + transaction.getPublicId() + " processed");
                        log.info("---------------------------------------");
                    }
                }, virtualExecutor)).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    //helper to process transactions
    private boolean processTransaction(Transaction transaction) {
        boolean notNormal = false;

        if(transaction.getAmount().compareTo(transaction.getAccount().getAccountLimit()) > 0){
            createAlert(transaction, "Transaction limit exceeded!",AlertType.LARGE_AMOUNT);
            notNormal = true;
        }


        LocalDateTime startTime = transaction.getCreatedAt().minusSeconds(30);

        LocalDateTime endTime = transaction.getCreatedAt().plusSeconds(30);

        List<Transaction> transactionsOfAccountBetweenTime = transactionRepository.findBetweenTimeByAccount(transaction.getAccount().getId(), startTime, endTime);
        if(transactionsOfAccountBetweenTime.size() > 3){
            for(Transaction t : transactionsOfAccountBetweenTime){
                createAlert(t, "Too many transactions in a short period!",AlertType.TOO_MANY_TRANSACTIONS);
            }

            notNormal = true;
        }
        return notNormal;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "Alert")
    @CacheEvict(value = "Alert", allEntries = true)
    public ApiResponse<Object> getAll(Pageable pageable) {
        try{
            Page<Alert> alerts = alertRepository.findAll(pageable);
            return new ApiResponse<>(alerts.map(this::toAlertDTO), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }

    }
    public ApiResponse<Object> getByTransactionId(UUID transactionPublicId, Pageable pageable) {
        try{
            Optional<Transaction> optionalTransaction = transactionRepository.findByPublicId(transactionPublicId);
            if(optionalTransaction.isEmpty()) {
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Page<Alert> alerts = alertRepository.findByTransactionPublicId(transactionPublicId, pageable);
            return new ApiResponse<>(alerts.map(this::toAlertDTO), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    public ApiResponse<Object> getByAccountNumber(String accountNumber, Pageable pageable) {
        try{
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
            if(optionalAccount.isEmpty()) {
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Page<Alert> alerts = alertRepository.findByAccountNumber(accountNumber, pageable);
            return new ApiResponse<>(alerts.map(this::toAlertDTO), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    private void createAlert(Transaction transaction, String description, AlertType type) {
        Alert alert = new Alert();
        alert.setTransaction(transaction);
        alert.setDescription(description);
        alert.setType(type);
        alertRepository.save(alert);
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> search(AlertSearchDTO alertSearchDTO, Pageable pageable) {
        try{

            if(alertSearchDTO == null) {
                return new ApiResponse<>(ReturnMessage.NULL_VALUE.getCode(), ReturnMessage.NULL_VALUE.getMessage());
            }

            if(alertSearchDTO.getStart() != null && alertSearchDTO.getEnd() != null && alertSearchDTO.getStart().isAfter(alertSearchDTO.getEnd()))
            {
                return new ApiResponse<>(ReturnMessage.INVALID_ARGUMENTS.getCode(), ReturnMessage.INVALID_ARGUMENTS.getMessage());
            }
            Optional<Transaction> optionalTransaction = transactionRepository.findByPublicId(alertSearchDTO.getTransactionPublicId());
            if(optionalTransaction.isEmpty())
            {
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Transaction transaction = optionalTransaction.get();
            Specification<Alert> spec = (root, query, builder) -> builder.conjunction(); // base

            spec = spec.and(AlertSpecification.hasTransaction(transaction.getId()));
            spec = spec.and(AlertSpecification.hasStatus(alertSearchDTO.getStatus()));
            spec = spec.and(AlertSpecification.hasType(alertSearchDTO.getType()));
            spec = spec.and(AlertSpecification.createdAfter(alertSearchDTO.getStart()));
            spec = spec.and(AlertSpecification.createdBefore(alertSearchDTO.getEnd()));
            Page<Alert> alertPage = alertRepository.findAll(spec, pageable);
            Page<AlertDTO> alertDTOPage = alertPage.map(this::toAlertDTO);
            return new ApiResponse<>(alertDTOPage, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> selfSearch(UUID publicTransactionId, AlertUserSearchDTO alertUserSearchDTO, Pageable pageable) {
        try{
            if(alertUserSearchDTO == null) {
                return new ApiResponse<>(ReturnMessage.NULL_VALUE.getCode(), ReturnMessage.NULL_VALUE.getMessage());
            }

            if(alertUserSearchDTO.getStart() != null && alertUserSearchDTO.getEnd() != null && alertUserSearchDTO.getStart().isAfter(alertUserSearchDTO.getEnd()))
            {
                return new ApiResponse<>(ReturnMessage.INVALID_ARGUMENTS.getCode(), ReturnMessage.INVALID_ARGUMENTS.getMessage());
            }
            Optional<Transaction> optionalTransaction = transactionRepository.findByPublicId(publicTransactionId);
            if(optionalTransaction.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Transaction transaction = optionalTransaction.get();
            Specification<Alert> spec = (root, query, builder) -> builder.conjunction(); // base

            spec = spec.and(AlertSpecification.hasTransaction(transaction.getId()));
            spec = spec.and(AlertSpecification.hasStatus(alertUserSearchDTO.getStatus()));
            spec = spec.and(AlertSpecification.hasType(alertUserSearchDTO.getType()));
            spec = spec.and(AlertSpecification.createdAfter(alertUserSearchDTO.getStart()));
            spec = spec.and(AlertSpecification.createdBefore(alertUserSearchDTO.getEnd()));
            Page<Alert> alertPage = alertRepository.findAll(spec, pageable);
            Page<AlertDTO> alertDTOPage = alertPage.map(this::toAlertDTO);
            return new ApiResponse<>(alertDTOPage, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    private AlertDTO toAlertDTO(Alert alert) {
        return AlertDTO.builder()
                .accountNumber(alert.getTransaction().getAccount().getAccountNumber())
                .transactionPublicId(alert.getTransaction().getPublicId())
                .description(alert.getDescription())
                .type(alert.getType())
                .status(alert.getStatus())
                .timestamp(alert.getTimestamp()).build();
    }
}
