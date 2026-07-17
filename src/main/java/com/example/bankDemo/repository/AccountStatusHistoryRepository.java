package com.example.bankDemo.repository;

import com.example.bankDemo.entity.AccountStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountStatusHistoryRepository extends JpaRepository<AccountStatusHistory, Long>, JpaSpecificationExecutor<AccountStatusHistory> {
    boolean existsByAccountId(Long accountId);
    @Query("SELECT a FROM AccountStatusHistory a WHERE a.publicId = :publicId")
    Optional<AccountStatusHistory> findByPublicId(UUID publicId);
    @Query("SELECT a FROM AccountStatusHistory a WHERE a.account.accountNumber = :accountNumber")
    Page<AccountStatusHistory> findByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

    @Query("SELECT a FROM AccountStatusHistory a WHERE (a.account.accountNumber = :accountNumber ) AND (a.timestamp BETWEEN :start AND :end)")
    Page<AccountStatusHistory> findBetweenByAccountNumber(@Param("accountNumber") String accountNumber, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}
