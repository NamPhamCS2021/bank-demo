package com.example.bankDemo.entity;

import com.example.bankDemo.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account_status_history")
public class AccountStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @Column(nullable = false)
    private AccountStatus status;
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void initialiseAccountStatusHistory() {
        this.timestamp = LocalDateTime.now();
        this.publicId = UUID.randomUUID();
    }

}
