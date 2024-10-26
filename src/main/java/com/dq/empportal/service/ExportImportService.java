package com.dq.empportal.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ExportImportService<T> {


    public void exportToExcel(List<T> data, OutputStream outputStream) throws IOException, IllegalAccessException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("No data to export");
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        Field[] fields = data.get(0).getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(fields[i].getName());
        }

        // Fill data rows
        int rowNum = 1;
        for (T item : data) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                Object value = fields[i].get(item);
                Cell cell = row.createCell(i);
                if (value != null) {
                    if (value instanceof Boolean) {
                        cell.setCellValue((Boolean) value);
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            }
        }

        // Write to OutputStream and close resources
        try {
            workbook.write(outputStream);
        } finally {
            workbook.close();
            outputStream.close();
        }
    }


    public List<T> importFromCSV(MultipartFile file, Class<T> clazz) throws IOException, InstantiationException, IllegalAccessException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded");
        }

        List<T> importedData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            // Print headers for debugging
            System.out.println("CSV Headers: " + csvParser.getHeaderMap().keySet());

            for (CSVRecord csvRecord : csvParser) {
                T instance = clazz.newInstance();
                Field[] fields = clazz.getDeclaredFields();

                for (Field field : fields) {
                    field.setAccessible(true);
                    String header = field.getName();
                    String value = getValueOrDefault(csvRecord, header);

                    if (header.equalsIgnoreCase("id")) {
                        continue;
                    }
                    if (field.getType().equals(String.class)) {
                        field.set(instance, value);
                    } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                        field.set(instance, value.isEmpty() ? null : Integer.parseInt(value));
                    } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                        field.set(instance, value.isEmpty() ? null : Long.parseLong(value));
                    } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                        field.set(instance, value.isEmpty() ? null : Double.parseDouble(value));
                    } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                        field.set(instance, parseBoolean(value));
                    } else if (field.getType().equals(LocalDateTime.class)) {
                        field.set(instance, value.isEmpty() ? null : LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME));
                    } else if (field.getType().equals(List.class)) {
                        field.set(instance, parseList(value));
                    }
                }
                importedData.add(instance);
            }
        }
        return importedData;
    }

    // Utility method to get value from CSVRecord or default to empty string if not present
    private String getValueOrDefault(CSVRecord csvRecord, String header) {
        return csvRecord.isMapped(header) ? csvRecord.get(header) : "";
    }

    // Utility method to parse boolean values from CSV
    private boolean parseBoolean(String value) {
        return value != null && !value.isEmpty() && Boolean.parseBoolean(value);
    }

    // Utility method to parse a list from a comma-separated string
    private List<String> parseList(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(", "));
    }
}
