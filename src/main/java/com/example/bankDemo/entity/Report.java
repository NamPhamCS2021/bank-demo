package com.example.bankDemo.entity;

import com.example.bankDemo.enums.ReportPeriod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "periodical_report")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportPeriod period;

    @Column
    private Long numberOfTransactions;

    @Column
    private BigDecimal totalAmount;

    @Column
    private BigDecimal averageAmount;

    @Column
    private BigDecimal maximumAmount;

    @Column
    private BigDecimal minimumAmount;

    @Column
    private LocalDateTime timestamp;

    @Column
    private LocalDateTime startAt;

   @Column
   private LocalDateTime endAt;

   @PrePersist
   private void initialisePeriodicalReport() {
        this.timestamp = LocalDateTime.now();
        this.publicId = UUID.randomUUID();

   }
}
