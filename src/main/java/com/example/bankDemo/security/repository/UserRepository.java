package com.example.bankDemo.security.repository;

import com.example.bankDemo.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(String username);
    @Query("SELECT u.customer FROM User u WHERE u.username = :username")
    Optional<User> findCustomerByUsername(String username);
    Boolean existsByUsername(String username);
}
