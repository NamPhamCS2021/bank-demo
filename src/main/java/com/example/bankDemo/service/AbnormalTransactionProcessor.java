package com.example.bankDemo.service;

import com.example.bankDemo.entity.Alert;
import com.example.bankDemo.entity.Transaction;
import com.example.bankDemo.enums.AlertType;
import com.example.bankDemo.repository.AlertRepository;
import com.example.bankDemo.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class AbnormalTransactionProcessor {

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAndMarkChecked(Long transactionId) {
        try {
            Transaction transaction = transactionRepository.findByIdWithAccount(transactionId)
                    .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));

            evaluate(transaction);
            transaction.setChecked(true);
        } catch (Exception e) {
            log.error("Error processing transaction with id {}", transactionId, e);
        }
    }

    private void evaluate(Transaction transaction) {
        if (transaction.getAmount().compareTo(transaction.getAccount().getAccountLimit()) > 0) {
            createAlert(transaction, "Transaction limit exceeded!", AlertType.LARGE_AMOUNT);
        }

        LocalDateTime startTime = transaction.getCreatedAt().minusSeconds(30);
        LocalDateTime endTime = transaction.getCreatedAt().plusSeconds(30);

        List<Transaction> nearbyTransactions = transactionRepository.findBetweenTimeByAccount(
                transaction.getAccount().getId(), startTime, endTime);

        // Chỉ tạo MỘT alert cho giao dịch đang xét, không lặp qua toàn bộ cụm giao
        // dịch trong cửa sổ 30s (tránh N giao dịch liền nhau tạo ra N² alert trùng lặp).
        if (nearbyTransactions.size() > 3) {
            createAlert(transaction, "Too many transactions in a short period!", AlertType.TOO_MANY_TRANSACTIONS);
        }
    }

    private void createAlert(Transaction transaction, String description, AlertType type) {
        Alert alert = new Alert();
        alert.setTransaction(transaction);
        alert.setDescription(description);
        alert.setType(type);
        alertRepository.save(alert);
    }
}