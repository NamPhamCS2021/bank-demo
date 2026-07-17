package com.example.bankDemo.specification;

import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.Alert;
import com.example.bankDemo.enums.AlertStatus;
import com.example.bankDemo.enums.AlertType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class AlertSpecification {

    public static Specification<Alert> hasTransaction(Long transactionId) {
        return ((root, query, builder) ->
                builder.equal(root.get("transaction"), transactionId));
    }

    public static Specification<Account> hasPublicId(UUID publicId) {
        return(root, query, builder) ->
                publicId == null ? builder.conjunction() : builder.equal(root.get("publicId"), publicId);
    }

    public static Specification<Alert> hasType(AlertType type) {
        return((root, query, builder) ->
                builder.equal(root.get("type"), type));
    }

    public static Specification<Alert> hasStatus(AlertStatus status) {
        return((root, query, builder) ->
                builder.equal(root.get("status"), status));
    }

    public static Specification<Alert> createdBefore(LocalDateTime dateTime) {
        return((root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("created"), dateTime));
    }

    public static Specification<Alert> createdAfter(LocalDateTime dateTime) {
        return((root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("created"), dateTime));
    }


}
