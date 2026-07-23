package com.example.bankDemo.service;

import com.example.bankDemo.entity.Report;
import com.example.bankDemo.enums.ExportFormat;
import com.example.bankDemo.enums.ReportPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReportService {

    Map<String, Object> generateReport(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, Object> generateReportByAccount(String accountNumber, LocalDateTime start, LocalDateTime end);

    Map<String, Object> generateAccountReport(LocalDateTime start, LocalDateTime end);


    Map<String, Object> generatePeriodicalReport(ReportPeriod period, LocalDateTime start, LocalDateTime end);

    Page<Report> getPeriodicalReportHistory(ReportPeriod period, Pageable pageable);

    List<Report> getRecentPeriodicalReportsForTrend(ReportPeriod period);


    void exportReport(Map<String, Object> reportData, ExportFormat format, OutputStream outputStream) throws IOException;

    void exportTrendReport(ReportPeriod period, ExportFormat format, OutputStream outputStream) throws IOException;
}