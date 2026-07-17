package com.example.bankDemo.service;

import com.example.bankDemo.entity.PeriodicalReport;
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

    /**
     * Sinh báo cáo cho một chu kỳ (tuần/quý/năm) và LƯU LẠI thành một
     * PeriodicalReport để tra cứu lịch sử và vẽ xu hướng sau này.
     */
    Map<String, Object> generatePeriodicalReport(ReportPeriod period, LocalDateTime start, LocalDateTime end);

    Page<Report> getPeriodicalReportHistory(ReportPeriod period, Pageable pageable);

    /** Các kỳ báo cáo gần nhất, theo thứ tự thời gian tăng dần - dùng để vẽ biểu đồ xu hướng. */
    List<Report> getRecentPeriodicalReportsForTrend(ReportPeriod period);

    /**
     * Ghi một báo cáo dạng key/value ra outputStream theo định dạng chỉ định.
     * Nhận một OutputStream thuần (không phải HttpServletResponse) để không ràng
     * buộc service vào servlet API - controller chịu trách nhiệm set header HTTP,
     * service chỉ lo phần nội dung file.
     */
    void exportReport(Map<String, Object> reportData, ExportFormat format, OutputStream outputStream) throws IOException;

    /** Ghi báo cáo xu hướng (kèm biểu đồ) của một chu kỳ ra outputStream theo định dạng chỉ định. */
    void exportTrendReport(ReportPeriod period, ExportFormat format, OutputStream outputStream) throws IOException;
}