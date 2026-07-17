package com.example.bankDemo.entity;

import com.example.bankDemo.enums.AlertStatus;
import com.example.bankDemo.enums.AlertType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alert")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private AlertType type;

    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false)
    private LocalDateTime timestamp;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @PrePersist
    public void initialiseAlert(){
        this.timestamp = LocalDateTime.now();
        this.status = AlertStatus.NEW;
        this.publicId = UUID.randomUUID();
    }
}
