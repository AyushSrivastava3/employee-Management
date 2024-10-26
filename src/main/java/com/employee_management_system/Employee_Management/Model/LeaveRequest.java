package com.employee_management_system.Employee_Management.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LeaveRequest {
    private int employeeClientInfoId; // Reference to EmployeeClientInfo
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;  // Example: "Paid", "Unpaid", etc.
    private String reason;
}
