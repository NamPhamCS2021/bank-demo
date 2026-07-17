package com.example.bankDemo.security.model;

import com.example.bankDemo.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponse {
    private String username;
    private UserRole role;
}
