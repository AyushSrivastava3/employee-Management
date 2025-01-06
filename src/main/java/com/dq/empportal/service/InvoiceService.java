package com.dq.empportal.service;

import com.dq.empportal.dtos.InvoiceDto;
import com.dq.empportal.model.Client;
import com.dq.empportal.model.Employee;
import com.dq.empportal.model.Invoice;
import com.dq.empportal.repository.ClientRepository;
import com.dq.empportal.repository.EmployeeClientInfoRepository;
import com.dq.empportal.repository.InvoiceRepository;
import com.dq.empportal.model.EmployeeClientInfo;
import com.ibm.icu.text.RuleBasedNumberFormat;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.InputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {
    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private EmployeeClientInfoRepository employeeClientInfoRepository;

    @Autowired
    private ClientRepository clientRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Invoice generateMonthlyInvoice(Integer clientId, YearMonth yearMonth) {
        // Fetch the client using the provided clientId
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Define the start and end date for the specified month
        LocalDate startDate = yearMonth.atDay(1); // First day of the specified month
        LocalDate endDate = yearMonth.atEndOfMonth(); // Last day of the specified month

        // Fetch all employees assigned to this client and working during the invoice period
        List<EmployeeClientInfo> resources = employeeClientInfoRepository
                .findActiveEmployeesByClientForInvoice(client, startDate, endDate);

        // Calculate total invoice amount (after considering leaves)
        Double totalAmount = calculateTotalInvoiceAmount(resources, startDate, endDate);

//        // Calculate the total amount with taxes
        Double totalAmountWithTaxes = totalAmount;

        // Create a new invoice every time (skip the check for existing invoices)
        Invoice invoice = new Invoice();
        invoice.setClient(client);
        invoice.setStartDate(startDate);
        invoice.setEndDate(endDate);
        invoice.setStatus("Pending");
        String valueInInr= String.valueOf(totalAmount*81);
        if (client.getCurrency().equals("USD")){
            invoice.setInInr("Rs"+" "+valueInInr);
        }
        else {
            invoice.setInInr("Rs"+" "+totalAmount);
        }
        invoice.setMonth(String.valueOf(yearMonth.getMonth()));

        // Initialize the employeeClientInfos list
        invoice.setEmployeeClientInfos(new ArrayList<>());

        // Convert the resources list to a Set to ensure uniqueness
        Set<EmployeeClientInfo> uniqueResources = new HashSet<>(resources);

        // Add only unique EmployeeClientInfos to avoid duplicate entries
        for (EmployeeClientInfo resource : uniqueResources) {
            // Check if this association already exists in the database
            boolean alreadyExists = invoice.getEmployeeClientInfos().stream()
                    .anyMatch(info -> info.equals(resource));

            if (!alreadyExists) {
                invoice.getEmployeeClientInfos().add(resource);
            }
        }

        // Set the total amount for the invoice
        invoice.setTotalAmount(client.getCurrency()+" "+totalAmount);

        // Add tax details to the invoice
//        invoice.setCgst(cgst);
//        invoice.setSgst(sgst);
//        invoice.setIgst(igst);
        invoice.setTotalAmountAfterGstInWords(convertAmountToWords(totalAmountWithTaxes));

        // Format the total amount with taxes as a string
        String formattedTotalAmountWithTaxes = String.format("%s %.2f", client.getCurrency(), totalAmountWithTaxes);
        invoice.setTotalAmountAfterGst(formattedTotalAmountWithTaxes);

        // Set the raised date to the current date
        LocalDate raisedDate = LocalDate.now();
        invoice.setRaisedDate(raisedDate);

        // Set the due date to 60 days after the raised date
        log.info("Timeline",invoice.getClient().getTimeline());
        invoice.setDueDate(raisedDate.plusDays(Long.parseLong(invoice.getClient().getTimeline())));

        // Generate a dynamic invoice number (Dq/{invoiceNumber})
        long invoiceCount = invoiceRepository.count(); // Get the total number of invoices
        String invoiceNo = "DQ/PVT/" + System.currentTimeMillis(); // Uses the current timestamp in milliseconds

        invoice.setInvoiceNo(invoiceNo);

        // Set comments for the invoice
        String comments = "Invoice generated for " + yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        invoice.setComments(comments);

        // Save the new invoice with employee-client info associations
        invoice = invoiceRepository.save(invoice);

        return invoice;
    }

//    private Double calculateTotalInvoiceAmount(List<EmployeeClientInfo> employees, LocalDate startDate, LocalDate endDate) {
//        double total = 0;
//
//        for (EmployeeClientInfo employee : employees) {
//            double hourlyRate = employee.getHourlyRate();
//            int totalBillableHours = employee.TotalBillableHours(startDate, endDate);
//
//            // Get the number of leave days, holidays, and non-billable days within the invoice period
//            //int leaveDaysWithinPeriod = employee.getLeaveDaysWithinPeriod(startDate, endDate);
//            int leaveDaysWithinPeriod = employee.getLeaveDaysWithinPeriod(startDate,endDate);
//            //int holidaysWithinPeriod = employee.getHolidaysWithinPeriod(startDate, endDate);
//            int holidaysWithinPeriod=employee.getHolidaysWithinPeriod(startDate,endDate);
//            //int nonBillableDaysWithinPeriod = employee.getNonBillableDaysWithinPeriod(startDate, endDate);
//            int nonBillableDaysWithinPeriod=employee.getNonBillableDaysWithinPeriod(startDate,endDate);
//            int weekendDays= employee.getWeekendDays(startDate, endDate);
//
//            // Assuming 9 hours per day for leaves, holidays, and non-billable days, deduct from the total billable hours
//            int hoursToDeduct = (leaveDaysWithinPeriod + holidaysWithinPeriod + nonBillableDaysWithinPeriod-weekendDays) * 8;
//            totalBillableHours -= hoursToDeduct;
//
//            // Ensure that billable hours are not negative
//            totalBillableHours = Math.max(totalBillableHours, 0);
//            employee.setTotalBillableHours(totalBillableHours);
//
//            // Calculate the total cost for this employee
//            total += hourlyRate * totalBillableHours;
//        }
//
//        return total;
//    }



    private Double calculateTotalInvoiceAmount(List<EmployeeClientInfo> employees, LocalDate startDate, LocalDate endDate) {
        double total = 0;

        for (EmployeeClientInfo employee : employees) {
            double hourlyRate = employee.getHourlyRate();
            int totalBillableHours = employee.TotalBillableHours(startDate, endDate);

            // Get the number of leave days, holidays, and non-billable days within the invoice period
            int leaveDaysWithinPeriod = employee.getLeaveDaysWithinPeriod(startDate, endDate);
            int holidaysWithinPeriod = employee.getHolidaysWithinPeriod(startDate, endDate);
            int nonBillableDaysWithinPeriod = employee.getNonBillableDaysWithinPeriod(startDate, endDate);
            int weekendDays = employee.getWeekendDays(startDate, endDate);

            // Deduct hours for leave, holidays, and non-billable days
            int hoursToDeduct = (leaveDaysWithinPeriod + holidaysWithinPeriod + nonBillableDaysWithinPeriod - weekendDays) * 8;
            totalBillableHours -= hoursToDeduct;

            // Ensure that billable hours are not negative
            totalBillableHours = Math.max(totalBillableHours, 0);
            employee.setTotalBillableHours(totalBillableHours);

            // Calculate the total cost for this employee
            double employeeAmount = hourlyRate * totalBillableHours;

            // Calculate taxes for this employee's amount
            double cgst = 0.0, sgst = 0.0, igst = 0.0;
            Client client = employee.getClient(); // Assuming EmployeeClientInfo has a reference to Client

            if (client.getClientCountry().equalsIgnoreCase("India")) {
                if (client.getState().equalsIgnoreCase("Telangana")) {
                    cgst = employeeAmount * 0.09; // 9%
                    sgst = employeeAmount * 0.09; // 9%
                } else {
                    igst = employeeAmount * 0.18; // 18%
                }
            }

            // Add taxes to the employee's amount
            double employeeAmountWithTax = employeeAmount + cgst + sgst + igst;

            // Add the employee's total amount with tax to the overall total
            total += employeeAmountWithTax;
        }

        return total;
    }



//    public ByteArrayInputStream generateInvoicePdf(Invoice invoice, List<EmployeeClientInfo> employeeClientInfos) {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        try {
//            // Initialize PDF writer and document
//            PdfWriter writer = new PdfWriter(out);
//            PdfDocument pdfDoc = new PdfDocument(writer);
//            Document document = new Document(pdfDoc);
//
//            // Load standard fonts
//            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//
//            // Create table for company details in the top right corner
//            Table companyTable = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(40))
//                    .setHorizontalAlignment(HorizontalAlignment.RIGHT); // Align to top-right
//
//            companyTable.addCell(new Cell().add(new Paragraph("DIGIQUAD SOLUTIONS").setFont(titleFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
//            companyTable.addCell(new Cell().add(new Paragraph("Flat No.302, Sai Chandra Residency,\nOpp to Greenpeace, Puppalagud,\nManikonda, Serilingampally,\nRangareddy").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
//            companyTable.addCell(new Cell().add(new Paragraph("Contact: +91- 8790922288").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
//            companyTable.addCell(new Cell().add(new Paragraph("Email: info@digiquadsolutions.com").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
//
//            // Add the company details to the document
//            document.add(companyTable);
//
//            // Add a blank line for spacing
//            document.add(new Paragraph("\n"));
//
//            // Add Invoice title
//            Paragraph title = new Paragraph("Invoice")
//                    .setFont(titleFont)
//                    .setFontSize(18)
//                    .setTextAlignment(TextAlignment.CENTER);
//            document.add(title);
//
//            // Add client information
//            document.add(new Paragraph("Client Name: " + invoice.getClient().getClientName()).setFont(regularFont));
//            document.add(new Paragraph("Invoice Date: " + LocalDate.now()).setFont(regularFont));
//            document.add(new Paragraph("Invoice Period: " + invoice.getStartDate() + " to " + invoice.getEndDate()).setFont(regularFont));
//            document.add(new Paragraph("\n")); // Add blank line for spacing
//
//            // Add table for Employee information
//            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2}));
//            table.setWidth(UnitValue.createPercentValue(100));
//
//            // Add table headers
//            table.addHeaderCell(new Cell().add(new Paragraph("Employee Name").setFont(titleFont)));
//            table.addHeaderCell(new Cell().add(new Paragraph("Work Hours").setFont(titleFont)));
//            table.addHeaderCell(new Cell().add(new Paragraph("Hourly Rate").setFont(titleFont)));
//            table.addHeaderCell(new Cell().add(new Paragraph("Total Amount").setFont(titleFont)));
//
//            // Add employee data to the table
//            for (EmployeeClientInfo info : employeeClientInfos) {
//                Employee employee = info.getEmployee();
//                Integer workHours = info.getTotalBillableHours(); // Assuming EmployeeClientInfo has getTotalWorkHours()
//                Integer hourlyRate = info.getHourlyRate(); // Assuming EmployeeClientInfo has getHourlyRate()
//
//                table.addCell(new Cell().add(new Paragraph(employee.getFirstName() + " " + employee.getLastName()).setFont(regularFont)));
//                table.addCell(new Cell().add(new Paragraph(workHours.toString()).setFont(regularFont)));
//                table.addCell(new Cell().add(new Paragraph(hourlyRate.toString()).setFont(regularFont)));
//                table.addCell(new Cell().add(new Paragraph(String.valueOf(workHours * hourlyRate)).setFont(regularFont)));
//            }
//
//            // Add the table to the document
//            document.add(table);
//
//            // Add blank line for spacing before total
//            document.add(new Paragraph("\n"));
//
//            // Add Total Amount
//            document.add(new Paragraph("Total Invoice Amount: " + invoice.getTotalAmount()).setFont(regularFont));
//
//            // Add blank line for spacing before bank details
//            document.add(new Paragraph("\n"));
//
//            // Add Bank Account Information
//            document.add(new Paragraph("Bank Account Details:").setFont(titleFont));
//            document.add(new Paragraph("Account No: 10095409575").setFont(regularFont));
//            document.add(new Paragraph("\n")); // Add blank line for spacing
//
//            // Close the document
//            document.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return new ByteArrayInputStream(out.toByteArray());
//    }

//    public ByteArrayInputStream generateInvoicePdf(Invoice invoice, List<EmployeeClientInfo> employeeClientInfos) {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        try {
//            PdfWriter writer = new PdfWriter(out);
//            PdfDocument pdfDoc = new PdfDocument(writer);
//            Document document = new Document(pdfDoc);
//
//            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//
//            // Company Details with Logo
//            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 4}))
//                    .setWidth(UnitValue.createPercentValue(100));
//
//            // Add logo to the left column
//            try {
//                ImageData logo = ImageDataFactory.create("/Users/dq-mac-m2-1/Downloads/logo  12.jpeg"); // Replace with the actual logo path
//                Image image = new Image(logo);
//                image.setWidth(200); // Set the desired width (e.g., 100 points)
//                image.setHeight(50); // Set the desired height (e.g., 50 points)
//// OR
//                image.scaleToFit(200, 100); // Adjust size as needed
//                headerTable.addCell(new Cell().add(image).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//
////                headerTable.addCell(new Cell().add(image).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//            } catch (Exception e) {
//                // If the logo fails to load, fallback to a placeholder
//                headerTable.addCell(new Cell().add(new Paragraph("LOGO").setFont(titleFont)).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//            }
//
//            // Add company details to the right column
//            Table companyTable = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100));
//            companyTable.addCell(new Cell().add(new Paragraph("DIGIQUAD SOLUTIONS PRIVATE LIMITED").setFont(titleFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
//            companyTable.addCell(new Cell().add(new Paragraph("3-8-71, Bodrai Bazar, Near Ramalayam\nSuryapet, Hyderabad, Telangana 508213").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
//            companyTable.addCell(new Cell().add(new Paragraph("GSTIN: 36AAKCD0977E1ZK").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
//            companyTable.addCell(new Cell().add(new Paragraph("Contact: 8790922288").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
//            companyTable.addCell(new Cell().add(new Paragraph("Email: accounts@digiquadsolutions.com").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
//
//            headerTable.addCell(new Cell().add(companyTable).setBorder(Border.NO_BORDER));
//
//            document.add(headerTable);
//
//            // Invoice Header
//            document.add(new Paragraph("\n"));
//            document.add(new Paragraph("INVOICE")
//                    .setFont(titleFont)
//                    .setFontSize(18)
//                    .setTextAlignment(TextAlignment.CENTER));
//            document.add(new Paragraph("Invoice No.: " + invoice.getInvoiceNo()).setFont(regularFont));
//            document.add(new Paragraph("Invoice Date: " + invoice.getRaisedDate()).setFont(regularFont));
//            document.add(new Paragraph("Due Date: " + invoice.getDueDate()).setFont(regularFont));
//            document.add(new Paragraph("\n"));
//
//            // Client Details
//            document.add(new Paragraph("Bill To:").setFont(titleFont));
//            document.add(new Paragraph(invoice.getClient().getClientName() + "\n" + invoice.getClient().getClientAddress()).setFont(regularFont));
//            document.add(new Paragraph("\n"));
//
//            // Employee Details Table
//            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2, 2}));
//            table.setWidth(UnitValue.createPercentValue(100));
//            table.addHeaderCell(new Cell().add(new Paragraph("S. No.").setFont(titleFont)));
//            table.addHeaderCell(new Cell().add(new Paragraph("Description").setFont(titleFont)));
//            table.addHeaderCell(new Cell().add(new Paragraph("HSN/SAC").setFont(titleFont)));
//            table.addHeaderCell(new Cell().add(new Paragraph("No. of Hours").setFont(titleFont)));
//            table.addHeaderCell(new Cell().add(new Paragraph("Rate per Hour").setFont(titleFont)));
//            table.addHeaderCell(new Cell().add(new Paragraph("Billable Amount").setFont(titleFont)));
//
//            // Populate table rows
//            int serialNo = 1;
//            for (EmployeeClientInfo info : employeeClientInfos) {
//                Employee employee = info.getEmployee();
//                int hoursWorked = info.getTotalBillableHours();
//                int ratePerHour = info.getHourlyRate();
//                int totalAmount = hoursWorked * ratePerHour;
//
//                table.addCell(new Cell().add(new Paragraph(String.valueOf(serialNo++)).setFont(regularFont)));
//                table.addCell(new Cell().add(new Paragraph(employee.getFirstName() + " " + employee.getLastName()).setFont(regularFont)));
//                table.addCell(new Cell().add(new Paragraph("998311").setFont(regularFont))); // Example HSN/SAC
//                table.addCell(new Cell().add(new Paragraph(String.valueOf(hoursWorked)).setFont(regularFont)));
//                table.addCell(new Cell().add(new Paragraph(String.valueOf(ratePerHour)).setFont(regularFont)));
//                table.addCell(new Cell().add(new Paragraph(String.valueOf(totalAmount)).setFont(regularFont)));
//            }
//
//            document.add(table);
//
//            // Add totals
//            document.add(new Paragraph("\n"));
//            //document.add(new Paragraph("Sub Total: $ " + invoice.getSubTotal()).setFont(regularFont));
//            document.add(new Paragraph("Total:  " + invoice.getTotalAmount()).setFont(titleFont));
//            //document.add(new Paragraph("Balance Due: $ " + invoice.getBalanceDue()).setFont(titleFont));
//            //document.add(new Paragraph("Total In Words: " + invoice.getTotalInWords()).setFont(regularFont));
//            document.add(new Paragraph("\n"));
//
//            // Bank Account Details
//            document.add(new Paragraph("Bank Account Details:").setFont(titleFont));
//            document.add(new Paragraph("DIGIQUAD SOLUTIONS PRIVATE LIMITED\nAccount No: 10163323012\nIDFC FIRST BANK\nIFSC Code: IDFB0080226").setFont(regularFont));
//
//            // Close document
//            document.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return new ByteArrayInputStream(out.toByteArray());
//    }



//    public ByteArrayInputStream generateInvoicePdf(Invoice invoice, List<EmployeeClientInfo> employeeClientInfos) {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        try {
//            PdfWriter writer = new PdfWriter(out);
//            PdfDocument pdfDoc = new PdfDocument(writer);
//            Document document = new Document(pdfDoc);
//
//            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//
//            // Section: Company Details with Logo
//            Table headerSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1));
//
//            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 4}))
//                    .setWidth(UnitValue.createPercentValue(100));
//
//            // Add logo to the left column (Make the image bigger)
//            // Add logo to the left column
//            try {
//                ImageData logo = ImageDataFactory.create("/Users/dq-mac-m2-1/Downloads/logo  12.jpeg"); // Replace with the actual logo path
//                Image image = new Image(logo);
//                image.setWidth(200); // Set the desired width (e.g., 100 points)
//                image.setHeight(50); // Set the desired height (e.g., 50 points)
//// OR
//                image.scaleToFit(100, 100); // Adjust size as needed
//                headerTable.addCell(new Cell().add(image).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//
////                headerTable.addCell(new Cell().add(image).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//            } catch (Exception e) {
//                // If the logo fails to load, fallback to a placeholder
//                headerTable.addCell(new Cell().add(new Paragraph("LOGO").setFont(titleFont)).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//            }
//
//            // Add company details to the right column
//            headerTable.addCell(new Cell()
//                    .add(new Paragraph("DIGIQUAD SOLUTIONS PRIVATE LIMITED").setFont(titleFont))
//                    .add(new Paragraph("3-8-71, Bodrai Bazar, Near Ramalayam\nSuryapet, Hyderabad, Telangana 508213").setFont(regularFont))
//                    .add(new Paragraph("GSTIN: 36AAKCD0977E1ZK").setFont(regularFont))
//                    .add(new Paragraph("Contact: 8790922288").setFont(regularFont))
//                    .add(new Paragraph("Email: accounts@digiquadsolutions.com").setFont(regularFont))
//                    .setTextAlignment(TextAlignment.RIGHT)
//                    .setBorder(Border.NO_BORDER));
//            headerSection.addCell(new Cell().add(headerTable).setBorder(Border.NO_BORDER));
//            document.add(headerSection);
//
//            // Section: Invoice Details
//            Table invoiceSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1))
//                    .setMarginTop(10);
//
//            invoiceSection.addCell(new Cell().add(new Paragraph("INVOICE")
//                    .setFont(titleFont)
//                    .setFontSize(18)
//                    .setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
//            invoiceSection.addCell(new Cell().add(new Paragraph("Invoice No.: " + invoice.getInvoiceNo()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            invoiceSection.addCell(new Cell().add(new Paragraph("Invoice Date: " + invoice.getRaisedDate()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            invoiceSection.addCell(new Cell().add(new Paragraph("Due Date: " + invoice.getDueDate()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            document.add(invoiceSection);
//
//            // Section: Client Details
//            Table clientSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1))
//                    .setMarginTop(10);
//
//            clientSection.addCell(new Cell().add(new Paragraph("Bill To:").setFont(titleFont)).setBorder(Border.NO_BORDER));
//            clientSection.addCell(new Cell().add(new Paragraph(invoice.getClient().getClientName() + "\n" + invoice.getClient().getClientAddress()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            document.add(clientSection);
//
//            // Section: Employee Details Table
//            Table employeeSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1))
//                    .setMarginTop(10);
//
//            Table employeeTable = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2, 2}))
//                    .setWidth(UnitValue.createPercentValue(100));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("S. No.").setFont(titleFont)));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("Description").setFont(titleFont)));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("HSN/SAC").setFont(titleFont)));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("No. of Hours").setFont(titleFont)));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("Rate per Hour").setFont(titleFont)));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("Billable Amount").setFont(titleFont)));
//
//            int serialNo = 1;
//            for (EmployeeClientInfo info : employeeClientInfos) {
//                Employee employee = info.getEmployee();
//                int hoursWorked = info.getTotalBillableHours();
//                int ratePerHour = info.getHourlyRate();
//                int totalAmount = hoursWorked * ratePerHour;
//
//                employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(serialNo++)).setFont(regularFont)));
//                employeeTable.addCell(new Cell().add(new Paragraph(employee.getFirstName() + " " + employee.getLastName()).setFont(regularFont)));
//                employeeTable.addCell(new Cell().add(new Paragraph("998311").setFont(regularFont))); // Example HSN/SAC
//                employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(hoursWorked)).setFont(regularFont)));
//                employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(ratePerHour)).setFont(regularFont)));
//                employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(totalAmount)).setFont(regularFont)));
//            }
//            employeeSection.addCell(new Cell().add(employeeTable).setBorder(Border.NO_BORDER));
//            document.add(employeeSection);
//
//            // Section: Total Amount (Placed below Employee Details Table)
//            Table totalSection = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1))
//                    .setMarginTop(10);
//
//            // Left: Bank Details
//            Cell bankDetailsCell = new Cell()
//                    .add(new Paragraph("Bank Account Details:").setFont(titleFont).setPaddingBottom(5))
//                    .add(new Paragraph("DIGIQUAD SOLUTIONS PRIVATE LIMITED").setFont(regularFont))
//                    .add(new Paragraph("Account No: 10163323012").setFont(regularFont))
//                    .add(new Paragraph("IDFC FIRST BANK").setFont(regularFont))
//                    .add(new Paragraph("IFSC Code: IDFB0080226").setFont(regularFont))
//                    .setBorder(Border.NO_BORDER)
//                    .setTextAlignment(TextAlignment.LEFT);
//            totalSection.addCell(bankDetailsCell);
//
//            // Right: Total Amount
//            Cell totalAmountCell = new Cell()
//                    .add(new Paragraph("Total: " + invoice.getTotalAmount())
//                            .setFont(titleFont)
//                            .setTextAlignment(TextAlignment.RIGHT))
//                    .setBorder(Border.NO_BORDER);
//            totalSection.addCell(totalAmountCell);
//
//            document.add(totalSection);
//
//            // Close document
//            document.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return new ByteArrayInputStream(out.toByteArray());
//    }



    public ByteArrayInputStream generateInvoicePdf(Invoice invoice, List<EmployeeClientInfo> employeeClientInfos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            PageSize customPageSize = new PageSize(PageSize.A4.getWidth() + 200, PageSize.A4.getHeight()); // Add 100 units to the width
            pdfDoc.setDefaultPageSize(customPageSize);

            Document document = new Document(pdfDoc);

            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Example condition: Check if the client is not from India
//            boolean isClientOutsideIndia = !invoice.getClient().getClientCountry().equalsIgnoreCase("India");
//
//            if (isClientOutsideIndia) {
//                String supplementText = "SUPPLY MEANT FOR EXPORT UNDER BOND OR LETTER OF UNDERTAKING WITHOUT PAYMENT OF INTEGRATED TAX " +
//                        "(FINANCIAL YEAR FY 2024-25, LUT ARN No. AD360624028565X)";
//                Paragraph supplementParagraph = new Paragraph(supplementText)
//                        .setFont(regularFont)
//                        .setFontSize(10)
//                        .setTextAlignment(TextAlignment.CENTER)
//                        .setMarginBottom(10);
//
//                // Add the supplement paragraph to the document
//                document.add(supplementParagraph);
//            }

            // Section: Company Details with Logo
            Table headerSection = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER);

            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 4}))
                    .setWidth(UnitValue.createPercentValue(100));

            try {
                // Load the image from the classpath
                InputStream imageStream = getClass().getResourceAsStream("/images/logo  12 (2).jpeg"); // Adjust the path as per your resource directory structure

                if (imageStream == null) {
                    throw new IllegalArgumentException("Image not found in classpath");
                }

                // Create ImageData from the InputStream
                ImageData logo = ImageDataFactory.create(imageStream.readAllBytes());
                Image image = new Image(logo);
                image.scaleToFit(220, 220);

                // Add the image to the header table
                headerTable.addCell(
                        new Cell()
                                .add(image)
                                .setBorder(Border.NO_BORDER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );
            } catch (Exception e) {
                // Add fallback text if the image fails to load
                headerTable.addCell(
                        new Cell()
                                .add(new Paragraph("LOGO").setFont(titleFont))
                                .setBorder(Border.NO_BORDER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );
            }

//            Color customBlue = new DeviceRgb(173, 216, 230); // RGB for light blue


            headerTable.addCell(new Cell()
                    .add(new Paragraph("DIGIQUAD SOLUTIONS PRIVATE LIMITED").setFont(titleFont).setFontSize(20).setBold())
                    .add(new Paragraph("3-8-71, Bodrai Bazar, Near Ramalayam\nSuryapet, Hyderabad, Telangana 508213").setFont(regularFont).setFontSize(15))
//                    .add(new Paragraph("GSTIN: 36AAKCD0977E1ZK").setFont(regularFont).setFontSize(15))
                    .add(new Paragraph("Contact: 8790922288").setFont(regularFont).setFontSize(15))
                    .add(new Paragraph("Email: accounts@digiquadsolutions.com").setFont(regularFont).setFontSize(15))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER));
            headerSection.addCell(new Cell().add(headerTable).setBorder(Border.NO_BORDER));
            document.add(headerSection);


            Color customBlue = new DeviceRgb(65, 105, 225);

            // Section: Invoice Details
//            Table invoiceSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(ColorConstants.BLUE, 1))
//                    .setMarginTop(10);
//
//            invoiceSection.addCell(new Cell().add(new Paragraph("INVOICE")
//                    .setFont(titleFont)
//                    .setFontSize(18)
//                    .setFontColor(customBlue)
//                            .setBold()
//                    .setTextAlignment(TextAlignment.CENTER)).setBorder(new SolidBorder(ColorConstants.BLUE,1)).setPadding(10));
////            invoiceSection.addCell(new Cell().add(new Paragraph("Invoice No.: " + invoice.getInvoiceNo()).setFont(regularFont)).setBorder(Border.NO_BORDER).setPadding(10)));
//            invoiceSection.addCell(new Cell()
//                    .add(new Paragraph("Invoice No.: " + invoice.getInvoiceNo())
//                            .setFont(regularFont))
//                    .setBorder(Border.NO_BORDER)
//                    .setPaddingLeft(10));
////            invoiceSection.addCell(new Cell().add(new Paragraph("Invoice Date: " + invoice.getRaisedDate()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            invoiceSection.addCell(new Cell()
//                    .add(new Paragraph("Invoice Date: " + invoice.getRaisedDate())
//                            .setFont(regularFont))
//                    .setBorder(Border.NO_BORDER)
//                    .setPaddingLeft(10));
////            invoiceSection.addCell(new Cell().add(new Paragraph("Due Date: " + invoice.getDueDate()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            invoiceSection.addCell(new Cell()
//                    .add(new Paragraph("Due Date: " + invoice.getDueDate())
//                            .setFont(regularFont))
//                    .setBorder(Border.NO_BORDER)
//                    .setPaddingLeft(10));
////            invoiceSection.addCell(new Cell().add(new Paragraph("Cgst: " + invoice.getClient().getGstNumber()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//
//            document.add(invoiceSection);


            // Outer Table for the Invoice Section
//            Table invoiceSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(ColorConstants.BLUE, 1))
//                    .setMarginTop(10);
//
//// Invoice Header
//            invoiceSection.addCell(new Cell()
//                    .add(new Paragraph("INVOICE")
//                            .setFont(titleFont)
//                            .setFontSize(18)
//                            .setFontColor(ColorConstants.BLUE)
//                            .setBold()
//                            .setTextAlignment(TextAlignment.CENTER))
//                    .setBorder(new SolidBorder(ColorConstants.BLUE, 1))
//                    .setPadding(10));
//
//// Inner Table for Two Sections
//            Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})) // Two equal columns
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(Border.NO_BORDER);
//
//// Left Section: Customer Information
//            detailsTable.addCell(new Cell()
//                    .add(new Paragraph("Customer Information")
//                            .setFont(titleFont)
//                            .setFontSize(12)
//                            .setBold())
//                    .setBorder(Border.NO_BORDER)
//                    .setPadding(5));
//
//            detailsTable.addCell(new Cell()
//                    .add(new Paragraph("Invoice No.: " + invoice.getInvoiceNo())
//                            .setFont(regularFont))
//                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
//                    .setPaddingLeft(10));
//
//            detailsTable.addCell(new Cell()
//                    .add(new Paragraph("Customer Name: " + invoice.getClient().getClientName())
//                            .setFont(regularFont))
//                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
//                    .setPadding(5));
//
//            detailsTable.addCell(new Cell()
//                    .add(new Paragraph("Invoice Date: " + invoice.getRaisedDate())
//                            .setFont(regularFont))
//                    .setBorder(Border.NO_BORDER)
//                    .setPaddingLeft(10));
//
//            detailsTable.addCell(new Cell()
//                    .add(new Paragraph("Customer Address: " + invoice.getClient().getClientAddress())
//                            .setFont(regularFont))
//                    .setBorder(Border.NO_BORDER)
//                    .setPadding(5));
//
//            detailsTable.addCell(new Cell()
//                    .add(new Paragraph("Due Date: " + invoice.getDueDate())
//                            .setFont(regularFont))
//                    .setBorder(Border.NO_BORDER)
//                    .setPaddingLeft(10));
//
//// Add Inner Table to Outer Table
//            invoiceSection.addCell(new Cell()
//                    .add(detailsTable)
//                    .setBorder(Border.NO_BORDER)
//                    .setPadding(10));
//
//// Add the Invoice Section to the Document
//            document.add(invoiceSection);



            // Outer Table for the Invoice Section
            Table invoiceSection = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(ColorConstants.BLUE, 1))
                    .setMarginTop(10);

// Invoice Header
            invoiceSection.addCell(new Cell()
                    .add(new Paragraph("INVOICE")
                            .setFont(titleFont)
                            .setFontSize(18)
                            .setFontColor(ColorConstants.BLUE)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBorder(new SolidBorder(ColorConstants.BLUE, 1))
                    .setPadding(2));

            boolean isClientOutsideIndia = !invoice.getClient().getClientCountry().equalsIgnoreCase("India");

            if (isClientOutsideIndia) {
                String supplementText = "SUPPLY MEANT FOR EXPORT UNDER BOND OR LETTER OF UNDERTAKING WITHOUT PAYMENT OF INTEGRATED TAX " +
                        "(FINANCIAL YEAR FY 2024-25, LUT ARN No. AD360624028565X)";

                // Add the supplementary information as a new cell
                invoiceSection.addCell(new Cell()
                        .add(new Paragraph(supplementText)
                                .setFont(regularFont)
                                .setFontSize(10)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(new SolidBorder(ColorConstants.BLUE, 1))
                        .setPadding(5));
            }

// Inner Table for Two Sections (Customer and Invoice Information)
            Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})) // Two equal columns
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER);

// Left Column: Customer Information
            Cell customerDetails = new Cell()
                    .add(new Paragraph("Customer Details")
                            .setFont(titleFont)
                            .setFontSize(18)
                            .setBold())
                    .add(new Paragraph("Customer Name: " + invoice.getClient().getClientName())
                            .setFont(regularFont))
                    .add(new Paragraph("Customer Address: " + invoice.getClient().getClientAddress())
                            .setFont(regularFont))
                    .add(new Paragraph("Customer Country: " + invoice.getClient().getClientCountry())
                            .setFont(regularFont))
                    .add(new Paragraph("Customer Gst In: " + invoice.getClient().getGstNumber())
                            .setFont(regularFont))
                    .setBorder(Border.NO_BORDER) // No border on other sides
                    .setBorderRight(new SolidBorder(ColorConstants.LIGHT_GRAY, 1)) // Border between sections
                    .setPadding(10);

            DateTimeFormatter ukDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

// Right Column: Invoice Information
            Cell invoiceDetails = new Cell()
                    .add(new Paragraph("Invoice Information")
                            .setFont(titleFont)
                            .setFontSize(18)
                            .setBold())
                    .add(new Paragraph("Invoice No.: " + invoice.getInvoiceNo())
                            .setFont(regularFont))
                    .add(new Paragraph("Invoice Date: " + invoice.getRaisedDate().format(ukDateFormat))
                            .setFont(regularFont))
                    .add(new Paragraph("Due Date: " + invoice.getDueDate().format(ukDateFormat))
                            .setFont(regularFont))
                    .add(new Paragraph("Currency: " + invoice.getClient().getCurrency())
                            .setFont(regularFont))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(10);

// Add Cells to Inner Table
            detailsTable.addCell(customerDetails);
            detailsTable.addCell(invoiceDetails);

// Add Inner Table to Outer Table
            invoiceSection.addCell(new Cell()
                    .add(detailsTable)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(10));

// Add the Invoice Section to the Document
            document.add(invoiceSection);



            // Section: Client Details
//            Table clientSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1))
//                    .setMarginTop(10);
//
//            clientSection.addCell(new Cell().add(new Paragraph("Bill To:").setFont(titleFont)).setBorder(Border.NO_BORDER));
//            clientSection.addCell(new Cell().add(new Paragraph(invoice.getClient().getClientName() + "\n" + invoice.getClient().getClientAddress()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            clientSection.addCell(new Cell().add(new Paragraph("GST IN: " + invoice.getClient().getGstNumber()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            document.add(clientSection);

            // Section: Employee Details Table
            Table employeeSection = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(ColorConstants.BLUE, 1))
                    .setMarginTop(10);

            float[] columnWidths = invoice.getClient().getClientCountry().equalsIgnoreCase("India") ?
                    new float[]{
                            0.8f, // S. No.
                            2.5f, // Description
                            1.2f, // HSN/SAC
                            1.2f, // No. of Hours
                            1.5f, // Rate per Hour
                            1.2f, // SGST (%)
                            1.5f, // SGST (Amount)
                            1.2f, // CGST (%)
                            1.5f, // CGST (Amount)
                            1.2f, // IGST (%)
                            1.5f, // IGST (Amount)
                            2.0f  // Billable Amount
                    } :
                    new float[]{1, 3, 2, 2, 2, 2};

// Create the table with dynamic column widths
            Table employeeTable = new Table(UnitValue.createPercentArray(columnWidths))
                    .setBorder(new SolidBorder(ColorConstants.BLUE, 1))
                    .setWidth(UnitValue.createPercentValue(100));

// Add headers
            employeeTable.addHeaderCell(new Cell().add(new Paragraph("S. No.").setFont(titleFont)));
            employeeTable.addHeaderCell(new Cell().add(new Paragraph("Description").setFont(titleFont)));
            employeeTable.addHeaderCell(new Cell().add(new Paragraph("HSN/SAC").setFont(titleFont)));
            employeeTable.addHeaderCell(new Cell().add(new Paragraph("No. of Hours").setFont(titleFont)));
            employeeTable.addHeaderCell(new Cell().add(new Paragraph("Rate per Hour").setFont(titleFont)));
            if (invoice.getClient().getClientCountry().equalsIgnoreCase("India")) {
                employeeTable.addHeaderCell(new Cell().add(new Paragraph("SGST (%)").setFont(titleFont)));
                employeeTable.addHeaderCell(new Cell().add(new Paragraph("SGST (Amount)").setFont(titleFont)));
                employeeTable.addHeaderCell(new Cell().add(new Paragraph("CGST (%)").setFont(titleFont)));
                employeeTable.addHeaderCell(new Cell().add(new Paragraph("CGST (Amount)").setFont(titleFont)));
                employeeTable.addHeaderCell(new Cell().add(new Paragraph("IGST (%)").setFont(titleFont)));
                employeeTable.addHeaderCell(new Cell().add(new Paragraph("IGST (Amount)").setFont(titleFont)));
            }
            employeeTable.addHeaderCell(new Cell().add(new Paragraph("Billable Amount").setFont(titleFont)));

            int serialNo = 1;
            for (EmployeeClientInfo info : employeeClientInfos) {
                Employee employee = info.getEmployee();
                int hoursWorked = info.getTotalBillableHours();
                double ratePerHour = info.getHourlyRate();
                double totalAmount = hoursWorked * ratePerHour;

                double sgstPercent = 0, sgstAmount = 0;
                double cgstPercent = 0, cgstAmount = 0;
                double igstPercent = 0, igstAmount = 0;

                // Check client location and apply taxes accordingly
                if (invoice.getClient().getClientCountry().equalsIgnoreCase("India")) {
                    if (invoice.getClient().getState().equalsIgnoreCase("Telangana")) {
                        // Telangana: Apply SGST and CGST
                        sgstPercent = 9.0;
                        cgstPercent = 9.0;
                        sgstAmount = totalAmount * sgstPercent / 100;
                        cgstAmount = totalAmount * cgstPercent / 100;
                    } else {
                        // Other Indian states: Apply IGST
                        igstPercent = 18.0;
                        igstAmount = totalAmount * igstPercent / 100;
                    }
                }

                // Add employee details to the table
                employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(serialNo++)).setFont(regularFont)));
                employeeTable.addCell(new Cell().add(new Paragraph(employee.getFirstName() + " " + employee.getLastName()).setFont(regularFont)));
                employeeTable.addCell(new Cell().add(new Paragraph("998311").setFont(regularFont))); // Example HSN/SAC
                employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(hoursWorked)).setFont(regularFont)));
                employeeTable.addCell(new Cell().add(new Paragraph(invoice.getClient().getCurrency() + " " + String.valueOf(ratePerHour)).setFont(regularFont)));

                if (invoice.getClient().getClientCountry().equalsIgnoreCase("India")) {
                    // Add SGST details
                    employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(sgstPercent) + "%").setFont(regularFont)));
                    employeeTable.addCell(new Cell().add(new Paragraph(invoice.getClient().getCurrency() + " " + String.format("%.2f", sgstAmount)).setFont(regularFont)));

                    // Add CGST details
                    employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(cgstPercent) + "%").setFont(regularFont)));
                    employeeTable.addCell(new Cell().add(new Paragraph(invoice.getClient().getCurrency() + " " + String.format("%.2f", cgstAmount)).setFont(regularFont)));

                    // Add IGST details
                    employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(igstPercent) + "%").setFont(regularFont)));
                    employeeTable.addCell(new Cell().add(new Paragraph(invoice.getClient().getCurrency() + " " + String.format("%.2f", igstAmount)).setFont(regularFont)));
                }
                // Add Billable Amount
                employeeTable.addCell(new Cell().add(new Paragraph(invoice.getClient().getCurrency() + " " + String.valueOf(totalAmount)).setFont(regularFont)));
            }

// Add the employee table to the section
            employeeSection.addCell(new Cell().add(employeeTable).setBorder(Border.NO_BORDER));
            document.add(employeeSection);


//            // Section: Tax Details

            // Section: Total Amount After GST with Bank Details
            Table totalAfterGstSection = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(1))
                    .setMarginTop(10);

// Add the Total Amount row
            totalAfterGstSection.addCell(new Cell()
                    .add(new Paragraph("Sub Total:").setFont(titleFont))
                    .setBorder(Border.NO_BORDER));
            totalAfterGstSection.addCell(new Cell()
                    .add(new Paragraph(invoice.getTotalAmountAfterGst()).setFont(regularFont))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER));


            // Add "Amount in Words" row
            totalAfterGstSection.addCell(new Cell()
                    .add(new Paragraph("Amount in Words:").setFont(titleFont))
                    .setBorder(Border.NO_BORDER));

            totalAfterGstSection.addCell(new Cell()
                    .add(new Paragraph(invoice.getTotalAmountAfterGstInWords().toUpperCase() + " ONLY").setFont(regularFont))
                            .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER));

// Add the Bank Details row
            Cell bankDetailsCell = new Cell(1, 2) // Span across two columns
                    .add(new Paragraph("Bank Account Details:").setFont(titleFont).setPaddingBottom(5))
                    .add(new Paragraph("DIGIQUAD SOLUTIONS PRIVATE LIMITED").setFont(regularFont))
                    .add(new Paragraph("Account No: 10163323012").setFont(regularFont))
                    .add(new Paragraph("IDFC FIRST BANK").setFont(regularFont))
                    .add(new Paragraph("IFSC Code: IDFB0080226").setFont(regularFont))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.LEFT);
            totalAfterGstSection.addCell(bankDetailsCell);

            document.add(totalAfterGstSection);



            // Close document
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }






//    public ByteArrayInputStream generateInvoicePdf(Invoice invoice, List<EmployeeClientInfo> employeeClientInfos) {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        try {
//            PdfWriter writer = new PdfWriter(out);
//            PdfDocument pdfDoc = new PdfDocument(writer);
//            Document document = new Document(pdfDoc);
//
//            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//
//            // Section: Company Details with Logo
//            Table headerSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1));
//
//            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 4}))
//                    .setWidth(UnitValue.createPercentValue(100));
//
//            try {
//                ImageData logo = ImageDataFactory.create("/Users/dq-mac-m2-1/Downloads/logo  12.jpeg"); // Replace with the actual logo path
//                Image image = new Image(logo);
//                image.scaleToFit(100, 100); // Adjust size as needed
//                headerTable.addCell(new Cell().add(image).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//            } catch (Exception e) {
//                headerTable.addCell(new Cell().add(new Paragraph("LOGO").setFont(titleFont)).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));
//            }
//
//            headerTable.addCell(new Cell()
//                    .add(new Paragraph("DIGIQUAD SOLUTIONS PRIVATE LIMITED").setFont(titleFont))
//                    .add(new Paragraph("3-8-71, Bodrai Bazar, Near Ramalayam\nSuryapet, Hyderabad, Telangana 508213").setFont(regularFont))
//                    .add(new Paragraph("GSTIN: 36AAKCD0977E1ZK").setFont(regularFont))
//                    .add(new Paragraph("Contact: 8790922288").setFont(regularFont))
//                    .add(new Paragraph("Email: accounts@digiquadsolutions.com").setFont(regularFont))
//                    .setTextAlignment(TextAlignment.RIGHT)
//                    .setBorder(Border.NO_BORDER));
//            headerSection.addCell(new Cell().add(headerTable).setBorder(Border.NO_BORDER));
//            document.add(headerSection);
//
//            // Section: Invoice Details
//            Table invoiceSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1))
//                    .setMarginTop(10);
//
//            invoiceSection.addCell(new Cell().add(new Paragraph("INVOICE")
//                    .setFont(titleFont)
//                    .setFontSize(18)
//                    .setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
//            invoiceSection.addCell(new Cell().add(new Paragraph("Invoice No.: " + invoice.getInvoiceNo()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            invoiceSection.addCell(new Cell().add(new Paragraph("Invoice Date: " + invoice.getRaisedDate()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            invoiceSection.addCell(new Cell().add(new Paragraph("Due Date: " + invoice.getDueDate()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            document.add(invoiceSection);
//
//            // Section: Client Details
//            Table clientSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1))
//                    .setMarginTop(10);
//
//            clientSection.addCell(new Cell().add(new Paragraph("Bill To:").setFont(titleFont)).setBorder(Border.NO_BORDER));
//            clientSection.addCell(new Cell().add(new Paragraph(invoice.getClient().getClientName() + "\n" + invoice.getClient().getClientAddress()).setFont(regularFont)).setBorder(Border.NO_BORDER));
//            document.add(clientSection);
//
//            // Section: Employee Details Table
//            Table employeeSection = new Table(UnitValue.createPercentArray(new float[]{1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1))
//                    .setMarginTop(10);
//
//            Table employeeTable = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2, 2}))
//                    .setWidth(UnitValue.createPercentValue(100));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("S. No.").setFont(titleFont)));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("Description").setFont(titleFont)));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("HSN/SAC").setFont(titleFont)));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("No. of Hours").setFont(titleFont)));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("Rate per Hour").setFont(titleFont)));
//            employeeTable.addHeaderCell(new Cell().add(new Paragraph("Billable Amount").setFont(titleFont)));
//
//            int serialNo = 1;
//            for (EmployeeClientInfo info : employeeClientInfos) {
//                Employee employee = info.getEmployee();
//                int hoursWorked = info.getTotalBillableHours();
//                int ratePerHour = info.getHourlyRate();
//                int totalAmount = hoursWorked * ratePerHour;
//
//                employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(serialNo++)).setFont(regularFont)));
//                employeeTable.addCell(new Cell().add(new Paragraph(employee.getFirstName() + " " + employee.getLastName()).setFont(regularFont)));
//                employeeTable.addCell(new Cell().add(new Paragraph("998311").setFont(regularFont))); // Example HSN/SAC
//                employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(hoursWorked)).setFont(regularFont)));
//                employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(ratePerHour)).setFont(regularFont)));
//                employeeTable.addCell(new Cell().add(new Paragraph(String.valueOf(totalAmount)).setFont(regularFont)));
//            }
//            employeeSection.addCell(new Cell().add(employeeTable).setBorder(Border.NO_BORDER));
//            document.add(employeeSection);
//
//            // Section: Total Amount and Bank Details
//            Table totalSection = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
//                    .setWidth(UnitValue.createPercentValue(100))
//                    .setBorder(new SolidBorder(1))
//                    .setMarginTop(10);
//
//            // Left: Bank Details
//            Cell bankDetailsCell = new Cell()
//                    .add(new Paragraph("Bank Account Details:").setFont(titleFont).setPaddingBottom(5))
//                    .add(new Paragraph("DIGIQUAD SOLUTIONS PRIVATE LIMITED").setFont(regularFont))
//                    .add(new Paragraph("Account No: 10163323012").setFont(regularFont))
//                    .add(new Paragraph("IDFC FIRST BANK").setFont(regularFont))
//                    .add(new Paragraph("IFSC Code: IDFB0080226").setFont(regularFont))
//                    .setBorder(Border.NO_BORDER)
//                    .setTextAlignment(TextAlignment.LEFT);
//            totalSection.addCell(bankDetailsCell);
//
//            // Right: Total Amount and Amount in Words
//// Sanitize the total amount to remove "Rs " and trim any extra spaces
//            String sanitizedAmount = invoice.getTotalAmount().replace("Rs ", "").trim();
//
//// Convert the sanitized amount to a double
//            double amount = Double.parseDouble(sanitizedAmount);
//
//// Convert the amount to words using your helper method
//            String amountInWords = convertAmountToWords(amount);
//            Cell totalAmountCell = new Cell()
//                    .add(new Paragraph("Total: " + invoice.getTotalAmount())
//                            .setFont(titleFont)
//                            .setTextAlignment(TextAlignment.RIGHT))
//                    .add(new Paragraph("Amount in Words: " + amountInWords)
//                            .setFont(regularFont)
//                            .setTextAlignment(TextAlignment.RIGHT))
//                    .setBorder(Border.NO_BORDER);
//            totalSection.addCell(totalAmountCell);
//
//            document.add(totalSection);
//
//            // Close document
//            document.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return new ByteArrayInputStream(out.toByteArray());
//    }

    // Helper Method: Convert Amount to Words
    private String convertAmountToWords(double amount) {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(Locale.US, RuleBasedNumberFormat.SPELLOUT);
        return formatter.format(amount);
    }





    public static InvoiceDto toInvoiceDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setClientId(invoice.getClient().getId());
        dto.setClientName(invoice.getClient().getClientName());
        dto.setRaisedDate(invoice.getRaisedDate());
        dto.setMonth(invoice.getMonth());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setInvoiceNo(invoice.getInvoiceNo());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus());
        dto.setComments(invoice.getComments());
        dto.setInInr(invoice.getInInr());
        dto.setTransactionId(invoice.getTransactionId());
        return dto;
    }

    // Method to convert a list of Invoice entities to a list of InvoiceDto
    public static List<InvoiceDto> toInvoiceDtoList(List<Invoice> invoices) {
        return invoices.stream()
                .map(InvoiceService::toInvoiceDto)
                .collect(Collectors.toList());
    }

//    public void saveInvoicePdf(Invoice invoice, byte[] pdfBytes) {
//        // Save to database
//        invoice.setPdfData(pdfBytes);
//        invoiceRepository.save(invoice);
//
//    }

    public Map<String, Long> getInvoiceStatusCounts() {
        List<Object[]> statusCounts = invoiceRepository.countInvoicesByStatus();
        Map<String, Long> result = new HashMap<>();

        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            result.put(status, count);
        }

        return result;
    }

    public Map<String, Double> getTotalAmounts() {
        // Initialize totals
        double totalPaid = 0;
        double totalPending = 0;

        // Calculate total paid
        List<Invoice> paidInvoices = invoiceRepository.findByStatus("PAID");
        for (Invoice invoice : paidInvoices) {
            totalPaid += parseAmount(invoice.getInInr());
        }

        // Calculate total pending
        List<Invoice> pendingInvoices = invoiceRepository.findByStatus("PENDING");
        for (Invoice invoice : pendingInvoices) {
            totalPending += parseAmount(invoice.getInInr());
        }

        // Prepare the response map
        Map<String, Double> amounts = new HashMap<>();
        amounts.put("totalPaid", totalPaid);
        amounts.put("totalPending", totalPending);

        return amounts;
    }

    private double parseAmount(String amount) {
        try {
            // Trim and sanitize the input
            String sanitizedAmount = amount.trim().replaceAll("[^\\d.]", ""); // Remove any non-numeric characters except '.'
            return Double.parseDouble(sanitizedAmount); // Convert sanitized String to double
        } catch (NumberFormatException e) {
            // Log the error for debugging purposes
            System.err.println("Invalid number format: " + amount);
            return 0.0; // Return 0 if the string can't be parsed
        }
    }



}