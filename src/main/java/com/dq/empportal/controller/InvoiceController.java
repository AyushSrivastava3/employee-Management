package com.dq.empportal.controller;


import com.dq.empportal.model.Client;
import com.dq.empportal.model.Invoice;
import com.dq.empportal.repository.ClientRepository;
import com.dq.empportal.repository.InvoiceRepository;
import com.dq.empportal.service.InvoiceService;
import com.dq.empportal.transform.InvoiceToInvoiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    InvoiceToInvoiceResponse invoiceToInvoiceResponse;

    @GetMapping("/clients")
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

//    @PostMapping("/create")
//    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
//        invoice.setCreatedDate(LocalDate.now());
//        Invoice savedInvoice = invoiceRepository.save(invoice);
//        return ResponseEntity.ok(savedInvoice);
//    }
//
//    @GetMapping("/all")
//    public List<Invoice> getAllInvoices() {
//        return invoiceRepository.findAll();
//    }
//
//    @GetMapping("/byYear")
//    public List<Invoice> getInvoicesByYear(@RequestParam int year) {
//        return invoiceService.getInvoicesByYear(year);
//    }
//
//    @GetMapping("/byMonth")
//    public List<Invoice> getInvoicesByMonth(@RequestParam int year, @RequestParam int month) {
//        return invoiceService.getInvoicesByMonth(year, month);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Integer id) {
//        Optional<Invoice> invoice = invoiceRepository.findById(id);
//        return invoice.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // New endpoint to update a specific invoice by its ID
//    @PutMapping("/{id}")
//    public ResponseEntity<Invoice> updateInvoice(@PathVariable Integer id, @RequestBody Invoice invoiceDetails) {
//        Optional<Invoice> optionalInvoice = invoiceRepository.findById(id);
//
//        if (optionalInvoice.isPresent()) {
//            Invoice invoice = optionalInvoice.get();
//            invoice.setInvoiceNumber(invoiceDetails.getInvoiceNumber());
//            invoice.setClientName(invoiceDetails.getClientName());
//            invoice.setInvoiceValue(invoiceDetails.getInvoiceValue());
//            invoice.setCurrency(invoiceDetails.getCurrency());
//            invoice.setMonth(invoiceDetails.getMonth());
//            invoice.setRaisedOn(invoiceDetails.getRaisedOn());
//            invoice.setTimeline(invoiceDetails.getTimeline());
//            Invoice updatedInvoice = invoiceRepository.save(invoice);
//            return ResponseEntity.ok(updatedInvoice);
//        }else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteInvoice(@PathVariable Integer id){
//        invoiceService.deleteInvocieById(id);
//        return ResponseEntity.noContent().build();
//    }
//
//
//
//    @GetMapping("/pending")
//    public ResponseEntity<List<Invoice>> getPendingInvoices() {
//        List<Invoice> invoices = invoiceService.getPendingInvoices();
//
//        if (invoices.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        } else {
//            return ResponseEntity.ok(invoices);
//        }
//    }
//
//    @GetMapping("/pending/clientInvoices")
//    public ResponseEntity<List<Invoice>> getPendingClientInvoices(@RequestParam String clientId) {
//        List<Invoice> invoices = invoiceService.getPendingInvoicesByClientId(clientId);
//        return ResponseEntity.ok(invoices);
//    }
//
//    @GetMapping("/export/invoices")
//    public ResponseEntity<byte[]> exportInvoicesToExcel() {
//        try {
//            List<Invoice> invoices = invoiceRepository.findAll();
//            ExportImportService<Invoice> exporter = new ExportImportService<>();
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            exporter.exportToExcel(invoices, baos);
//
//            byte[] excelBytes = baos.toByteArray();
//            System.out.println("Excel file size: " + excelBytes.length);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clients_export.xlsx");
//            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//
//            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(("Failed to create Excel file: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @PostMapping("/import/invoices")
//    public ResponseEntity<?> importInvoicesFromCSV(@RequestParam("file") MultipartFile file) {
//        try {
//            ExportImportService<Invoice> importer = new ExportImportService<>();
//            List<Invoice> invoices = importer.importFromCSV(file, Invoice.class);
//            invoiceRepository.saveAll(invoices);
//            return ResponseEntity.ok(invoices);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Map<String, String> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Failed to import CSV file: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(errorResponse);
//        }
//    }
//
//    @GetMapping("/getTodayInvoices")
//    public List<Invoice> getTodayInvoices() {
//        return invoiceService.getInvoiceCreatedToday();
//    }
//    @GetMapping("/getWeekInvoices")
//    public List<Invoice> getWeekInvoices() {
//        return invoiceService.getInvoiceCreatedInWeek();
//    }
//
//
//    @GetMapping("/byClient")
//    public ResponseEntity<List<Invoice>> getInvoicesByClientId(@RequestParam Integer clientId){
//        List<Invoice> invoices= invoiceService.getInvoicesByClientId(clientId);
//        if(invoices.isEmpty()){
//            return ResponseEntity.noContent().build();
//        }else {
//            return ResponseEntity.ok(invoices);
//        }
//    }
//
//
//    @GetMapping("/clients/invoices/export")
//    public ResponseEntity<byte[]> exportClientInvoicesToExcel(@RequestParam Integer clientId) {
//        try {
//            List<Invoice> invoices= invoiceService.getInvoicesByClientId(clientId);
//            ExportImportService<Invoice> exporter = new ExportImportService<>();
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            exporter.exportToExcel(invoices, baos);
//
//            byte[] excelBytes = baos.toByteArray();
//            System.out.println("Excel file size: " + excelBytes.length);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=profiles_export.xlsx");
//            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//
//            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(("Failed to create Excel file: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @GetMapping("/pending/clientInvoices/export")
//    public ResponseEntity<byte[]> exportPendingInvoicesToExcel(@RequestParam String clientId) {
//        try {
//            List<Invoice> invoices = invoiceService.getPendingInvoicesByClientId(clientId);
//            ExportImportService<Invoice> exporter = new ExportImportService<>();
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            exporter.exportToExcel(invoices, baos);
//
//            byte[] excelBytes = baos.toByteArray();
//            System.out.println("Excel file size: " + excelBytes.length);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=profiles_export.xlsx");
//            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//
//            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(("Failed to create Excel file: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }



//    @PostMapping("/generate")
//    public InvoiceResponseDto generateMonthlyInvoice(@RequestParam Integer clientId) {
//        return invoiceToInvoiceResponse.transform(invoiceService.generateMonthlyInvoice(clientId));
//    }

//    @PostMapping("/generate")
//    public ResponseEntity<ByteArrayResource> generateMonthlyInvoice(@RequestParam Integer clientId) {
//        // Fetch and generate the invoice
//        Invoice invoice = invoiceService.generateMonthlyInvoice(clientId);
//
//        // Generate the PDF stream (from your existing method)
//        ByteArrayInputStream pdfStream = invoiceService.generateInvoicePdf(invoice, invoice.getEmployeeClientInfos());
//
//        // Convert the ByteArrayInputStream to a ByteArrayResource
//        ByteArrayResource pdfResource = new ByteArrayResource(pdfStream.readAllBytes());
//
//        // Set HTTP headers for file download
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + clientId + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) + ".pdf");
//
//        // Return PDF file as a ResponseEntity
//        return ResponseEntity.ok()
//                .headers(headers)
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(pdfResource);
//    }




    @PostMapping("/generate")
    public ResponseEntity<ByteArrayResource> generateMonthlyInvoice(
            @RequestParam Integer clientId,
            @RequestParam(required = false) String yearMonth) {

        // If yearMonth is not provided, default to the current month
        YearMonth invoiceMonth;
        if (yearMonth == null || yearMonth.isEmpty()) {
            invoiceMonth = YearMonth.now();
        } else {
            // Parse the provided yearMonth (e.g., "2024-10")
            try {
                invoiceMonth = YearMonth.parse(yearMonth);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body(null); // Invalid date format
            }
        }

        // Fetch and generate the invoice for the given month
        Invoice invoice = invoiceService.generateMonthlyInvoice(clientId, invoiceMonth);

        // Generate the PDF stream (from your existing method)
        ByteArrayInputStream pdfStream = invoiceService.generateInvoicePdf(invoice, invoice.getEmployeeClientInfos());

        // Convert the ByteArrayInputStream to a ByteArrayResource
        ByteArrayResource pdfResource = new ByteArrayResource(pdfStream.readAllBytes());

        // Set HTTP headers for file download with the year-month in the filename
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=invoice_" + clientId + "_" + invoiceMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")) + ".pdf");

        // Return PDF file as a ResponseEntity
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfResource);
    }



}