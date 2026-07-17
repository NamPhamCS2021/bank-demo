package com.example.bankDemo.specification;

import com.example.bankDemo.entity.Account;
import com.example.bankDemo.enums.AccountStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountSpecification {

    public static Specification<Account> hasStatus(AccountStatus status) {
        return (root, query, builder) ->
                status == null ? builder.conjunction() : builder.equal(root.get("status"), status);
    }

    public static Specification<Account> hasCustomer(Long id) {
        return (root, query, builder) ->
                id == null ? builder.conjunction() : builder.equal(root.get("customer").get("id"), id);
    }

    public static Specification<Account> hasPublicId(UUID publicId) {
        return(root, query, builder) ->
                publicId == null ? builder.conjunction() : builder.equal(root.get("publicId"), publicId);
    }

    public static Specification<Account> hasAccountNumber(String accountNumber) {
        return (root, query, builder) ->
                accountNumber == null ? builder.conjunction() : builder.equal(root.get("accountNumber"), accountNumber);
    }

    public static Specification<Account> hasMaxBalance(BigDecimal balance) {
        return (root, query, builder) ->
                balance == null ? builder.conjunction() : builder.lessThanOrEqualTo(root.get("balance"), balance);
    }

    public static Specification<Account> hasMinBalance(BigDecimal balance) {
        return (root, query, builder) ->
                balance == null ? builder.conjunction() : builder.greaterThanOrEqualTo(root.get("balance"), balance);
    }

    public static Specification<Account> hasMinLimit(BigDecimal limit) {
        return (root, query, builder) ->
                limit == null ? builder.conjunction() : builder.greaterThanOrEqualTo(root.get("accountLimit"), limit);
    }
    public static Specification<Account> hasMaxLimit(BigDecimal limit) {
        return (root, query, builder) ->
                limit == null ? builder.conjunction() : builder.lessThanOrEqualTo(root.get("accountLimit"), limit);
    }

    public static Specification<Account> createdAfter(java.time.LocalDateTime dateTime) {
        return (root, query, builder) ->
                dateTime == null ? builder.conjunction() : builder.greaterThanOrEqualTo(root.get("openingDate"), dateTime);
    }
    public static Specification<Account> createdBefore(java.time.LocalDateTime dateTime) {
        return (root, query, builder) ->
                dateTime == null ? builder.conjunction() : builder.lessThanOrEqualTo(root.get("openingDate"), dateTime);
    }
}
