package com.example.bankDemo.controller;

import com.example.bankDemo.entity.Report;
import com.example.bankDemo.enums.ExportFormat;
import com.example.bankDemo.enums.ReportPeriod;
import com.example.bankDemo.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ---------- Level 1/2 endpoints (không đổi) ----------

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/transaction-report")
    public Map<String, Object> getTransactionReport(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                    @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return reportService.generateReport(start, end);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/report-for-account/{accountNumber}")
    public Map<String, Object> generateReportByAccount(@PathVariable String accountNumber,
                                                       @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                       @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return reportService.generateReportByAccount(accountNumber, start, end);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/account-report")
    public Map<String, Object> generateAccountReport(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                     @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return reportService.generateAccountReport(start, end);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/transaction-report-pdf")
    public void exportTransactionReportToPdf(HttpServletResponse response,
                                             @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                             @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        writeReport(response, reportService.generateReport(start, end), ExportFormat.PDF, "transaction-report");
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/transaction-report-by-account-pdf")
    public void exportTransactionReportByAccountToPdf(HttpServletResponse response, @RequestParam String accountNumber,
                                                      @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                      @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        writeReport(response, reportService.generateReportByAccount(accountNumber, start, end), ExportFormat.PDF, "account-transaction-report");
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/account-report-pdf")
    public void exportAccountReportToPdf(HttpServletResponse response,
                                         @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                         @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        writeReport(response, reportService.generateAccountReport(start, end), ExportFormat.PDF, "account-report");
    }

    // ---------- Level 3, mục 1: báo cáo định kỳ (tuần/quý/năm) ----------

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/periodical-report")
    public Map<String, Object> getPeriodicalReport(@RequestParam ReportPeriod period,
                                                   @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                   @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return reportService.generatePeriodicalReport(period, start, end);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/periodical-report-history")
    public Page<Report> getPeriodicalReportHistory(@RequestParam ReportPeriod period,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startAt").descending());
        return reportService.getPeriodicalReportHistory(period, pageable);
    }

    // ---------- Level 3, mục 4: xuất báo cáo Excel/PDF + biểu đồ xu hướng ----------

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/periodical-report-excel")
    public void exportPeriodicalReportExcel(HttpServletResponse response, @RequestParam ReportPeriod period,
                                            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Map<String, Object> reportMap = reportService.generatePeriodicalReport(period, start, end);
        writeReport(response, reportMap, ExportFormat.EXCEL, "periodical-report");
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/periodical-report-pdf")
    public void exportPeriodicalReportPdf(HttpServletResponse response, @RequestParam ReportPeriod period,
                                          @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                          @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Map<String, Object> reportMap = reportService.generatePeriodicalReport(period, start, end);
        writeReport(response, reportMap, ExportFormat.PDF, "periodical-report");
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/trend-report-excel")
    public void exportTrendReportExcel(HttpServletResponse response, @RequestParam ReportPeriod period) {
        writeTrendReport(response, period, ExportFormat.EXCEL);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/trend-report-pdf")
    public void exportTrendReportPdf(HttpServletResponse response, @RequestParam ReportPeriod period) {
        writeTrendReport(response, period, ExportFormat.PDF);
    }

    //helpers

    private void writeReport(HttpServletResponse response, Map<String, Object> reportMap, ExportFormat format, String filenamePrefix) {
        try {
            setResponseHeaders(response, format, filenamePrefix);
            reportService.exportReport(reportMap, format, response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("failed to export report as " + format, e);
        }
    }

    private void writeTrendReport(HttpServletResponse response, ReportPeriod period, ExportFormat format) {
        try {
            setResponseHeaders(response, format, "trend-report");
            reportService.exportTrendReport(period, format, response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("failed to export trend report", e);
        }
    }

    private void setResponseHeaders(HttpServletResponse response, ExportFormat format, String filenamePrefix) {
        switch (format) {
            case PDF -> {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=" + filenamePrefix + ".pdf");
            }
            case EXCEL -> {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=" + filenamePrefix + ".xlsx");
            }
        }
    }
}