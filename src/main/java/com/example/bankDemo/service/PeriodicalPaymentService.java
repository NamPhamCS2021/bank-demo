package com.example.bankDemo.service;

import com.example.bankDemo.dto.ApiResponse;
import com.example.bankDemo.dto.periodicallypayment.PeriodicalPaymentSearchDTO;
import com.example.bankDemo.dto.periodicallypayment.PeriodicalPaymentUserSearchDTO;
import com.example.bankDemo.dto.periodicallypayment.PeriodicallyPaymentDTO;
import com.example.bankDemo.dto.periodicallypayment.PeriodicallyPaymentUpdateDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PeriodicalPaymentService {

    ApiResponse<Object> createPeriodicalPayment(PeriodicallyPaymentDTO periodicallyPaymentDTO);
    ApiResponse<Object> updatePeriodicalPayment(UUID publicId, PeriodicallyPaymentUpdateDTO periodicallyPaymentUpdateDTO);
    ApiResponse<Object> getPeriodicalPaymentById(UUID publicId);;
    ApiResponse<Object> getPeriodicalPaymentByAccountNumber(String accountNumber, Pageable pageable);
    ApiResponse<Object> search(PeriodicalPaymentSearchDTO dto, Pageable pageable);
    ApiResponse<Object> selfSearch(String accountNumber, PeriodicalPaymentUserSearchDTO dto, Pageable pageable);
    void processingPayment();
}
