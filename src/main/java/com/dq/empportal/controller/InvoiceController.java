package com.dq.empportal.controller;
import com.dq.empportal.dtos.InvoiceDto;
import com.dq.empportal.dtos.InvoiceUpdateRequest;
import com.dq.empportal.exception.DuplicateInvoiceException;
import com.dq.empportal.model.Client;
import com.dq.empportal.model.Invoice;
import com.dq.empportal.repository.ClientRepository;
import com.dq.empportal.repository.InvoiceRepository;
import com.dq.empportal.service.InvoiceService;
import com.dq.empportal.transform.InvoiceToInvoiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/invoices")
public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);
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

    @PutMapping("/{invoiceId}")
    @Transactional
    public ResponseEntity<Invoice> editInvoice(@PathVariable Integer invoiceId, @RequestBody InvoiceUpdateRequest invoiceUpdateRequest) {
        // Find the invoice by ID
        log.info("InvoiceUpdateRequest",invoiceUpdateRequest.getInvoiceNo(),invoiceUpdateRequest.getTotalAmount());
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Update the fields if they are provided
        if (invoiceUpdateRequest.getStatus() != null) {
            invoice.setStatus(invoiceUpdateRequest.getStatus());
        }
        if (invoiceUpdateRequest.getComments() != null) {
            invoice.setComments(invoiceUpdateRequest.getComments());
        }
        if (invoiceUpdateRequest.getTransactionId() != null) {
            invoice.setTransactionId(invoiceUpdateRequest.getTransactionId());
        }
        if (invoiceUpdateRequest.getRaisedDate() != null) {
            invoice.setRaisedDate(invoiceUpdateRequest.getRaisedDate());
        }
        if (invoiceUpdateRequest.getDueDate() != null) {
            invoice.setDueDate(invoiceUpdateRequest.getDueDate());
        }
        if (invoiceUpdateRequest.getTotalAmount() != null) {
            invoice.setTotalAmount(invoiceUpdateRequest.getTotalAmount());
        }
        if (invoiceUpdateRequest.getInInr() != null) {
            invoice.setInInr(invoiceUpdateRequest.getInInr());
        }
        if (invoiceUpdateRequest.getMonth() != null) {
            invoice.setMonth(invoiceUpdateRequest.getMonth());
        }
        if (invoiceUpdateRequest.getInvoiceNo()!=null){
            invoice.setInvoiceNo(invoiceUpdateRequest.getInvoiceNo());
        }


        // Save the updated invoice
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return ResponseEntity.ok(updatedInvoice);
    }


    @GetMapping
    public ResponseEntity<List<InvoiceDto>> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll(); // Fetch all invoices
        List<InvoiceDto> invoiceDtos = InvoiceService.toInvoiceDtoList(invoices); // Convert entities to DTOs
        return ResponseEntity.ok(invoiceDtos);
    }

    @PostMapping
    public ResponseEntity<?> createInvoice(@RequestBody InvoiceDto invoiceDto) {
        // Convert InvoiceDto to Invoice entity
        // Check for duplicate invoice number
        if (invoiceRepository.existsByInvoiceNo(invoiceDto.getInvoiceNo())) {
            throw new DuplicateInvoiceException("Invoice number already exists: " + invoiceDto.getInvoiceNo());
        }
        Invoice invoice = new Invoice();
        Client client=clientRepository.findById(invoiceDto.getClientId()).get();
        invoice.setClient(clientRepository.findById(invoiceDto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found")));
        invoice.setRaisedDate(invoiceDto.getRaisedDate());
        invoice.setMonth(invoiceDto.getMonth());
        invoice.setTotalAmount(invoiceDto.getTotalAmount());
        invoice.setInvoiceNo(invoiceDto.getInvoiceNo());
        invoice.setDueDate(invoiceDto.getDueDate());
        invoice.setStatus(invoiceDto.getStatus());
        invoice.setComments(invoiceDto.getComments());
        invoice.setCurrency(client.getCurrency());
//        String valueInInr= String.valueOf(invoiceDto.getTotalAmount()*81);
//        if (client.getCurrency().equals("USD")){
//            invoice.setInInr("Rs"+" "+valueInInr);
//        }
//        else {
//            invoice.setInInr("Rs"+" "+invoiceDto.getTotalAmount());
//        }

        try {
            BigDecimal totalAmount = new BigDecimal(invoiceDto.getTotalAmount());
            BigDecimal exchangeRate = BigDecimal.valueOf(81);
            String valueInInr = "Rs " + totalAmount.multiply(exchangeRate).toPlainString();

            if (client.getCurrency().equals("USD")) {
                invoice.setInInr(valueInInr);
            } else {
                invoice.setInInr("Rs " + invoiceDto.getTotalAmount());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid total amount: " + invoiceDto.getTotalAmount(), e);
        }


        // Save the invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Convert saved Invoice to InvoiceDto
        InvoiceDto responseDto = convertToDto(savedInvoice);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable Integer id){
        invoiceRepository.deleteById(id);
        return ResponseEntity.ok("Invoice deleted successfully");
    }

    private InvoiceDto convertToDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setClientId(invoice.getClient().getId());
        dto.setClientName(invoice.getClient().getClientName());
        dto.setRaisedDate(invoice.getRaisedDate());
        dto.setMonth(invoice.getMonth());
        dto.setTotalAmount(invoice.getClient().getCurrency()+" "+invoice.getTotalAmount());
        dto.setInvoiceNo(invoice.getInvoiceNo());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus());
        dto.setComments(invoice.getComments());
        dto.setInInr(invoice.getInInr());
        return dto;
    }

    @GetMapping("/status-count")
    public ResponseEntity<Map<String, Long>> getInvoiceStatusCounts() {
        Map<String, Long> statusCounts = invoiceService.getInvoiceStatusCounts();
        return ResponseEntity.ok(statusCounts);
    }

    @GetMapping("/invoices/total-amounts")
    public ResponseEntity<Map<String, Double>> getTotalAmounts() {
        Map<String, Double> amounts = invoiceService.getTotalAmounts();
        return ResponseEntity.ok(amounts);
    }




}