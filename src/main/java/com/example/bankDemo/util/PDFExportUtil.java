package com.example.bankDemo.util;

import com.example.bankDemo.entity.Report;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PDFExportUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void exportPDF(OutputStream outputStream, Map<String, Object> data) {
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD);

        Paragraph title = new Paragraph("Report", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(""));

        document.add(buildKeyValueTable(data));
        document.close();
    }

    /**
     * Level 3, muc 4: bao cao xu huong - ve bieu do duong (JFreeChart) roi nhung
     * vao PDF duoi dang anh, kem theo bang so lieu chi tiet ben duoi. `reports`
     * phai theo thu tu thoi gian tang dan.
     */
    public static void exportTrendPdf(OutputStream outputStream, String title, List<Report> reports) throws IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD);
        Paragraph titleParagraph = new Paragraph(title, titleFont);
        titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titleParagraph);
        document.add(new Paragraph(""));

        if (!reports.isEmpty()) {
            Image chartImage = Image.getInstance(renderTrendChartPng(title, reports));
            chartImage.scaleToFit(500, 300);
            chartImage.setAlignment(Image.ALIGN_CENTER);
            document.add(chartImage);
            document.add(new Paragraph(""));
        }

        document.add(buildTrendTable(reports));
        document.close();
    }

    private static byte[] renderTrendChartPng(String title, List<Report> reports) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Report r : reports) {
            String label = r.getStartAt() == null ? "" : r.getStartAt().format(DATE_FORMAT);
            double total = r.getTotalAmount() == null ? 0 : r.getTotalAmount().doubleValue();
            double average = r.getAverageAmount() == null ? 0 : r.getAverageAmount().doubleValue();
            dataset.addValue(total, "Tong so tien", label);
            dataset.addValue(average, "So tien trung binh", label);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                title, "Ky bao cao", "So tien", dataset, PlotOrientation.VERTICAL, true, false, false);

        BufferedImage image = chart.createBufferedImage(500, 300);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private static PdfPTable buildKeyValueTable(Map<String, Object> data) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD);
        table.addCell(new Paragraph("Key", headerFont));
        table.addCell(new Paragraph("Value", headerFont));

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            table.addCell(entry.getKey());
            Object value = entry.getValue();
            table.addCell(value == null ? "" : value.toString());
        }
        return table;
    }

    private static PdfPTable buildTrendTable(List<Report> reports) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD);
        for (String header : new String[]{"Ky bat dau", "Ky ket thuc", "So giao dich", "Tong so tien", "TB moi giao dich"}) {
            table.addCell(new Paragraph(header, headerFont));
        }

        for (Report r : reports) {
            table.addCell(r.getStartAt() == null ? "" : r.getStartAt().format(DATE_FORMAT));
            table.addCell(r.getEndAt() == null ? "" : r.getEndAt().format(DATE_FORMAT));
            table.addCell(r.getNumberOfTransactions() == null ? "0" : r.getNumberOfTransactions().toString());
            table.addCell(r.getTotalAmount() == null ? "0" : r.getTotalAmount().toString());
            table.addCell(r.getAverageAmount() == null ? "0" : r.getAverageAmount().toString());
        }
        return table;
    }
}