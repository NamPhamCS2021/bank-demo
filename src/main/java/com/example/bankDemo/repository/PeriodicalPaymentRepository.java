package com.example.bankDemo.repository;

import com.example.bankDemo.entity.PeriodicalPayment;
import com.example.bankDemo.enums.SubscriptionStatus;
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
public interface PeriodicalPaymentRepository extends JpaRepository<PeriodicalPayment, Long>, JpaSpecificationExecutor<PeriodicalPayment> {
    @Query("SELECT p from PeriodicalPayment p WHERE p.account.accountNumber = :accountNumber")
    Page<PeriodicalPayment> findByAccountNumber(@Param("accountId") String accountNumber, Pageable pageable);
    @Query("SELECT p from PeriodicalPayment p WHERE p.publicId = :publicId")
    Optional<PeriodicalPayment> findByPublicId(@Param("publicId") UUID publicId);
    @Query("SELECT p from PeriodicalPayment p WHERE p.account.id = :accountId AND p.status = :status")
    Page<PeriodicalPayment> findByAccountIdAndStatus(@Param("accountId") Long accountId, @Param("status") SubscriptionStatus status, Pageable pageable);
}
