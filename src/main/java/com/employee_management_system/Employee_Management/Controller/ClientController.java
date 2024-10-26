package com.employee_management_system.Employee_Management.Controller;

import com.employee_management_system.Employee_Management.Model.Client;
import com.employee_management_system.Employee_Management.Repository.ClientRepository;
import com.employee_management_system.Employee_Management.Service.ClientService;
import com.employee_management_system.Employee_Management.Service.EmployeeClientService;
import com.employee_management_system.Employee_Management.Service.ExportImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    ClientService clientService;
    @Autowired
    EmployeeClientService employeeClientService;

    @GetMapping
    public List<Client> getAllClients(){
        return clientRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Integer id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id " + id));
        return ResponseEntity.ok(client);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Integer id, @RequestBody Client clientDetails) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id " + id));

        // Update only the fields that are not null in clientDetails
        if (clientDetails.getClientName() != null) {
            client.setClientName(clientDetails.getClientName());
        }
        if (clientDetails.getClientAddress() != null) {
            client.setClientAddress(clientDetails.getClientAddress());
        }
        if (clientDetails.getGstNumber() != null) {
            client.setGstNumber(clientDetails.getGstNumber());
        }
        if (clientDetails.getAccountNumber() != null) {
            client.setAccountNumber(clientDetails.getAccountNumber());
        }
        if (clientDetails.getIfscCode() != null) {
            client.setIfscCode(clientDetails.getIfscCode());
        }
        if (clientDetails.getCurrency() != null) {
            client.setCurrency(clientDetails.getCurrency());
        }
        if (clientDetails.getLocation()!=null){
            client.setLocation(clientDetails.getLocation());
        }
        if (clientDetails.getTimeline()!=null){
            client.setTimeline(clientDetails.getTimeline());
        }
        if (clientDetails.getMiscellaneous()!=null){
            client.setMiscellaneous(clientDetails.getMiscellaneous());
        }

        // Save and return the updated client
        Client updatedClient = clientRepository.save(client);
        return ResponseEntity.ok(updatedClient);
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client){
        client.setCreatedDate(LocalDate.now());
        Client savedclient= clientRepository.save(client);
        return ResponseEntity.ok(savedclient);
    }

    @GetMapping("/clients/export")
    public ResponseEntity<byte[]> exportClientsToExcel() {
        try {
            List<Client> clients = clientRepository.findAll();
            ExportImportService<Client> exporter = new ExportImportService<>();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            exporter.exportToExcel(clients, baos);

            byte[] excelBytes = baos.toByteArray();
            System.out.println("Excel file size: " + excelBytes.length);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=profiles_export.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(("Failed to create Excel file: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/clients/import")
    public ResponseEntity<?> importClientsFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            ExportImportService<Client> importer = new ExportImportService<>();
            List<Client> profiles = importer.importFromCSV(file, Client.class);
            clientRepository.saveAll(profiles);
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to import CSV file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Integer id){
        clientService.deleteClientById(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/getTodayClients")
    public List<Client> getTodayClients() {
        return clientService.getClientsCreatedToday();
    }
    @GetMapping("/getWeekClients")
    public List<Client> getWeekClients() {
        return clientService.getClientsCreatedInWeek();
    }

    @PostMapping("/{clientId}/assign-employee/{employeeId}")
    public String assignEmployeeToClient(
            @PathVariable Integer clientId,
            @PathVariable Integer employeeId,
            @RequestParam Integer hourlyRate,

            @RequestParam LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        employeeClientService.assignEmployeeToClient(employeeId, clientId, hourlyRate, startDate, endDate);
        return "Employee assigned to client successfully!";
    }


}