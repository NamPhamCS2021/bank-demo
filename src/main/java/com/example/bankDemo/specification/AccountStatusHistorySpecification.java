package com.example.bankDemo.specification;

import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.AccountStatusHistory;
import com.example.bankDemo.enums.AccountStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class AccountStatusHistorySpecification {

    public static Specification<AccountStatusHistory> hasAccount(Long id) {
        return ((root, query, builder) ->
                builder.equal(root.get("account"), id));
    }

    public static Specification<Account> hasPublicId(UUID publicId) {
        return(root, query, builder) ->
                publicId == null ? builder.conjunction() : builder.equal(root.get("publicId"), publicId);
    }

    public static Specification<AccountStatusHistory> hasStatus(AccountStatus status) {
        return ((root, query, builder) ->
                builder.equal(root.get("accountStatusHistory"), status));
    }

    public static Specification<AccountStatusHistory> createdBefore(LocalDateTime timestamp) {
        return ((root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("createdAt"), timestamp));
    }

    public static Specification<AccountStatusHistory> createdAfter(LocalDateTime timestamp) {
        return ((root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("createdAt"), timestamp));
    }
}
