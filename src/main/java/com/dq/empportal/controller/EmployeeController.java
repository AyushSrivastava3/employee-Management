package com.dq.empportal.controller;
import com.dq.empportal.model.Employee;
import com.dq.empportal.service.EmployeeService;
import com.dq.empportal.dtos.UpdateDaysRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // Get all employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    // Get employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Integer id) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        return employee.map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Create a new employee
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    // Update an existing employee
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Integer id, @RequestBody Employee employeeDetails) {
        Employee updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
        return ResponseEntity.ok(updatedEmployee);
    }

    // Delete an employee
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Integer id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/updateDays")
    public ResponseEntity<Employee> updateDays(
            @RequestParam Integer employeeId,
            @RequestBody UpdateDaysRequest request) {

        // Update the employee-client info with the provided values
        Employee updatedInfo = employeeService.updateDays(
                employeeId,
                request.getLeaveDays(),
                request.getHolidays(),
                request.getNonBillableDays());
        return ResponseEntity.ok(updatedInfo);
    }

    @GetMapping("/employees/active")
    public List<Employee> getActiveEmployeesInMonth(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        return employeeService.getActiveEmployeesInMonth(year, month);
    }

    @PostMapping("/removeDays")
    public ResponseEntity<Employee> removeDays(
            @RequestParam Integer employeeId,
            @RequestBody UpdateDaysRequest request) {

        Employee updatedInfo = employeeService.removeDays(employeeId, request.getLeaveDays(), request.getHolidays(), request.getNonBillableDays());
        return ResponseEntity.ok(updatedInfo);
    }

}

