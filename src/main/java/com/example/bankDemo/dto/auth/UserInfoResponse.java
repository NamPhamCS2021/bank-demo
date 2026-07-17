package com.example.bankDemo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private UUID customerPublicId;
}
