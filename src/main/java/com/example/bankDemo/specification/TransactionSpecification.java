package com.example.bankDemo.specification;

import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.Transaction;
import com.example.bankDemo.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionSpecification {

    public static Specification<Account> hasPublicId(UUID publicId) {
        return(root, query, builder) ->
                publicId == null ? builder.conjunction() : builder.equal(root.get("publicId"), publicId);
    }
    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, builder) ->
                type == null ? null : builder.equal(root.get("type"), type);
    }

    public static Specification<Transaction> hasAccountId(Long id) {
        return (root, query, builder) ->
                id == null ? null : builder.equal(root.get("account").get("id"), id);
    }

    public static Specification<Transaction> hasReceiverId(Long id) {
        return (root, query, builder) ->
                id == null ? null : builder.equal(root.get("receiver").get("id"), id);
    }

    public static Specification<Transaction> hasMinAmount(BigDecimal amount) {
        return (root, query, builder) ->
                amount == null ? null : builder.greaterThanOrEqualTo(root.get("amount"), amount);
    }

    public static Specification<Transaction> hasMaxAmount(BigDecimal amount) {
        return (root, query, builder) ->
                amount == null ? null : builder.lessThanOrEqualTo(root.get("amount"), amount);
    }

    public static Specification<Transaction> occurredAfter(LocalDateTime from) {
        return (root, query, builder) ->
                from == null ? null : builder.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Transaction> occurredBefore(LocalDateTime to) {
        return (root, query, builder) ->
                to == null ? null : builder.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<Transaction> hasChecked(Boolean checked) {
        return (root, query, builder) ->
                checked == null ? null : builder.equal(root.get("checked"), checked);
    }

    public static Specification<Transaction> hasLocation(String location) {
        return (root, query, builder) ->
                location == null ? null : builder.like(builder.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }
}
