package com.example.bankDemo.entity;

import com.example.bankDemo.enums.AccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "account")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 10)
    @Pattern(regexp = "^[A-Z0-9]{10}$", message = "Account number must be 10 characters of uppercase letters and numbers")
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(nullable = false)
    private BigDecimal accountLimit;

    @Column(nullable = false)
    private LocalDateTime openingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccountStatusHistory> accountStatusHistory;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PeriodicalPayment> periodicalPayments;

    @PrePersist
    public void initialiseAccount() {
        if (this.accountNumber == null) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String random = String.format("%04d", (int) (Math.random() * 10000));
            this.accountNumber = timestamp.substring(timestamp.length() - 6) + random;
        }
        this.openingDate = LocalDateTime.now();
    }

}
