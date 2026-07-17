package com.example.bankDemo.entity;


import com.example.bankDemo.enums.CustomerType;
import com.example.bankDemo.security.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    @Email
    private String email;

    @Column(nullable = false, unique = true)
    @Pattern(regexp = "^[0-9]+$")
    private String phoneNumber;

    @Column(nullable = false)
    private CustomerType type;

    @CreatedDate
    @Column(name = "created_date",nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "customer", cascade =CascadeType.ALL, fetch =FetchType.LAZY)
    private List<Account> accounts;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void initialiseCustomer() {
        this.createdDate = LocalDateTime.now();
        this.publicId = UUID.randomUUID();
    }
}
