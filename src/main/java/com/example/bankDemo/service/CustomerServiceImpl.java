package com.example.bankDemo.service;


import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.customer.*;

import com.example.bankDemo.entity.Customer;
import com.example.bankDemo.enums.CustomerType;
import com.example.bankDemo.enums.ReturnMessage;
import com.example.bankDemo.enums.UserRole;
import com.example.bankDemo.repository.CustomerRepository;
import com.example.bankDemo.security.entity.User;
import com.example.bankDemo.security.repository.UserRepository;
import com.example.bankDemo.specification.CustomerSpecification;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;


    private final UserRepository userRepository;


    private final PasswordEncoder passwordEncoder;


    @Override
    @CachePut(value = "customers")
    public ApiResponse<Object> createCustomer(CustomerCreateDTO customerCreateDTO){
        try{
            if(customerRepository.existsByPhoneNumberOrEmail(customerCreateDTO.getPhoneNumber(), customerCreateDTO.getEmail())){
                return new ApiResponse<>(ReturnMessage.ALREADY_EXISTED.getCode(), ReturnMessage.ALREADY_EXISTED.getMessage());
            }

            Customer customer = new Customer();
            customer.setFirstName(customerCreateDTO.getFirstName());
            customer.setLastName(customerCreateDTO.getLastName());
            customer.setEmail(customerCreateDTO.getEmail());
            customer.setType(customerCreateDTO.getType() == null ? CustomerType.PERSONAL : customerCreateDTO.getType());
            customer.setPhoneNumber(customerCreateDTO.getPhoneNumber());

            User user = new User();
            user.setCustomer(customer);
            user.setUsername(customerCreateDTO.getEmail());
            user.setRole(UserRole.USER);
            user.setPassword(passwordEncoder.encode("123456"));
            customer.setUser(user);
            userRepository.save(user);
            customerRepository.save(customer);

            return new ApiResponse<>(toCustomerResponse(customer), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @CachePut(value = "customers")
    public ApiResponse<Object> updateCustomer(UUID publicId, CustomerUpdateDTO customerUpdateDTO){
        try{
            Optional<Customer> optionalCustomer = customerRepository.findByPublicId(publicId);

            if(optionalCustomer.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }

            Customer existingCustomer = optionalCustomer.get();

            if(customerRepository.existsByPhoneNumberOrEmail(customerUpdateDTO.getPhoneNumber(), customerUpdateDTO.getEmail())){
                return new ApiResponse<>(ReturnMessage.ALREADY_EXISTED.getCode(), ReturnMessage.ALREADY_EXISTED.getMessage());
            }
            if(customerUpdateDTO.getEmail() != null){
                existingCustomer.setEmail(customerUpdateDTO.getEmail());
            }
            if(customerUpdateDTO.getPhoneNumber() != null){
                existingCustomer.setPhoneNumber(customerUpdateDTO.getPhoneNumber());
            }
            Customer updatedCustomer = customerRepository.save(existingCustomer);
//        later
            return new ApiResponse<>(toCustomerResponse(updatedCustomer), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }

    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "customers")
    public ApiResponse<Object> getAll(Pageable pageable){
        try {
            Page<Customer> customerPage = customerRepository.findAll(pageable);
            return new ApiResponse<>(customerPage.map(this::toCustomerResponse), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "customers", key = "#publicId")
    public ApiResponse<Object> getCustomerByPublicId(UUID publicId){
        try{
            Optional<Customer> optionalCustomer = customerRepository.findByPublicId(publicId);
            if(optionalCustomer.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Customer customer = optionalCustomer.get();
            return new ApiResponse<>(toCustomerResponse(customer), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    public ApiResponse<Object> search(CustomerSearchDTO dto, Pageable pageable) {
        try{

            if(dto == null) {
                return new ApiResponse<>(ReturnMessage.NULL_VALUE.getCode(), ReturnMessage.NULL_VALUE.getMessage());
            }

            if(dto.getFrom().isAfter(dto.getTo())){
                return new ApiResponse<>(ReturnMessage.INVALID_ARGUMENTS.getCode(), ReturnMessage.INVALID_ARGUMENTS.getMessage());
            }
            Specification<Customer> spec = (root, query, builder) -> builder.conjunction(); // base

            spec = spec.and(CustomerSpecification.hasFirstName(dto.getFirstName()));
            spec = spec.and(CustomerSpecification.hasLastName(dto.getLastName()));
            spec = spec.and(CustomerSpecification.hasEmail(dto.getEmail()));
            spec = spec.and(CustomerSpecification.hasPhoneNumber(dto.getPhone()));
            spec = spec.and(CustomerSpecification.hasType(dto.getType()));
            spec = spec.and(CustomerSpecification.createdBefore(dto.getTo()));
            spec = spec.and(CustomerSpecification.createdAfter(dto.getFrom()));
            Page<Customer> customerPage = customerRepository.findAll(spec, pageable);
            Page<CustomerResponseDTO> customerResponseDTOPage = customerPage.map(this::toCustomerResponse);
            return new ApiResponse<>(customerResponseDTOPage, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    public ApiResponse<Object> getCustomerByEmail(String email) {
        Optional<Customer> optionalCustomer= customerRepository.findByEmail(email);
        if(optionalCustomer.isEmpty()){
            return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
        }
        Customer customer = optionalCustomer.get();
        return new ApiResponse<>(toCustomerResponse(customer), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
    }

    //helper
    private CustomerResponseDTO toCustomerResponse(Customer customer){
        return CustomerResponseDTO.builder()
                .publicId(customer.getPublicId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .type(customer.getType())
                .createDate(customer.getCreatedDate()).build();

    }
//    private CustomerSummaryDTO toCustomerSummaryDTO(Customer customer){
//        return CustomerSummaryDTO.builder().firstName(customer.getFirstName())
//                .lastName(customer.getLastName())
//                .email(customer.getEmail()).phoneNumber(customer.getPhoneNumber()).build();
//    }
}
