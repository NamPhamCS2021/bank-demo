package com.example.bankDemo.dto.account;

import com.example.bankDemo.enums.AccountStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AccountUpdateStatusDTO {
    private AccountStatus status;
}
