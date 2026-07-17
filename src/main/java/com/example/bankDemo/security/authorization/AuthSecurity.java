package com.example.bankDemo.security.authorization;

import com.example.bankDemo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("authSecurity")
@RequiredArgsConstructor
public class AuthSecurity {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthSecurity.class);

    private final CustomerRepository customerRepository;

    private final AccountRepository accountRepository;

    private final PeriodicalPaymentRepository periodicalPaymentRepository;

    private final TransactionRepository transactionRepository;

    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    private final AlertRepository alertRepository;

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    private String getCurrentEmail(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public boolean isSelfCustomer(UUID customerPublicId) {
        if(isAdmin()) {
            return true;
        }
        return customerRepository.findByPublicId(customerPublicId)
                .map(c -> c.getEmail().equals(getCurrentEmail()))
                .orElse(false);
    }

    public boolean isOwnerOfAccount(Long accountId) {
        if(isAdmin()) {
            return true;
        }
        return accountRepository.findById(accountId)
                .map(a -> a.getCustomer().getEmail().equals(getCurrentEmail()))
                .orElse(false);
    }
    public boolean isOwnerOfAccountByAccountNumber(String accountNumber) {
        if (isAdmin()) {
            return true;
        }
        String currentEmail = getCurrentEmail();

        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> {
                    String ownerEmail = account.getCustomer().getEmail();

                    // 🔍 Log both values for debugging
                    log.debug("Validating account ownership: accountNumber={}, ownerEmail={}, currentEmail={}",
                            accountNumber, ownerEmail, currentEmail);

                    return ownerEmail != null && currentEmail != null &&
                            ownerEmail.trim().equalsIgnoreCase(currentEmail.trim());
                })
                .orElseGet(() -> {
                    log.debug("No account found with accountNumber={}", accountNumber);
                    return false;
                });
    }



    public boolean isOwnerOfPayment(UUID paymentPublicId) {
        if(isAdmin()) {
            return true;
        }
        return periodicalPaymentRepository.findByPublicId(paymentPublicId)
                .map(p -> p.getAccount().getCustomer().getEmail().equals(getCurrentEmail()))
                .orElse(false);
    }

    public boolean isOwnerOfTransaction(UUID transactionPublicId) {
        if(isAdmin()) {
            return true;
        }
        return transactionRepository.findByPublicId(transactionPublicId)
                .map(t -> {
                    String currentEmail = getCurrentEmail();
                    String senderMail = t.getAccount().getCustomer().getEmail();
                    String rerceiverMail = t.getReceiver() != null ? t.getReceiver().getCustomer().getEmail() : null;
                    return currentEmail.equals(senderMail) || currentEmail.equals(rerceiverMail);
                })
                .orElse(false);
    }
    public boolean isOwnerOfAccountStatusHistory(UUID publicId) {
        if(isAdmin()) {
            return true;
        }
        return accountStatusHistoryRepository.findByPublicId(publicId)
                .map(a -> a.getAccount().getCustomer().getEmail().equals(getCurrentEmail()))
                .orElse(false);
    }

    public boolean isOwnerOfAlert(UUID publicId) {
        if(isAdmin()) {
            return true;
        }
        return alertRepository.findByPublicId(publicId)
                .map(alert -> alert.getTransaction().getAccount().getCustomer().getEmail().equals(getCurrentEmail()))
                .orElse(false);
    }
}
