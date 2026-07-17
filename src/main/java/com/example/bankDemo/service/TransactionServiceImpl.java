package com.example.bankDemo.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.transaction.*;
import com.example.bankDemo.enums.ReturnMessage;
import com.example.bankDemo.projections.LocationCount;
import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.Transaction;
import com.example.bankDemo.enums.AccountStatus;
import com.example.bankDemo.enums.TransactionType;
import com.example.bankDemo.repository.AccountRepository;
import com.example.bankDemo.repository.CustomerRepository;
import com.example.bankDemo.repository.TransactionRepository;
import com.example.bankDemo.security.authorization.AuthSecurity;
import com.example.bankDemo.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {


    private final TransactionRepository transactionRepository;

    private final AccountRepository accountRepository;

    private final AuthSecurity authSecurity;

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    @CachePut(value = "transactionsByAccount", key = "#transactionCreateDTO.accountNumber")
    @CacheEvict(value = "accounts", key ="#transactionCreateDTO.accountNumber")
    public ApiResponse<Object> deposit(TransactionCreateDTO transactionCreateDTO) {
        try{
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(transactionCreateDTO.getAccountNumber());

            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }

            Account account = optionalAccount.get();
            if(account.getStatus() != AccountStatus.ACTIVE){
                return new ApiResponse<>(ReturnMessage.INACTIVE.getCode(), ReturnMessage.INACTIVE.getMessage());
            }

            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setAmount(transactionCreateDTO.getAmount());
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setLocation(transactionCreateDTO.getLocation());

            account.setBalance(account.getBalance().add(transactionCreateDTO.getAmount()));
            transactionRepository.save(transaction);
            return new ApiResponse<>(toTransactionResponseDTO(transaction), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch(Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional
    @CachePut(value = "transactionsByAccount", key = "#transactionCreateDTO.accountNumber")
    @CacheEvict(value = "accounts", key ="#transactionCreateDTO.accountNumber")
    public ApiResponse<Object> withdraw(TransactionCreateDTO transactionCreateDTO) {
        try{
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(transactionCreateDTO.getAccountNumber());

            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }

            Account account = optionalAccount.get();

            if(account.getStatus() != AccountStatus.ACTIVE){
                return new ApiResponse<>(ReturnMessage.INACTIVE.getCode(), ReturnMessage.INACTIVE.getMessage());
            }

            if(account.getBalance().compareTo(transactionCreateDTO.getAmount()) < 0){
                return new ApiResponse<>(ReturnMessage.INSUFFICIENT_BALANCE.getCode(), ReturnMessage.INSUFFICIENT_BALANCE.getMessage());
            }
            if(account.getAccountLimit().compareTo(transactionCreateDTO.getAmount()) < 0){
                return new ApiResponse<>(ReturnMessage.OFF_LIMIT.getCode(), ReturnMessage.OFF_LIMIT.getMessage());
            }

            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setAmount(transactionCreateDTO.getAmount());
            transaction.setType(TransactionType.WITHDRAWAL);
            transaction.setLocation(transactionCreateDTO.getLocation());

            account.setBalance(account.getBalance().subtract(transactionCreateDTO.getAmount()));
            transactionRepository.save(transaction);
            return new ApiResponse<>(toTransactionResponseDTO(transaction), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional
    @CachePut(value = "transactionsByAccount", key = "#transactionCreateDTO.accountNumber")
    @CacheEvict(value = "accounts", key ="#transactionCreateDTO.accountNumber")
    public ApiResponse<Object> transfer(TransactionCreateDTO transactionCreateDTO){
        try{
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(transactionCreateDTO.getAccountNumber());
            Optional<Account> optionalReceiver = accountRepository.findByAccountNumber(transactionCreateDTO.getReceiverNumber());

            if(optionalAccount.isEmpty() || optionalReceiver.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }

            Account account = optionalAccount.get();
            Account receiver = optionalReceiver.get();

            if(account.getBalance().compareTo(transactionCreateDTO.getAmount()) < 0){
                return new ApiResponse<>(ReturnMessage.INSUFFICIENT_BALANCE.getCode(), ReturnMessage.INSUFFICIENT_BALANCE.getMessage());
            }
            if(account.getStatus() != AccountStatus.ACTIVE){
                return new ApiResponse<>(ReturnMessage.INACTIVE.getCode(), ReturnMessage.INACTIVE.getMessage());
            }

            if(account.getAccountLimit().compareTo(transactionCreateDTO.getAmount()) < 0){
                return new ApiResponse<>(ReturnMessage.OFF_LIMIT.getCode(), ReturnMessage.OFF_LIMIT.getMessage());
            }

            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setAmount(transactionCreateDTO.getAmount());
            transaction.setType(TransactionType.TRANSFER);
            transaction.setReceiver(receiver);
            transaction.setLocation(transactionCreateDTO.getLocation());

            account.setBalance(account.getBalance().subtract(transactionCreateDTO.getAmount()));
            receiver.setBalance(receiver.getBalance().add(transactionCreateDTO.getAmount()));
            transactionRepository.save(transaction);
            return new ApiResponse<>(toTransactionResponseDTO(transaction), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "transactions", key = "#publicId")
    public ApiResponse<Object> getTransaction(UUID publicId){
        try{
            Optional<Transaction> optionalTransaction = transactionRepository.findByPublicId(publicId);

            if(optionalTransaction.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }

            Transaction transaction = optionalTransaction.get();

            return new ApiResponse<>(toTransactionResponseDTO(transaction), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }

    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> search(TransactionSearchDTO dto, Pageable pageable){
        try{
            if(dto == null) {
                return new ApiResponse<>(ReturnMessage.NULL_VALUE.getCode(), ReturnMessage.NULL_VALUE.getMessage());
            }
            if((dto.getFrom() != null && dto.getTo() != null && dto.getFrom().isAfter(dto.getTo()))
            || (dto.getMinAmount() != null && dto.getMaxAmount() != null && dto.getMinAmount().compareTo(dto.getMaxAmount()) > 0)) {
                return new ApiResponse<>(ReturnMessage.INVALID_ARGUMENTS.getCode(), ReturnMessage.INVALID_ARGUMENTS.getMessage());
            }

            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(dto.getAccountNumber());
            Optional<Account> optionalReceiver = accountRepository.findByAccountNumber(dto.getReceiverNumber());

            if(optionalAccount.isEmpty() || optionalReceiver.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }

            Account account = optionalAccount.get();
            Account receiver = optionalReceiver.get();

            Specification<Transaction> spec = (root, query, builder) -> builder.conjunction(); // base

            spec = spec.and(TransactionSpecification.hasAccountId(account.getId()));
            spec = spec.and(TransactionSpecification.hasReceiverId(receiver.getId()));
            spec = spec.and(TransactionSpecification.hasType(dto.getType()));
            spec = spec.and(TransactionSpecification.hasMinAmount(dto.getMinAmount()));
            spec = spec.and(TransactionSpecification.hasMaxAmount(dto.getMaxAmount()));
            spec = spec.and(TransactionSpecification.occurredBefore(dto.getFrom()));
            spec = spec.and(TransactionSpecification.occurredAfter(dto.getTo()));
            spec = spec.and(TransactionSpecification.hasChecked(dto.getChecked()));
            spec = spec.and(TransactionSpecification.hasLocation(dto.getLocation()));
            Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);
            Page<TransactionResponseDTO> transactionResponseDTOPage = transactionPage.map(this::toTransactionResponseDTO);
                return new ApiResponse<>(transactionResponseDTOPage, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> selfSearch(Long id, TransactionUserSearchDTO dto, Pageable pageable){
        try{
            if(dto == null) {
                return new ApiResponse<>(ReturnMessage.NULL_VALUE.getCode(), ReturnMessage.NULL_VALUE.getMessage());
            }
            if((dto.getFrom() != null && dto.getTo() != null && dto.getFrom().isAfter(dto.getTo()))
                    || (dto.getMinAmount() != null && dto.getMaxAmount() != null && dto.getMinAmount().compareTo(dto.getMaxAmount()) > 0)) {
                return new ApiResponse<>(ReturnMessage.INVALID_ARGUMENTS.getCode(), ReturnMessage.INVALID_ARGUMENTS.getMessage());
            }

            Optional<Account> optionalAccount = accountRepository.findById(id);
            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }

            Specification<Transaction> spec = (root, query, builder) -> builder.conjunction(); // base
            spec = spec.and(TransactionSpecification.hasAccountId(id)
                    .or(TransactionSpecification.hasReceiverId(id)));

            spec = spec.and(TransactionSpecification.hasMinAmount(dto.getMinAmount()));
            spec = spec.and(TransactionSpecification.hasMaxAmount(dto.getMaxAmount()));
            spec = spec.and(TransactionSpecification.hasLocation(dto.getLocation()));
            spec = spec.and(TransactionSpecification.occurredBefore(dto.getTo()));
            spec = spec.and(TransactionSpecification.occurredAfter(dto.getFrom()));
            spec = spec.and(TransactionSpecification.hasChecked(dto.getChecked()));

            Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);
            Page<TransactionResponseDTO> transactionResponseDTOPage = transactionPage.map(this::toTransactionResponseDTO);

            return new ApiResponse<>(transactionResponseDTOPage, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> selfSearchByAccountNumber(String accountNumber, TransactionUserSearchDTO dto, Pageable pageable){
        try{
            if(dto == null) {
                return new ApiResponse<>(ReturnMessage.NULL_VALUE.getCode(), ReturnMessage.NULL_VALUE.getMessage());
            }
            if((dto.getFrom() != null && dto.getTo() != null && dto.getFrom().isAfter(dto.getTo()))
                    || (dto.getMinAmount() != null && dto.getMaxAmount() != null && dto.getMinAmount().compareTo(dto.getMaxAmount()) > 0)) {
                return new ApiResponse<>(ReturnMessage.INVALID_ARGUMENTS.getCode(), ReturnMessage.INVALID_ARGUMENTS.getMessage());
            }

            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }
            Account account = optionalAccount.get();
            Specification<Transaction> spec = (root, query, builder) -> builder.conjunction(); // base
            spec = spec.and(TransactionSpecification.hasAccountId(account.getId())
                    .or(TransactionSpecification.hasReceiverId(account.getId())));

            spec = spec.and(TransactionSpecification.hasMinAmount(dto.getMinAmount()));
            spec = spec.and(TransactionSpecification.hasMaxAmount(dto.getMaxAmount()));
            spec = spec.and(TransactionSpecification.hasLocation(dto.getLocation()));
            spec = spec.and(TransactionSpecification.occurredBefore(dto.getTo()));
            spec = spec.and(TransactionSpecification.occurredAfter(dto.getFrom()));
            spec = spec.and(TransactionSpecification.hasChecked(dto.getChecked()));

            Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);
            Page<TransactionResponseDTO> transactionResponseDTOPage = transactionPage.map(this::toTransactionResponseDTO);

            return new ApiResponse<>(transactionResponseDTOPage, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> countTransactionsByLocation(){
        try{
            List<LocationCount> locationCounts = transactionRepository.countTransactionsByLocation();
            return new ApiResponse<>(locationCounts, ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e){
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional
    @CachePut(value = "transactionsByAccountNumber", key = "#transactionCreateDTO.accountNumber")
    @CacheEvict(value = "accounts", key ="#transactionCreateDTO.accountNumber")
    public ApiResponse<Object> depositByAccountNumber(TransactionCreateANDTO transactionCreateDTO){
        try{
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(transactionCreateDTO.getAccountNumber());

            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }

            Account account = optionalAccount.get();

            if(account.getStatus() != AccountStatus.ACTIVE){
                return new ApiResponse<>(ReturnMessage.INACTIVE.getCode(), ReturnMessage.INACTIVE.getMessage());
            }

            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setAmount(transactionCreateDTO.getAmount());
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setLocation(transactionCreateDTO.getLocation());

            account.setBalance(account.getBalance().add(transactionCreateDTO.getAmount()));
            transactionRepository.save(transaction);
            return new ApiResponse<>(toTransactionResponseDTO(transaction), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch(Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional
    @CachePut(value = "transactionsByAccountNumber", key = "#transactionCreateDTO.accountNumber")
    @CacheEvict(value = "accounts", key ="#transactionCreateDTO.accountNumber")
    public ApiResponse<Object> withdrawByAccountNumber(TransactionCreateANDTO transactionCreateDTO) {
        try{
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(transactionCreateDTO.getAccountNumber());

            if(optionalAccount.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }

            Account account = optionalAccount.get();

            if(account.getStatus() != AccountStatus.ACTIVE){
                return new ApiResponse<>(ReturnMessage.INACTIVE.getCode(), ReturnMessage.INACTIVE.getMessage());
            }

            if(account.getBalance().compareTo(transactionCreateDTO.getAmount()) < 0){
                return new ApiResponse<>(ReturnMessage.INSUFFICIENT_BALANCE.getCode(), ReturnMessage.INSUFFICIENT_BALANCE.getMessage());
            }
            if(account.getAccountLimit().compareTo(transactionCreateDTO.getAmount()) < 0){
                return new ApiResponse<>(ReturnMessage.OFF_LIMIT.getCode(), ReturnMessage.OFF_LIMIT.getMessage());
            }

            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setAmount(transactionCreateDTO.getAmount());
            transaction.setType(TransactionType.WITHDRAWAL);
            transaction.setLocation(transactionCreateDTO.getLocation());

            account.setBalance(account.getBalance().subtract(transactionCreateDTO.getAmount()));
            transactionRepository.save(transaction);
            return new ApiResponse<>(toTransactionResponseDTO(transaction), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    @Override
    @Transactional
    @CachePut(value = "transactionsByAccountNumber", key = "#transactionCreateDTO.accountNumber")
    @CacheEvict(value = "accounts", key ="#transactionCreateDTO.accountNumber")
    public ApiResponse<Object> transferByAccountNumber(TransactionCreateANDTO transactionCreateDTO){
        try{
            log.info("DTO: accountnumbe: {}, receivernumber: {}, location: {}, amount: {}", transactionCreateDTO.getAccountNumber(), transactionCreateDTO.getReceiverAccountNumber(),
                    transactionCreateDTO.getLocation(), transactionCreateDTO.getAmount());
            authSecurity.isOwnerOfAccountByAccountNumber(transactionCreateDTO.getAccountNumber());
            Optional<Account> optionalAccount = accountRepository.findByAccountNumber(transactionCreateDTO.getAccountNumber());
            Optional<Account> optionalReceiver = accountRepository.findByAccountNumber(transactionCreateDTO.getReceiverAccountNumber());


            if(optionalAccount.isEmpty() || optionalReceiver.isEmpty()){
                return new ApiResponse<>(ReturnMessage.NOT_FOUND.getCode(), ReturnMessage.NOT_FOUND.getMessage());
            }

            Account account = optionalAccount.get();
            Account receiver = optionalReceiver.get();

            if(account.getBalance().compareTo(transactionCreateDTO.getAmount()) < 0){
                return new ApiResponse<>(ReturnMessage.INSUFFICIENT_BALANCE.getCode(), ReturnMessage.INSUFFICIENT_BALANCE.getMessage());
            }
            if(account.getStatus() != AccountStatus.ACTIVE){
                return new ApiResponse<>(ReturnMessage.INACTIVE.getCode(), ReturnMessage.INACTIVE.getMessage());
            }

            if(account.getAccountLimit().compareTo(transactionCreateDTO.getAmount()) < 0){
                return new ApiResponse<>(ReturnMessage.OFF_LIMIT.getCode(), ReturnMessage.OFF_LIMIT.getMessage());
            }

            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setAmount(transactionCreateDTO.getAmount());
            transaction.setType(TransactionType.TRANSFER);
            transaction.setReceiver(receiver);
            transaction.setLocation(transactionCreateDTO.getLocation());

            account.setBalance(account.getBalance().subtract(transactionCreateDTO.getAmount()));
            receiver.setBalance(receiver.getBalance().add(transactionCreateDTO.getAmount()));
            transactionRepository.save(transaction);
            return new ApiResponse<>(toTransactionResponseDTO(transaction), ReturnMessage.SUCCESS.getCode(), ReturnMessage.SUCCESS.getMessage());
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>(e.getMessage(), ReturnMessage.FAIL.getCode(), ReturnMessage.FAIL.getMessage());
        }
    }

    //helper
    private TransactionResponseDTO toTransactionResponseDTO(Transaction transaction){
        return TransactionResponseDTO.builder()
                .accountNumber(transaction.getAccount().getAccountNumber())
                .receiverNumber(transaction.getReceiver() == null ? null : transaction.getReceiver().getAccountNumber())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .timestamp(transaction.getCreatedAt())
                .location(transaction.getLocation())
                .build();
    }
}
