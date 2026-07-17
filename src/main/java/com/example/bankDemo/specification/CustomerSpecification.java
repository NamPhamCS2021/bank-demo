package com.example.bankDemo.specification;

import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.Customer;
import com.example.bankDemo.enums.CustomerType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CustomerSpecification {

    public static Specification<Account> hasPublicId(UUID publicId) {
        return(root, query, builder) ->
                publicId == null ? builder.conjunction() : builder.equal(root.get("publicId"), publicId);
    }
    public static Specification<Customer> hasFirstName(String firstName) {
        return (root, query, builder) ->
                firstName == null ? null : builder.like(builder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%");
    }

    public static Specification<Customer> hasLastName(String lastName) {
        return (root, query, builder) ->
                lastName == null ? null : builder.like(builder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
    }

    public static Specification<Customer> hasEmail(String email) {
        return (root, query, builder) ->
                email == null ? null : builder.equal(builder.lower(root.get("email")), email.toLowerCase());
    }

    public static Specification<Customer> hasPhoneNumber(String phoneNumber) {
        return (root, query, builder) ->
                phoneNumber == null ? null : builder.equal(root.get("phoneNumber"), phoneNumber);
    }

    public static Specification<Customer> hasType(CustomerType type) {
        return (root, query, builder) ->
                type == null ? null : builder.equal(root.get("type"), type);
    }

    public static Specification<Customer> createdAfter(java.time.LocalDateTime dateTime) {
        return (root, query, builder) ->
                dateTime == null ? null : builder.greaterThanOrEqualTo(root.get("createdDate"), dateTime);
    }

    public static Specification<Customer> createdBefore(java.time.LocalDateTime dateTime) {
        return (root, query, builder) ->
                dateTime == null ? null : builder.lessThanOrEqualTo(root.get("createdDate"), dateTime);
    }
}
