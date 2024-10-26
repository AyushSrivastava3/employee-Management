package com.employee_management_system.Employee_Management.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "employee_leave")
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "employee_client_info_id")
    private EmployeeClientInfo employeeClientInfo;

    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String reason;
    private String status; // "Pending", "Approved", "Rejected"
    private String approvedBy; // Approver's name or ID
    private LocalDate approvalDate;

    // Compute the number of days for the leave
    public int getLeaveDuration() {
        return Period.between(startDate, endDate).getDays() + 1;  // Including both start and end date
    }
}
