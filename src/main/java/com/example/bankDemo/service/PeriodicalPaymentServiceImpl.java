package com.example.bankDemo.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.periodicallypayment.PeriodicalPaymentSearchDTO;
import com.example.bankDemo.dto.periodicallypayment.PeriodicalPaymentUserSearchDTO;
import com.example.bankDemo.dto.periodicallypayment.PeriodicallyPaymentDTO;
import com.example.bankDemo.dto.periodicallypayment.PeriodicallyPaymentUpdateDTO;
import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.PeriodicalPayment;
import com.example.bankDemo.enums.ReturnMessage;
import com.example.bankDemo.repository.AccountRepository;
import com.example.bankDemo.repository.PeriodicalPaymentRepository;
import com.example.bankDemo.specification.PeriodicalPaymentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class PeriodicalPaymentServiceImpl implements PeriodicalPaymentService {

    private final PeriodicalPaymentRepository periodicalPaymentRepository;

    private final AccountRepository accountRepository;



    @Override
    @CachePut(value = "payment", key = "periodicallyPaymentDTO.accountNumber")
    public ApiResponse<Object> createPeriodicalPayment(PeriodicallyPaymentDTO periodicallyPaymentDTO) {
        try{
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(periodicallyPaymentDTO.getAccountNumber());
            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Account account = optionalAccount.get();
            PeriodicalPayment periodicalPayment = new PeriodicalPayment();
            periodicalPayment.setAccount(account);
            periodicalPayment.setDescription(periodicallyPaymentDTO.getDescription());
            periodicalPayment.setAmount(periodicallyPaymentDTO.getAmount());
            periodicalPayment.setPeriod(periodicallyPaymentDTO.getPeriod());
            periodicalPaymentRepository.save(periodicalPayment);

            return new ApiResponse<>(toPeriodicallyPaymentDTO(periodicalPayment), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }

    }

    @Override
    @CachePut(value = "payment", key = "#publicId")
    public ApiResponse<Object> updatePeriodicalPayment(UUID publicId, PeriodicallyPaymentUpdateDTO periodicallyPaymentUpdateDTO){
        try{
            Optional<PeriodicalPayment> optionalPeriodicallyPayment = periodicalPaymentRepository.findByPublicId(publicId);
            if(optionalPeriodicallyPayment.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            PeriodicalPayment periodicalPayment = optionalPeriodicallyPayment.get();

            periodicalPayment.setAmount(periodicallyPaymentUpdateDTO.getAmount());
            periodicalPayment.setStatus(periodicallyPaymentUpdateDTO.getStatus());
            periodicalPaymentRepository.save(periodicalPayment);
            return new ApiResponse<>(toPeriodicallyPaymentDTO(periodicalPayment), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }



    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "payment", key = "#publicId")
    public ApiResponse<Object> getPeriodicalPaymentById(UUID publicId) {
        try {
            Optional<PeriodicalPayment> optionalPeriodicallyPayment = periodicalPaymentRepository.findByPublicId(publicId);
            if(optionalPeriodicallyPayment.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            PeriodicalPayment periodicalPayment = optionalPeriodicallyPayment.get();
            return new ApiResponse<>(toPeriodicallyPaymentDTO(periodicalPayment), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }

    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> getPeriodicalPaymentByAccountNumber(String accountNumber, Pageable pageable){
        try{
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Page<PeriodicalPayment> periodicallyPayment = periodicalPaymentRepository.findByAccountNumber(accountNumber, pageable);
            return new ApiResponse<>(periodicallyPayment.map(this::toPeriodicallyPaymentDTO), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> search(PeriodicalPaymentSearchDTO dto, Pageable pageable) {
        try{

            if(dto == null) {
                return new ApiResponse<>(ReturnMessage.NULL_VALUE.getCode(), ReturnMessage.NULL_VALUE.getMessage());
            }

            if((dto.getStartedAfter() != null && dto.getStartedBefore() != null && dto.getStartedBefore().isAfter(dto.getStartedAfter()))
            || (dto.getEndedAfter() != null && dto.getEndedBefore() != null && dto.getEndedBefore().isAfter(dto.getEndedAfter()))
            || (dto.getMinAmount() != null && dto.getMaxAmount() != null && dto.getMinAmount().compareTo(dto.getMaxAmount()) > 0)){
                return new ApiResponse<>(ReturnMessage.INVALID_ARGUMENTS.getCode(), ReturnMessage.INVALID_ARGUMENTS.getMessage());
            }
            Specification<PeriodicalPayment> spec = (root, query, builder) -> builder.conjunction(); // base

            spec = spec.and(PeriodicalPaymentSpecification.hasAccount(dto.getAccountId()));
            spec = spec.and(PeriodicalPaymentSpecification.hasPeriod(dto.getPeriod()));
            spec = spec.and(PeriodicalPaymentSpecification.hasStatus(dto.getStatus()));
            spec = spec.and(PeriodicalPaymentSpecification.hasMinAmount(dto.getMinAmount()));
            spec = spec.and(PeriodicalPaymentSpecification.hasMaxAmount(dto.getMaxAmount()));
            spec = spec.and(PeriodicalPaymentSpecification.startBefore(dto.getStartedBefore()));
            spec = spec.and(PeriodicalPaymentSpecification.startAfter(dto.getStartedAfter()));
            spec = spec.and(PeriodicalPaymentSpecification.endBefore(dto.getEndedBefore()));
            spec = spec.and(PeriodicalPaymentSpecification.endAfter(dto.getEndedAfter()));
            return new ApiResponse<>(periodicalPaymentRepository.findAll(spec, pageable).map(this::toPeriodicallyPaymentDTO), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> selfSearch(String accountNumber, PeriodicalPaymentUserSearchDTO dto, Pageable pageable) {
        try{
            if(dto == null) {
                return new ApiResponse<>(ReturnMessage.NULL_VALUE.getCode(), ReturnMessage.NULL_VALUE.getMessage());
            }

            if((dto.getStartedAfter() != null && dto.getStartedBefore() != null && dto.getStartedBefore().isAfter(dto.getStartedAfter()))
                    || (dto.getEndedAfter() != null && dto.getEndedBefore() != null && dto.getEndedAfter().isAfter(dto.getEndedAfter()))
                    || (dto.getMinAmount() != null && dto.getMaxAmount() != null && dto.getMinAmount().compareTo(dto.getMaxAmount()) > 0)){
                return new ApiResponse<>(ReturnMessage.INVALID_ARGUMENTS.getCode(), ReturnMessage.INVALID_ARGUMENTS.getMessage());
            }

            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Account account = optionalAccount.get();
            Specification<PeriodicalPayment> spec = (root, query, builder) -> builder.conjunction(); // base

            spec = spec.and(PeriodicalPaymentSpecification.hasAccount(account.getId()));
            spec = spec.and(PeriodicalPaymentSpecification.hasPeriod(dto.getPeriod()));
            spec = spec.and(PeriodicalPaymentSpecification.hasStatus(dto.getStatus()));
            spec = spec.and(PeriodicalPaymentSpecification.hasMinAmount(dto.getMinAmount()));
            spec = spec.and(PeriodicalPaymentSpecification.hasMaxAmount(dto.getMaxAmount()));
            spec = spec.and(PeriodicalPaymentSpecification.startBefore(dto.getStartedBefore()));
            spec = spec.and(PeriodicalPaymentSpecification.startAfter(dto.getStartedAfter()));
            spec = spec.and(PeriodicalPaymentSpecification.endBefore(dto.getEndedBefore()));
            spec = spec.and(PeriodicalPaymentSpecification.endAfter(dto.getEndedAfter()));
            return new ApiResponse<>(periodicalPaymentRepository.findAll(spec, pageable).map(this::toPeriodicallyPaymentDTO), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *") //every day at midnight
    public void processingPayment() {
        List<PeriodicalPayment> payments = periodicalPaymentRepository.findAll();

        for(PeriodicalPayment payment : payments){
            if(payment.getEndedAt() == null && payment.getEndedAt().isBefore(LocalDateTime.now())){

                updateEndedAt(payment);
                Account account = payment.getAccount();
                account.setBalance(account.getBalance().subtract(payment.getAmount()));
                accountRepository.save(account);
                periodicalPaymentRepository.save(payment);
            }
        }
    }
    //helper

    private PeriodicallyPaymentDTO toPeriodicallyPaymentDTO(PeriodicalPayment periodicalPayment) {
        return PeriodicallyPaymentDTO.builder()
                .accountNumber(periodicalPayment.getAccount().getAccountNumber())
                .description(periodicalPayment.getDescription())
                .amount(periodicalPayment.getAmount())
                .period(periodicalPayment.getPeriod())
                .startedAt(periodicalPayment.getStartedAt())
                .build();
    }
    private void updateEndedAt(PeriodicalPayment periodicalPayment) {
        switch(periodicalPayment.getPeriod()) {
            case WEEKLY -> periodicalPayment.setEndedAt(LocalDateTime.now().plusWeeks(1));
            case MONTHLY -> periodicalPayment.setEndedAt(LocalDateTime.now().plusMonths(1));
            case YEARLY -> periodicalPayment.setEndedAt(LocalDateTime.now().plusYears(1));
            default -> throw new RuntimeException("Unexpected value: " + periodicalPayment.getPeriod());
        }
    }
}
