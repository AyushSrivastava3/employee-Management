package com.dq.empportal.service;

import com.dq.empportal.dtos.EmployeeClientInfoDTO;
import com.dq.empportal.model.Client;
import com.dq.empportal.repository.ClientRepository;
import com.dq.empportal.repository.EmployeeClientInfoRepository;
import com.dq.empportal.repository.EmployeeRepository;
import com.dq.empportal.model.Employee;
import com.dq.empportal.model.EmployeeClientInfo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeClientService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EmployeeClientInfoRepository employeeClientInfoRepository;

    @Transactional
    public void assignEmployeeToClient(Integer employeeId, Integer clientId, Integer hourlyRate,Integer workingHour, LocalDate startDate, LocalDate endDate) {
        // Fetch the employee and client by their IDs
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        // Create a new EmployeeClientInfo object
        EmployeeClientInfo employeeClientInfo = new EmployeeClientInfo();
        employeeClientInfo.setEmployee(employee);
        employeeClientInfo.setClient(client);
        employeeClientInfo.setHourlyRate(hourlyRate);
        employeeClientInfo.setWorkingHours(workingHour);
        employeeClientInfo.setJoiningDateToClient(startDate);
        if (endDate != null) {
//            employeeClientInfo.endDate(endDate);
            employeeClientInfo.setEndDate(endDate);
        }

        // Add this resource to the client's resource list
        client.getResources().add(employeeClientInfo);

        // Save the employee-client assignment
        employeeClientInfoRepository.save(employeeClientInfo);

        // You might want to save the client again if you want to persist the changes to the resources list
        clientRepository.save(client);
    }
//    public EmployeeClientInfo updateDays(Integer infoId, List<LocalDate> leaveDays, List<LocalDate> holidays, List<LocalDate> nonBillableDays) {
//        EmployeeClientInfo info = employeeClientInfoRepository.findById(infoId)
//                .orElseThrow(() -> new EntityNotFoundException("EmployeeClientInfo not found"));
//
//        // Update only if non-null values are provided
//        if (leaveDays != null) {
//            info.setLeaveDays(leaveDays);
//        }
//
//        if (holidays != null) {
//            info.setHolidays(holidays);
//        }
//
//        if (nonBillableDays != null) {
//            info.setNonBillableDays(nonBillableDays);
//        }
//
//        return employeeClientInfoRepository.save(info);
//    }

    public EmployeeClientInfo updateDays(Integer infoId, List<LocalDate> leaveDays, List<LocalDate> holidays, List<LocalDate> nonBillableDays) {
        EmployeeClientInfo info = employeeClientInfoRepository.findById(infoId)
                .orElseThrow(() -> new EntityNotFoundException("EmployeeClientInfo not found"));

        // Append new leaveDays, if provided
        if (leaveDays != null && !leaveDays.isEmpty()) {
            List<LocalDate> existingLeaveDays = info.getLeaveDays();
            Set<LocalDate> updatedLeaveDays = new HashSet<>(existingLeaveDays);  // Convert to Set to avoid duplicates
            updatedLeaveDays.addAll(leaveDays);  // Add new leave days
            info.setLeaveDays(new ArrayList<>(updatedLeaveDays));  // Convert back to List
        }

        // Append new holidays, if provided
        if (holidays != null && !holidays.isEmpty()) {
            List<LocalDate> existingHolidays = info.getHolidays();
            Set<LocalDate> updatedHolidays = new HashSet<>(existingHolidays);
            updatedHolidays.addAll(holidays);
            info.setHolidays(new ArrayList<>(updatedHolidays));
        }

        // Append new nonBillableDays, if provided
        if (nonBillableDays != null && !nonBillableDays.isEmpty()) {
            List<LocalDate> existingNonBillableDays = info.getNonBillableDays();
            Set<LocalDate> updatedNonBillableDays = new HashSet<>(existingNonBillableDays);
            updatedNonBillableDays.addAll(nonBillableDays);
            info.setNonBillableDays(new ArrayList<>(updatedNonBillableDays));
        }

        return employeeClientInfoRepository.save(info);
    }


//    public List<EmployeeClientInfoDTO> getEmployeesForClientInMonth(Integer clientId, String yearMonth) {
//        Client client = clientRepository.findById(clientId)
//                .orElseThrow(() -> new RuntimeException("Client not found"));
//
//        YearMonth ym = YearMonth.parse(yearMonth);
//        LocalDate startDate = ym.atDay(1);
//        LocalDate endDate = ym.atEndOfMonth();
//
//        List<EmployeeClientInfo> resources = employeeClientInfoRepository
//                .findActiveEmployeesByClientForInvoice(client, startDate, endDate);
//
//        return resources.stream().map(info -> new EmployeeClientInfoDTO(
//                info.getId(),
//                info.getEmployee().getFirstName() + " " + info.getEmployee().getLastName(),
//                info.getHourlyRate(),
//                info.getTotalBillableHours(),
//
//                // Filter leaveDays for the specified month
//                info.getLeaveDays().stream()
//                        .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
//                        .collect(Collectors.toList()),
//
//                // Filter holidays for the specified month
//                info.getHolidays().stream()
//                        .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
//                        .collect(Collectors.toList()),
//
//                // Filter nonBillableDays for the specified month
//                info.getNonBillableDays().stream()
//                        .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
//                        .collect(Collectors.toList())
//        )).collect(Collectors.toList());
//    }
public List<EmployeeClientInfoDTO> getEmployeesForClientInMonth(Integer clientId, String yearMonth) {
    Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new RuntimeException("Client not found"));

    YearMonth ym = YearMonth.parse(yearMonth);
    LocalDate startDate = ym.atDay(1);
    LocalDate endDate = ym.atEndOfMonth();

    List<EmployeeClientInfo> resources = employeeClientInfoRepository
            .findActiveEmployeesByClientForInvoice(client, startDate, endDate);

    return resources.stream().map(info -> {
        // Calculate total days in the month
        long totalDaysInMonth = ChronoUnit.DAYS.between(startDate, endDate) + 1; // +1 to include end date

        // Count the working days by subtracting leave, holidays, and non-billable days
        long leaveDaysCount = info.getLeaveDays().stream()
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .count();

        long holidaysCount = info.getHolidays().stream()
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .count();

        long nonBillableDaysCount = info.getNonBillableDays().stream()
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .count();

        // Calculate total billable hours
        long totalBillableDays = totalDaysInMonth - (leaveDaysCount + holidaysCount + nonBillableDaysCount);
        int totalBillableHours = (int) (totalBillableDays * 9); // Assuming 9 hours per working day

        return new EmployeeClientInfoDTO(
                info.getId(),
                info.getEmployee().getFirstName() + " " + info.getEmployee().getLastName(),
                info.getHourlyRate(),
                totalBillableHours, // Use calculated billable hours here

                // Filter leaveDays for the specified month
                info.getLeaveDays().stream()
                        .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                        .collect(Collectors.toList()),

                // Filter holidays for the specified month
                info.getHolidays().stream()
                        .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                        .collect(Collectors.toList()),

                // Filter nonBillableDays for the specified month
                info.getNonBillableDays().stream()
                        .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                        .collect(Collectors.toList())
        );
    }).collect(Collectors.toList());
}



    public EmployeeClientInfo removeDays(Integer infoId, List<LocalDate> leaveDaysToRemove, List<LocalDate> holidaysToRemove, List<LocalDate> nonBillableDaysToRemove) {
        EmployeeClientInfo info = employeeClientInfoRepository.findById(infoId)
                .orElseThrow(() -> new EntityNotFoundException("EmployeeClientInfo not found"));

        // Remove specific leaveDays, if provided
        if (leaveDaysToRemove != null && !leaveDaysToRemove.isEmpty()) {
            info.getLeaveDays().removeAll(leaveDaysToRemove); // Remove the provided days
        }

        // Remove specific holidays, if provided
        if (holidaysToRemove != null && !holidaysToRemove.isEmpty()) {
            info.getHolidays().removeAll(holidaysToRemove);
        }

        // Remove specific nonBillableDays, if provided
        if (nonBillableDaysToRemove != null && !nonBillableDaysToRemove.isEmpty()) {
            info.getNonBillableDays().removeAll(nonBillableDaysToRemove);
        }

        return employeeClientInfoRepository.save(info);
    }


}
