package com.willows.rta.service;

import com.willows.rta.model.Member;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting member data to various formats
 */
@Service
public class MemberExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Export members to CSV format
     */
    public void exportToCsv(List<Member> members, PrintWriter writer) {
        // Write header
        writer.println("ID,Full Name,Flat Number,Address,Email,Phone,Membership Status,Has Login Account,Leaseholder,Registration Date");

        // Write data rows
        for (Member m : members) {
            writer.println(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                m.getId(),
                escapeCsv(m.getFullName()),
                escapeCsv(m.getFlatNumber()),
                escapeCsv(m.getAddress()),
                escapeCsv(m.getEmail()),
                escapeCsv(m.getPhoneNumber()),
                m.getMembershipStatus(),
                m.isHasUserAccount() ? "Yes" : "No",
                m.isLeaseholder() ? "Yes" : "No",
                m.getRegistrationDate() != null ? m.getRegistrationDate().format(DATE_FORMATTER) : ""
            ));
        }
        
        writer.flush();
    }

    /**
     * Export members to Excel format with formatting
     */
    public void exportToExcel(List<Member> members, OutputStream outputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Members");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID", "Full Name", "Flat Number", "Address", "Email", 
            "Phone", "Membership Status", "Has Login Account", 
            "Leaseholder", "Registration Date"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (Member m : members) {
            Row row = sheet.createRow(rowNum++);

            createStyledCell(row, 0, m.getId().toString(), dataStyle);
            createStyledCell(row, 1, m.getFullName(), dataStyle);
            createStyledCell(row, 2, m.getFlatNumber(), dataStyle);
            createStyledCell(row, 3, m.getAddress(), dataStyle);
            createStyledCell(row, 4, m.getEmail(), dataStyle);
            createStyledCell(row, 5, m.getPhoneNumber(), dataStyle);
            createStyledCell(row, 6, m.getMembershipStatus(), dataStyle);
            createStyledCell(row, 7, m.isHasUserAccount() ? "Yes" : "No", dataStyle);
            createStyledCell(row, 8, m.isLeaseholder() ? "Yes" : "No", dataStyle);
            createStyledCell(row, 9, 
                m.getRegistrationDate() != null ? m.getRegistrationDate().format(DATE_FORMATTER) : "", 
                dataStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // Add a bit of padding
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
        }

        // Write to output stream
        workbook.write(outputStream);
        workbook.close();
    }

    /**
     * Helper method to create a styled cell
     */
    private void createStyledCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // If contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}
