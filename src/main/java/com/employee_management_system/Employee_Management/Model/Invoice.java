package com.employee_management_system.Employee_Management.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
//    private String clientName;
//    private Integer clientId;
//    private String invoiceNumber;
//    private String invoiceValue;
//    private String currency;
//    private String month;
//    private LocalDate raisedOn;
//    private String timeline;
//    private LocalDate targetDate;
//    private String createdBy;
//    private LocalDate createdDate;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalAmount; // Total amount for the invoice
    private String status;      // e.g., "Paid", "Unpaid"
//    @ManyToMany
//    @JoinTable(name = "invoice_employees")
//    private List<Employee> employees = new ArrayList<>();
@ManyToMany
private List<EmployeeClientInfo> employeeClientInfos = new ArrayList<>();

    // Clear all employee associations
//    public void clearEmployees() {
//        this.employees.clear();
//    }
//    // Add employees to the invoice
//    public void addEmployee(Employee employee) {
//        this.employees.add(employee);
//    }

}