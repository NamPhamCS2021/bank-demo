package com.example.bankDemo.specification;

import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.PeriodicalPayment;
import com.example.bankDemo.enums.Period;
import com.example.bankDemo.enums.SubscriptionStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PeriodicalPaymentSpecification {

    public static Specification<Account> hasPublicId(UUID publicId) {
        return(root, query, builder) ->
                publicId == null ? builder.conjunction() : builder.equal(root.get("publicId"), publicId);
    }
    public static Specification<PeriodicalPayment> hasAccount(Long accountId) {
        return ((root, query, builder) ->
                builder.equal(root.get("account"), accountId));
    }

    public static Specification<PeriodicalPayment> hasMaxAmount(BigDecimal amount) {
        return ((root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("maxAmount"), amount));
    }

    public static Specification<PeriodicalPayment> hasMinAmount(BigDecimal amount) {
        return ((root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("minAmount"), amount));
    }

    public static Specification<PeriodicalPayment> hasPeriod(Period period) {
        return ((root, query, builder) ->
                builder.equal(root.get("period"), period));
    }

    public static Specification<PeriodicalPayment> hasStatus (SubscriptionStatus status) {
        return ((root, query, builder) ->
                builder.equal(root.get("status"), status));
    }

    public static Specification<PeriodicalPayment> startBefore(LocalDateTime timestamp) {
        return ((root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("startedAt"), timestamp));
    }

    public static Specification<PeriodicalPayment> startAfter(LocalDateTime timestamp) {
        return((root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("startedAt"), timestamp));
    }

    public static Specification<PeriodicalPayment> endBefore(LocalDateTime timestamp) {
        return ((root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("endedAt"), timestamp));
    }
    public static Specification<PeriodicalPayment> endAfter(LocalDateTime timestamp) {
        return ((root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("endedAt"), timestamp));
    }
}
