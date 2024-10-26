package com.dq.empportal.service;

import com.dq.empportal.model.Client;
import com.dq.empportal.model.Employee;
import com.dq.empportal.model.Invoice;
import com.dq.empportal.repository.ClientRepository;
import com.dq.empportal.repository.EmployeeClientInfoRepository;
import com.dq.empportal.repository.InvoiceRepository;
import com.dq.empportal.model.EmployeeClientInfo;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.*;
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
//
//@Service
//public class InvoiceService {
//
//    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
//    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//    @Autowired
//    private InvoiceRepository invoiceRepository;
//
//    public List<Invoice> getInvoicesByYear(int year) {
//        // Start of the year
//        String startDate = year + "-01-01T00:00:00";
//        // Start of the next year (to include all dates in the current year)
//        String endDate = (year + 1) + "-01-01T00:00:00";
//
//        // Convert Strings to LocalDateTime
//        LocalDate startDateTime = LocalDate.parse(startDate);
//        LocalDate endDateTime = LocalDate.parse(endDate);
//
//        // Call repository with LocalDateTime arguments
//        return invoiceRepository.findByRaisedOnBetween(startDateTime, endDateTime);
//    }
//
//
//    public List<Invoice> getInvoicesByMonth(int year, int month) {
//        // Format the start of the month
//        String startDate = String.format("%d-%02d-01T00:00:00", year, month);
//
//        // Convert the start date to LocalDate
//        LocalDate startLocalDate = LocalDate.parse(startDate.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//
//        // Get the last day of the month and convert to String with time
//        String endDate = startLocalDate.withDayOfMonth(startLocalDate.lengthOfMonth()).plusDays(1).toString() + "T00:00:00";
//
//        // Convert to LocalDateTime
//        LocalDate startDateTime = LocalDate.parse(startDate);
//        LocalDate endDateTime = LocalDate.parse(endDate);
//
//        // Call repository with LocalDateTime arguments
//        return invoiceRepository.findByRaisedOnBetween(startDateTime, endDateTime);
//    }
//
//
//    public void deleteInvocieById(Integer id){
//        if(invoiceRepository.existsById(id)){
//            invoiceRepository.deleteById(id);
//        }else {
//            // throw new ClientNotFoundException("Client with id " + id + " not found");
//        }
//    }
//
//
//    public List<Invoice> getPendingInvoices() {
//        LocalDate currentDateTime = LocalDate.now(); // Current date and time
//        // Fetch pending invoices based on the current date
//        LocalDate currentDate = LocalDate.now();
//        String currentDateString = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
//        List<Invoice> invoices = invoiceRepository.findPendingInvoices(currentDateString, currentDate);
//        return invoices;
//    }
//
//
//    public List<Invoice> getPendingInvoicesByClientId(String clientId) {
//        LocalDate currentDateTime = LocalDate.now(); // Current date and time
//        LocalDate currentDate = LocalDate.now();
//        String currentDateString = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
//        List<Invoice> invoices = invoiceRepository.findPendingInvoices(currentDateString, currentDate); // Get pending invoices
//
//        // Filter invoices by client ID
//        List<Invoice> result = new ArrayList<>();
//        for (Invoice currentInvoice : invoices) {
//            if (currentInvoice.getClientId().equals(clientId)) {
//                result.add(currentInvoice);
//            }
//        }
//        return result;
//    }
//
//    public List<Invoice> getInvoiceCreatedToday() {
//        LocalDate todayStart = LocalDate.now().with(LocalTime.MIN);
//        return invoiceRepository.findInvoiceAddedToday(todayStart);
//    }
//
//    public List<Invoice> getInvoiceCreatedInWeek() {
//        LocalDate weekAgo = LocalDate.now().minus(1, ChronoUnit.WEEKS);
//        LocalDate now = LocalDate.now();
//        return invoiceRepository.findInvoiceWithinDateRange(weekAgo,now);
//
//    }
//
//    public List<Invoice> getInvoicesByClientId(Integer clientId){
//        return invoiceRepository.findByClientId(clientId);
//    }
//}

@Service
public class InvoiceService {
    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private EmployeeClientInfoRepository employeeClientInfoRepository;

    @Autowired
    private ClientRepository clientRepository;
    @PersistenceContext
    private EntityManager entityManager;

//    public Invoice generateMonthlyInvoice(Integer clientId) {
//        // Fetch the client using the provided clientId
//        Client client = clientRepository.findById(clientId)
//                .orElseThrow(() -> new RuntimeException("Client not found"));
//
//        // Define the start and end date for the current month
//        LocalDate startDate = LocalDate.now().withDayOfMonth(1); // First day of current month
//        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()); // Last day of current month
//
//        // Fetch all employees assigned to this client and working during the invoice period
//        List<EmployeeClientInfo> resources = employeeClientInfoRepository.findActiveEmployeesByClientForInvoice(client, startDate, endDate);
//
//        // Calculate total invoice amount
//        Double totalAmount = calculateTotalInvoiceAmount(resources, startDate, endDate);
//
//        // Create and save invoice
//        Invoice invoice = new Invoice();
//        invoice.setClient(client);
//        invoice.setStartDate(startDate);
//        invoice.setEndDate(endDate);
//        invoice.setTotalAmount(totalAmount);
//        invoice.setStatus("Unpaid"); // Set invoice status
//        // Set employee details in the invoice
//        List<Employee> employees = resources.stream()
//                .map(EmployeeClientInfo::getEmployee) // Assuming EmployeeClientInfo has getEmployee method
//                .collect(Collectors.toList());
//
//        invoice.setEmployees(employees);
//
//
//        return invoiceRepository.save(invoice);
//    }

//    public Invoice generateMonthlyInvoice(Integer clientId) {
//        // Fetch the client using the provided clientId
//        Client client = clientRepository.findById(clientId)
//                .orElseThrow(() -> new RuntimeException("Client not found"));
//
//        // Define the start and end date for the current month
//        LocalDate startDate = LocalDate.now().withDayOfMonth(1); // First day of current month
//        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()); // Last day of current month
//
//        // Fetch all employees assigned to this client and working during the invoice period
//        List<EmployeeClientInfo> resources = employeeClientInfoRepository.findActiveEmployeesByClientForInvoice(client, startDate, endDate);
//
//        // Calculate total invoice amount (after considering leaves)
//        Double totalAmount = calculateTotalInvoiceAmount(resources, startDate, endDate);
//
//        // Check if there is already an invoice for this client and period
//        Invoice existingInvoice = invoiceRepository.findByClientAndStartDateAndEndDate(client, startDate, endDate);
//
//        Invoice invoice;
//
//        if (existingInvoice != null) {
//            // Update the existing invoice if one already exists
//            invoice = existingInvoice;
//            invoice.clearEmployees(); // Clear previous employee associations to avoid duplicates
//        } else {
//            // Create a new invoice
//            invoice = new Invoice();
//            invoice.setClient(client);
//            invoice.setStartDate(startDate);
//            invoice.setEndDate(endDate);
//            invoice.setStatus("Unpaid");
//        }
//
//        // Set employee details in the invoice
//        List<Employee> employees = resources.stream()
//                .map(EmployeeClientInfo::getEmployee) // Assuming EmployeeClientInfo has getEmployee method
//                .collect(Collectors.toList());
//
//        invoice.setEmployees(employees);
//        invoice.setTotalAmount(totalAmount);
//
//        // Save the updated invoice with employee associations
//        invoice= invoiceRepository.save(invoice);
//        // Generate the invoice PDF
//        ByteArrayInputStream pdfStream = generateInvoicePdf(invoice, resources);
//
//        // Save or send the PDF as needed
//        // Example: you can save the PDF to a file system, or send it via email.
//
//        return invoice;
//    }


//    public Invoice generateMonthlyInvoice(Integer clientId) {
//        // Fetch the client using the provided clientId
//        Client client = clientRepository.findById(clientId)
//                .orElseThrow(() -> new RuntimeException("Client not found"));
//
//        // Define the start and end date for the current month
//        LocalDate startDate = LocalDate.now().withDayOfMonth(1); // First day of current month
//        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()); // Last day of current month
//
//        // Fetch all employees assigned to this client and working during the invoice period
//        List<EmployeeClientInfo> resources = employeeClientInfoRepository
//                .findActiveEmployeesByClientForInvoice(client, startDate, endDate);
//
//        // Calculate total invoice amount (after considering leaves)
//        Double totalAmount = calculateTotalInvoiceAmount(resources, startDate, endDate);
//
//        // Check if there is already an invoice for this client and period
//        Invoice existingInvoice = invoiceRepository.findByClientAndStartDateAndEndDate(client, startDate, endDate);
//
//        Invoice invoice;
//
//        if (existingInvoice != null) {
//            // Update the existing invoice if one already exists
//            invoice = existingInvoice;
//            invoice.getEmployeeClientInfos().clear(); // Clear previous employee-client info associations
//        } else {
//            // Create a new invoice
//            invoice = new Invoice();
//            invoice.setClient(client);
//            invoice.setStartDate(startDate);
//            invoice.setEndDate(endDate);
//            invoice.setStatus("Unpaid");
//        }
//
//        // Set employee-client info details in the invoice
//        invoice.setEmployeeClientInfos(resources); // Add the detailed EmployeeClientInfo objects to the invoice
//
//        invoice.setTotalAmount(totalAmount);
//
//        // Save the updated invoice with employee-client info associations
//        invoice = invoiceRepository.save(invoice);
//
//        // Generate the invoice PDF
//        ByteArrayInputStream pdfStream = generateInvoicePdf(invoice, resources);
//
//        // Save or send the PDF as needed
//        // Example: you can save the PDF to a file system, or send it via email.
//
//        return invoice;
//    }



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

        // Check if there is already an invoice for this client and period
        Invoice existingInvoice = invoiceRepository.findByClientAndStartDateAndEndDate(client, startDate, endDate);

        Invoice invoice;

        if (existingInvoice != null) {
            // Update the existing invoice if one already exists
            invoice = existingInvoice;

            // Ensure the employeeClientInfos list is initialized
            if (invoice.getEmployeeClientInfos() == null) {
                invoice.setEmployeeClientInfos(new ArrayList<>());
            } else {
                invoice.getEmployeeClientInfos().clear(); // Clear previous employee-client info associations
            }
        } else {
            // Create a new invoice if none exists
            invoice = new Invoice();
            invoice.setClient(client);
            invoice.setStartDate(startDate);
            invoice.setEndDate(endDate);
            invoice.setStatus("Unpaid");

            // Initialize the employeeClientInfos list
            invoice.setEmployeeClientInfos(new ArrayList<>());
        }

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
        invoice.setTotalAmount(totalAmount);

        // Save the updated invoice with employee-client info associations
        invoice = invoiceRepository.save(invoice);

        return invoice;
    }







//    private Double calculateTotalInvoiceAmount(List<EmployeeClientInfo> employees, LocalDate startDate, LocalDate endDate) {
//        double total = 0;
//
//        for (EmployeeClientInfo employee : employees) {
//            // Assuming employee.getHourlyRate() returns the hourly rate and getTotalBillableHours() returns the hours for the invoice period
//            double hourlyRate = employee.getHourlyRate();
//            int totalBillableHours = employee.getTotalBillableHours(startDate, endDate)-employee.getLeaveDay()*9; // You may need a method to calculate this based on the period
////
//            // Calculate the cost for this employee
//            total += hourlyRate * totalBillableHours;
//        }
//
//        return total;
//    }

//    private Double calculateTotalInvoiceAmount(List<EmployeeClientInfo> employees, LocalDate startDate, LocalDate endDate) {
//        double total = 0;
//
//        for (EmployeeClientInfo employee : employees) {
//            double hourlyRate = employee.getHourlyRate();
//            int totalBillableHours = employee.TotalBillableHours(startDate, endDate);
//
//            // Get the number of leave days within the invoice period
//            int leaveDaysWithinPeriod = employee.getLeaveDaysWithinPeriod(startDate, endDate);
//
//            // Assuming 9 hours per leave day, deduct from the total billable hours
//            totalBillableHours -= leaveDaysWithinPeriod * 9;
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
            //int leaveDaysWithinPeriod = employee.getLeaveDaysWithinPeriod(startDate, endDate);
            int leaveDaysWithinPeriod = employee.getEmployee().getLeaveDaysWithinPeriod(startDate,endDate);
            //int holidaysWithinPeriod = employee.getHolidaysWithinPeriod(startDate, endDate);
            int holidaysWithinPeriod=employee.getEmployee().getHolidaysWithinPeriod(startDate,endDate);
            //int nonBillableDaysWithinPeriod = employee.getNonBillableDaysWithinPeriod(startDate, endDate);
            int nonBillableDaysWithinPeriod=employee.getEmployee().getNonBillableDaysWithinPeriod(startDate,endDate);

            // Assuming 9 hours per day for leaves, holidays, and non-billable days, deduct from the total billable hours
            int hoursToDeduct = (leaveDaysWithinPeriod + holidaysWithinPeriod + nonBillableDaysWithinPeriod) * 9;
            totalBillableHours -= hoursToDeduct;

            // Ensure that billable hours are not negative
            totalBillableHours = Math.max(totalBillableHours, 0);
            employee.setTotalBillableHours(totalBillableHours);

            // Calculate the total cost for this employee
            total += hourlyRate * totalBillableHours;
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
//            Document document =new Document(pdfDoc);
//
//            // Load standard font
//            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
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
//            // Add Total Amount
//            document.add(new Paragraph("\n")); // Add blank line for spacing
//            document.add(new Paragraph("Total Invoice Amount: Rs " + invoice.getTotalAmount()).setFont(regularFont));
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



    public ByteArrayInputStream generateInvoicePdf(Invoice invoice, List<EmployeeClientInfo> employeeClientInfos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // Initialize PDF writer and document
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Load standard fonts
            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Create table for company details in the top right corner
            Table companyTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .setWidth(UnitValue.createPercentValue(40))
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT); // Align to top-right

            companyTable.addCell(new Cell().add(new Paragraph("DIGIQUAD SOLUTIONS").setFont(titleFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
            companyTable.addCell(new Cell().add(new Paragraph("Flat No.302, Sai Chandra Residency,\nOpp to Greenpeace, Puppalagud,\nManikonda, Serilingampally,\nRangareddy").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
            companyTable.addCell(new Cell().add(new Paragraph("Contact: +91- 8790922288").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
            companyTable.addCell(new Cell().add(new Paragraph("Email: info@digiquadsolutions.com").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));

            // Add the company details to the document
            document.add(companyTable);

            // Add a blank line for spacing
            document.add(new Paragraph("\n"));

            // Add Invoice title
            Paragraph title = new Paragraph("Invoice")
                    .setFont(titleFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Add client information
            document.add(new Paragraph("Client Name: " + invoice.getClient().getClientName()).setFont(regularFont));
            document.add(new Paragraph("Invoice Date: " + LocalDate.now()).setFont(regularFont));
            document.add(new Paragraph("Invoice Period: " + invoice.getStartDate() + " to " + invoice.getEndDate()).setFont(regularFont));
            document.add(new Paragraph("\n")); // Add blank line for spacing

            // Add table for Employee information
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Add table headers
            table.addHeaderCell(new Cell().add(new Paragraph("Employee Name").setFont(titleFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Work Hours").setFont(titleFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Hourly Rate").setFont(titleFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Total Amount").setFont(titleFont)));

            // Add employee data to the table
            for (EmployeeClientInfo info : employeeClientInfos) {
                Employee employee = info.getEmployee();
                Integer workHours = info.getTotalBillableHours(); // Assuming EmployeeClientInfo has getTotalWorkHours()
                Integer hourlyRate = info.getHourlyRate(); // Assuming EmployeeClientInfo has getHourlyRate()

                table.addCell(new Cell().add(new Paragraph(employee.getFirstName() + " " + employee.getLastName()).setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph(workHours.toString()).setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph(hourlyRate.toString()).setFont(regularFont)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(workHours * hourlyRate)).setFont(regularFont)));
            }

            // Add the table to the document
            document.add(table);

            // Add blank line for spacing before total
            document.add(new Paragraph("\n"));

            // Add Total Amount
            document.add(new Paragraph("Total Invoice Amount: Rs " + invoice.getTotalAmount()).setFont(regularFont));

            // Add blank line for spacing before bank details
            document.add(new Paragraph("\n"));

            // Add Bank Account Information
            document.add(new Paragraph("Bank Account Details:").setFont(titleFont));
            document.add(new Paragraph("Account No: 10095409575").setFont(regularFont));
            document.add(new Paragraph("\n")); // Add blank line for spacing

            // Close the document
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }


}