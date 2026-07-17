package com.example.bankDemo.security.model;

import com.example.bankDemo.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String username;
    private UserRole role;
    private LocalDateTime createdDate;
}
