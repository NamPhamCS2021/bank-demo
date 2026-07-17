package com.example.bankDemo.util;

import com.example.bankDemo.entity.Report;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Level 3, mục 4: Xuất báo cáo tài chính ra Excel, đi kèm PDFExportUtil hiện có.
 */
public final class ExcelExportUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ExcelExportUtil() {
    }

    /** Xuất một báo cáo dạng key/value (giống các báo cáo hiện có trong ReportServiceImpl) ra Excel. */
    public static void exportExcel(OutputStream outputStream, Map<String, Object> reportData, String sheetTitle) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetTitle);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            writeHeaderCell(headerRow, 0, "Chỉ số", headerStyle);
            writeHeaderCell(headerRow, 1, "Giá trị", headerStyle);

            int rowIndex = 1;
            for (Map.Entry<String, Object> entry : reportData.entrySet()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(entry.getKey());
                Object value = entry.getValue();
                row.createCell(1).setCellValue(value == null ? "" : value.toString());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(outputStream);
        }
    }

    /**
     * Xuất bảng số liệu của nhiều kỳ báo cáo liên tiếp kèm biểu đồ đường thể hiện
     * xu hướng tổng số tiền giao dịch theo thời gian (đáp ứng yêu cầu "biểu đồ xu
     * hướng giao dịch" ở Level 3, mục 4). `reports` phải theo thứ tự thời gian
     * tăng dần để biểu đồ đọc đúng từ trái sang phải.
     */
    public static void exportTrendExcel(OutputStream outputStream, String chartTitle, List<Report> reports) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Xu huong giao dich");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Ky bat dau");
            header.createCell(1).setCellValue("Ky ket thuc");
            header.createCell(2).setCellValue("Tong so giao dich");
            header.createCell(3).setCellValue("Tong so tien");
            header.createCell(4).setCellValue("So tien trung binh");

            int rowIdx = 1;
            for (Report r : reports) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getStartAt() == null ? "" : r.getStartAt().format(DATE_FORMAT));
                row.createCell(1).setCellValue(r.getEndAt() == null ? "" : r.getEndAt().format(DATE_FORMAT));
                row.createCell(2).setCellValue(r.getNumberOfTransactions() == null ? 0 : r.getNumberOfTransactions());
                row.createCell(3).setCellValue(r.getTotalAmount() == null ? 0 : r.getTotalAmount().doubleValue());
                row.createCell(4).setCellValue(r.getAverageAmount() == null ? 0 : r.getAverageAmount().doubleValue());
            }

            int lastDataRow = rowIdx - 1;
            if (lastDataRow >= 1) {
                addTrendChart(sheet, chartTitle, lastDataRow);
            }

            for (int col = 0; col < 5; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(outputStream);
        }
    }

    private static void addTrendChart(XSSFSheet sheet, String chartTitle, int lastDataRow) {
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 6, 1, 16, 22);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(chartTitle);
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Ky bao cao");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("So tien");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFDataSource<String> periods = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(1, lastDataRow, 0, 0));
        XDDFNumericalDataSource<Double> totalAmounts = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(1, lastDataRow, 3, 3));
        XDDFNumericalDataSource<Double> averageAmounts = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(1, lastDataRow, 4, 4));

        XDDFLineChartData chartData = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        XDDFLineChartData.Series totalSeries = (XDDFLineChartData.Series) chartData.addSeries(periods, totalAmounts);
        totalSeries.setTitle("Tong so tien giao dich", null);
        totalSeries.setSmooth(false);
        totalSeries.setMarkerStyle(MarkerStyle.CIRCLE);

        XDDFLineChartData.Series avgSeries = (XDDFLineChartData.Series) chartData.addSeries(periods, averageAmounts);
        avgSeries.setTitle("So tien trung binh", null);
        avgSeries.setSmooth(false);
        avgSeries.setMarkerStyle(MarkerStyle.TRIANGLE);

        chart.plot(chartData);
    }

    private static void writeHeaderCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}