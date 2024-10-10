package com.employee_management_system.Employee_Management.Service;


import com.employee_management_system.Employee_Management.Model.Employee;
import com.employee_management_system.Employee_Management.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

   // Inject the sequence generator

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Integer id) { // Change to Integer
        return employeeRepository.findById(id);
    }

    public Employee createEmployee(Employee employee) {
        //employee.setId(sequenceGeneratorService.generateSequence("employee_sequence")); // Generate ID
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Integer id, Employee employeeDetails) { // Change to Integer
        return employeeRepository.findById(id).map(employee -> {
            employee.setFirstName(employeeDetails.getFirstName());
            employee.setLastName(employeeDetails.getLastName());
            // Update all other fields similarly
            return employeeRepository.save(employee);
        }).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public void deleteEmployee(Integer id) { // Change to Integer
        employeeRepository.deleteById(id);
    }
}
