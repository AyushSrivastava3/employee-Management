package com.dq.empportal.controller;

import com.dq.empportal.dtos.EmployeeClientInfoDTO;
import com.dq.empportal.dtos.UpdateDaysRequest;
import com.dq.empportal.model.EmployeeClientInfo;
import com.dq.empportal.repository.ClientRepository;
import com.dq.empportal.repository.EmployeeClientInfoRepository;
import com.dq.empportal.service.ClientService;
import com.dq.empportal.service.EmployeeClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/employeeClientInfo")
public class EmployeeClientInfoController {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    ClientService clientService;
    @Autowired
    EmployeeClientService employeeClientService;
    @Autowired
    private EmployeeClientInfoRepository employeeClientInfoRepository;
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

//    @PostMapping("/updateDays")
//    public ResponseEntity<EmployeeClientInfo> updateDays(
//            @RequestParam Integer infoId,
//            @RequestBody(required = false) List<LocalDate> leaveDays,
//            @RequestBody(required = false) List<LocalDate> holidays,
//            @RequestBody(required = false) List<LocalDate> nonBillableDays) {
//
//        // Update the employee-client info with the provided values
//        EmployeeClientInfo updatedInfo = employeeClientService.updateDays(infoId, leaveDays, holidays, nonBillableDays);
//        return ResponseEntity.ok(updatedInfo);
//    }

    @PostMapping("/updateDays")
    public ResponseEntity<EmployeeClientInfo> updateDays(
            @RequestParam Integer infoId,
            @RequestBody UpdateDaysRequest request) {

        // Update the employee-client info with the provided values
        EmployeeClientInfo updatedInfo = employeeClientService.updateDays(
                infoId,
                request.getLeaveDays(),
                request.getHolidays(),
                request.getNonBillableDays());
        return ResponseEntity.ok(updatedInfo);
    }

    @GetMapping("/employeesForClientInMonth")
    public ResponseEntity<List<EmployeeClientInfoDTO>> getEmployeesForClientInMonth(
            @RequestParam Integer clientId,
            @RequestParam String yearMonth) {

        try {
            // Call service to fetch employees for the client in the specified month
            List<EmployeeClientInfoDTO> employees = employeeClientService.getEmployeesForClientInMonth(clientId, yearMonth);
            return ResponseEntity.ok(employees);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(null); // Handle invalid date format
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Handle client not found
        }
    }

    @PostMapping("/removeDays")
    public ResponseEntity<EmployeeClientInfo> removeDays(
            @RequestParam Integer infoId,
            @RequestBody(required = false) List<LocalDate> leaveDaysToRemove,
            @RequestBody(required = false) List<LocalDate> holidaysToRemove,
            @RequestBody(required = false) List<LocalDate> nonBillableDaysToRemove) {

        EmployeeClientInfo updatedInfo = employeeClientService.removeDays(infoId, leaveDaysToRemove, holidaysToRemove, nonBillableDaysToRemove);
        return ResponseEntity.ok(updatedInfo);
    }


}
