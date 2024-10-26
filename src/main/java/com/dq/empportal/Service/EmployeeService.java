package com.dq.empportal.Service;
import com.dq.empportal.Model.Employee;
import com.dq.empportal.Repository.ClientRepository;
import com.dq.empportal.Repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ClientRepository clientRepository;

   // Inject the sequence generator

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Integer id) { // Change to Integer
        return employeeRepository.findById(id);
    }

    public Employee createEmployee(Employee employee) {
        Employee employee1=employeeRepository.save(employee);

        return employee1;
    }

    public Employee updateEmployee(Integer id, Employee employeeDetails) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setFirstName(employeeDetails.getFirstName());
            employee.setLastName(employeeDetails.getLastName());
            employee.setMobileNumber(employeeDetails.getMobileNumber());
            employee.setAlternateMobileNumber(employeeDetails.getAlternateMobileNumber());
            employee.setPersonalEmail(employeeDetails.getPersonalEmail());
            employee.setProfessionalEmail(employeeDetails.getProfessionalEmail());
            employee.setDateOfBirth(employeeDetails.getDateOfBirth());
            employee.setGender(employeeDetails.getGender());
            employee.setHomeAddress(employeeDetails.getHomeAddress());
            employee.setAadharNumber(employeeDetails.getAadharNumber());
            employee.setPanNumber(employeeDetails.getPanNumber());
            employee.setJobTitle(employeeDetails.getJobTitle());
            employee.setDepartment(employeeDetails.getDepartment());
            employee.setManagerName(employeeDetails.getManagerName());
            employee.setEmploymentType(employeeDetails.getEmploymentType());
            employee.setEmployeeStatus(employeeDetails.getEmployeeStatus());
            employee.setWorkLocation(employeeDetails.getWorkLocation());
            employee.setCurrentLocation(employeeDetails.getCurrentLocation());
            employee.setEmployeeGrade(employeeDetails.getEmployeeGrade());
            employee.setDateOfJoining(employeeDetails.getDateOfJoining());
            employee.setDateOfResigning(employeeDetails.getDateOfResigning());
            employee.setSalaryPerAnnum(employeeDetails.getSalaryPerAnnum());
            employee.setPayroll(employeeDetails.getPayroll());
            employee.setBankAccountDetails(employeeDetails.getBankAccountDetails());
            employee.setPayDate(employeeDetails.getPayDate());
            employee.setTaxInformation(employeeDetails.getTaxInformation());
            employee.setNomineeName(employeeDetails.getNomineeName());
            employee.setNomineeBankAccountDetails(employeeDetails.getNomineeBankAccountDetails());
            employee.setInsurancePlanDetails(employeeDetails.getInsurancePlanDetails());
            employee.setLeavesBalance(employeeDetails.getLeavesBalance());
            employee.setOtherBenefits(employeeDetails.getOtherBenefits());

            return employeeRepository.save(employee);
        }).orElseThrow(() -> new RuntimeException("Employee not found"));
    }


    public void deleteEmployee(Integer id) { // Change to Integer
        employeeRepository.deleteById(id);
    }

    public Employee updateDays(Integer infoId, List<LocalDate> leaveDays, List<LocalDate> holidays, List<LocalDate> nonBillableDays) {

        Employee info = employeeRepository.findById(infoId)
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

        return employeeRepository.save(info);
    }


    public List<Employee> getActiveEmployeesInMonth(int year, int month) {
        // Calculate the first and last day of the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);  // First day of the month
        LocalDate endDate = yearMonth.atEndOfMonth();  // Last day of the month

        return employeeRepository.findActiveEmployeesInMonth(startDate, endDate);
    }

    public Employee removeDays(Integer employeeId, List<LocalDate> leaveDaysToRemove, List<LocalDate> holidaysToRemove, List<LocalDate> nonBillableDaysToRemove) {
        Employee info = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

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

        return employeeRepository.save(info);
    }
}
