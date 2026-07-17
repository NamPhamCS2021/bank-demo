package com.example.bankDemo.dto.account;

import com.example.bankDemo.enums.AccountStatus;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiverDTO {
    private String accountNumber;
    private AccountStatus status;
    private String firstName;
    private String lastName;

}
