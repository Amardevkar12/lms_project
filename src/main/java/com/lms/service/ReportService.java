package com.lms.service;

import org.springframework.stereotype.Service;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;

// import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.*;

@Service
public class ReportService {

    // ================= DATA =================
    public int totalEmployees() {
        return 20;
    }

    public int totalLeaves() {
        return 9;
    }

    public int approvedLeaves() {
        return 8;
    }

    public int rejectedLeaves() {
        return 1;
    }

    // ================= EXCEL =================
    public byte[] generateExcel() {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet = workbook.createSheet("LMS Report");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Type");
            header.createCell(1).setCellValue("Count");

            Object[][] data = {
                    { "Employees", totalEmployees() },
                    { "Leaves", totalLeaves() },
                    { "Approved", approvedLeaves() },
                    { "Rejected", rejectedLeaves() }
            };

            int rowNum = 1;

            for (Object[] d : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(d[0].toString());
                row.createCell(1).setCellValue((int) d[1]);
            }

            byte[] chartBytes = generateChart(); // PNG image

            int pictureIdx = workbook.addPicture(chartBytes, Workbook.PICTURE_TYPE_PNG);

            CreationHelper helper = workbook.getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();

            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(3);
            anchor.setRow1(1);

            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    // ================= PDF =================
    public byte[] generatePDF() {

        try {
            Document doc = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);

            Paragraph title = new Paragraph("LMS REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);

            doc.add(title);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Total Employees: " + totalEmployees()));
            doc.add(new Paragraph("Total Leaves: " + totalLeaves()));
            doc.add(new Paragraph("Approved: " + approvedLeaves()));
            doc.add(new Paragraph("Rejected: " + rejectedLeaves()));

            doc.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    // ================= CHART =================
    public byte[] generateChart() {

        try {
            DefaultPieDataset dataset = new DefaultPieDataset();

            dataset.setValue("Approved", approvedLeaves());
            dataset.setValue("Rejected", rejectedLeaves());
            dataset.setValue("Pending",
                    totalLeaves() - approvedLeaves() - rejectedLeaves());

            JFreeChart chart = ChartFactory.createPieChart(
                    "Leave Status",
                    dataset,
                    true,
                    true,
                    false);

            BufferedImage image = chart.createBufferedImage(500, 400);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Chart generation failed", e);
        }
    }
}