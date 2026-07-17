package com.example.bankDemo.repository;

import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {
    @Query("SELECT COUNT(a) > 0 FROM Alert a WHERE a.transaction.account.id = :accountId")
    Boolean existsByAccountId(@Param("accountId") Long accountId);
    @Query("SELECT a from Alert a WHERE a.publicId = :publicId")
    Optional<Alert> findByPublicId(@Param("publicId") UUID publicId);
    Page<Alert> findByTransactionPublicId(UUID transactionPublicId, Pageable pageable);
    @Query("SELECT a FROM Alert a WHERE a.transaction.account.id = :accountId OR a.transaction.receiver.id = :accountId")
    Page<Alert> findByAccountNumber(@Param("accountNumber")String accountNumber, Pageable pageable);
    @Query("SELECT a.transaction.account FROM Alert a GROUP BY a.transaction.account.id ORDER BY Count(a) DESC")
    Account getMostAlertAccount();
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.transaction.account.id = :accountId")
    Long countAllAlertsByAccountId(Long accountId);
}
