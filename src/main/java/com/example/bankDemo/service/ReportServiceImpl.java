package com.example.bankDemo.service;

import com.example.bankDemo.entity.Account;
import com.example.bankDemo.entity.Report;
import com.example.bankDemo.enums.AccountStatus;
import com.example.bankDemo.enums.CustomerType;
import com.example.bankDemo.enums.ExportFormat;
import com.example.bankDemo.enums.ReportPeriod;
import com.example.bankDemo.repository.*;
import com.example.bankDemo.util.ExcelExportUtil;
import com.example.bankDemo.util.PDFExportUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements ReportService {

    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    private final AccountRepository accountRepository;

    private final CustomerRepository customerRepository;

    private final TransactionRepository transactionRepository;

    private final AlertRepository alertRepository;

    private final ReportRepository reportRepository;

    private final Executor virtualExecutor;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateReport(LocalDateTime start, LocalDateTime end) {
        CompletableFuture<BigDecimal> biggest = CompletableFuture.supplyAsync(
                () -> transactionRepository.findBiggestTransactionBetweenTime(start, end).getAmount(),
                virtualExecutor);
        CompletableFuture<BigDecimal> smallest = CompletableFuture.supplyAsync(
                () -> transactionRepository.findSmallestTransactionBetweenTime(start, end).getAmount(),
                virtualExecutor);
        CompletableFuture<Long> totalTransactions = CompletableFuture.supplyAsync(
                () -> transactionRepository.countAllTransactionByDate(start, end),
                virtualExecutor);
        CompletableFuture<BigDecimal> average = CompletableFuture.supplyAsync(
                () -> transactionRepository.getAvgTransactionAmountBetween(start, end),
                virtualExecutor);
        CompletableFuture<BigDecimal> totalAmount = CompletableFuture.supplyAsync(
                () -> transactionRepository.getTotalTransactionAmountBetween(start, end),
                virtualExecutor);

        CompletableFuture.allOf(biggest, smallest, totalTransactions, average, totalAmount).join();

        Map<String, Object> report = new HashMap<>();
        report.put("biggest transaction amount", biggest.join());
        report.put("smallest transaction amount", smallest.join());
        report.put("total transactions made", totalTransactions.join());
        report.put("average transaction amount", average.join());
        report.put("total transaction amount", totalAmount.join());

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateReportByAccount(String accountNumber, LocalDateTime start, LocalDateTime end) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Can not find account with account number: " + accountNumber));
        // account.getId() (khóa chính nội bộ) chỉ dùng để join/truy vấn hiệu quả trong nội bộ
        // service - không lộ ra ngoài API, người gọi chỉ thấy accountNumber.
        Long accountId = account.getId();
        String customerName = account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName();
        BigDecimal balance = account.getBalance();
        BigDecimal accountLimit = account.getAccountLimit();
        BigDecimal biggestAmount = transactionRepository.findBiggestTransactionBetweenTimeByAccountId(accountId, start, end);
        BigDecimal smallestAmount = transactionRepository.findSmallestTransactionBetweenTimeByAccountId(accountId, start, end);
        Long totalTransaction = transactionRepository.countTransactionBetweenTimeByAccount(accountId, start, end);
        BigDecimal averageTransactionAmount = transactionRepository.findAverageTransactionBetweenTimeByAccountId(accountId, start, end);
        BigDecimal totalAmount = transactionRepository.findTotalTransactionAmountByAccountId(accountId, start, end);
        Long totalAlert = alertRepository.countAllAlertsByAccountId(accountId);

        Map<String, Object> report = new HashMap<>();
        report.put("customer name", customerName);
        report.put("account number", accountNumber);
        report.put("balance", balance);
        report.put("account limit", accountLimit);
        report.put("biggest transaction amount", biggestAmount);
        report.put("smallest transaction amount", smallestAmount);
        report.put("average transaction amount", averageTransactionAmount);
        report.put("total transaction amount", totalAmount);
        report.put("total alerts", totalAlert);
        report.put("total transactions made", totalTransaction);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateAccountReport(LocalDateTime start, LocalDateTime end) {
        Long totalAccount = accountRepository.countAllAccounts();
        Long corporalAccounts = accountRepository.countAllAccountsByType(CustomerType.CORPORATE);
        Long personalAccounts = accountRepository.countAllAccountsByType(CustomerType.PERSONAL);
        Long temporaryAccounts = accountRepository.countAllAccountsByType(CustomerType.TEMPORARY);
        Long activeAccount = accountRepository.countAllAccountsByStatus(AccountStatus.ACTIVE);

        Map<String, Object> report = new HashMap<>();
        report.put("total accounts", totalAccount);
        report.put("corporate accounts", corporalAccounts);
        report.put("personal accounts", personalAccounts);
        report.put("temporary accounts", temporaryAccounts);
        report.put("active accounts", activeAccount);

        return report;
    }

    @Override
    @Transactional
    public Map<String, Object> generatePeriodicalReport(ReportPeriod period, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> transactionReport = generateReport(start, end);

        Report report = new Report();
        report.setPeriod(period);
        report.setStartAt(start);
        report.setEndAt(end);
        report.setNumberOfTransactions((Long) transactionReport.get("total transactions made"));
        report.setTotalAmount((BigDecimal) transactionReport.get("total transaction amount"));
        report.setAverageAmount((BigDecimal) transactionReport.get("average transaction amount"));
        report.setMaximumAmount((BigDecimal) transactionReport.get("biggest transaction amount"));
        report.setMinimumAmount((BigDecimal) transactionReport.get("smallest transaction amount"));

        reportRepository.save(report);

        transactionReport.put("period", period);
        transactionReport.put("reportId", report.getPublicId());
        transactionReport.put("startAt", start);
        transactionReport.put("endAt", end);

        return transactionReport;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> getPeriodicalReportHistory(ReportPeriod period, Pageable pageable) {
        return reportRepository.findByPeriodOrderByStartAtDesc(period, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> getRecentPeriodicalReportsForTrend(ReportPeriod period) {
        List<Report> reports = reportRepository.findTop24ByPeriodOrderByStartAtDesc(period);
        // đảo lại thành thứ tự thời gian tăng dần để biểu đồ xu hướng đọc từ trái qua phải cho đúng
        java.util.Collections.reverse(reports);
        return reports;
    }

    @Override
    public void exportReport(Map<String, Object> reportData, ExportFormat format, OutputStream outputStream) throws IOException {
        switch (format) {
            case PDF -> PDFExportUtil.exportPDF(outputStream, reportData);
            case EXCEL -> ExcelExportUtil.exportExcel(outputStream, reportData, "report");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void exportTrendReport(ReportPeriod period, ExportFormat format, OutputStream outputStream) throws IOException {
        List<Report> trend = getRecentPeriodicalReportsForTrend(period);
        String title = "Xu huong giao dich - " + period;
        switch (format) {
            case PDF -> PDFExportUtil.exportTrendPdf(outputStream, title, trend);
            case EXCEL -> ExcelExportUtil.exportTrendExcel(outputStream, title, trend);
        }
    }
}