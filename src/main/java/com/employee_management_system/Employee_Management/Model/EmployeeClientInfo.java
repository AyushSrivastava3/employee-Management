package com.employee_management_system.Employee_Management.Model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class EmployeeClientInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)

    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private Integer hourlyRate;
    private Integer totalBillableHours;
    private Integer leaveDay=0;
    private LocalDate joiningDateToClient;
    private LocalDate endDate;
    @OneToMany(mappedBy = "employeeClientInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Leave> leaves = new ArrayList<>();

    @ElementCollection
    private List<LocalDate> leaveDays = new ArrayList<>();
    @ElementCollection
    private List<LocalDate> holidays = new ArrayList<>(); // Store holidays as a list of dates
    @ElementCollection
    private List<LocalDate> nonBillableDays = new ArrayList<>();

    public boolean isCurrentlyAssigned() {
        return endDate == null || endDate.isAfter(LocalDate.now());
    }

    // Method to recalculate total leave days from approved leaves
    public void recalculateLeaveDays() {
        leaveDay = leaves.stream()
                .filter(leave -> "Approved".equals(leave.getStatus()))
                .mapToInt(Leave::getLeaveDuration)
                .sum();
    }

    // Method to add a leave and recalculate leave days
    public void addApprovedLeave(Leave leave) {
        if ("Approved".equals(leave.getStatus())) {
            leaves.add(leave);
            recalculateLeaveDays();
        }
    }

    public int TotalBillableHours(LocalDate startDate, LocalDate endDate) {
        // Assuming these fields are part of the EmployeeClientInfo class
        LocalDate joiningDateToClient = this.joiningDateToClient;
        LocalDate endDateForClient = this.endDate;  // Use this to avoid confusion with method parameter 'endDate'

        // Define the maximum billable hours per day
        final int HOURS_PER_DAY = 9;

        // Determine the actual working period for this employee
        LocalDate effectiveStartDate = (joiningDateToClient.isAfter(startDate)) ? joiningDateToClient : startDate;
        LocalDate effectiveEndDate = (endDateForClient != null && endDateForClient.isBefore(endDate)) ? endDateForClient : endDate;

        // If the employee's effective working period does not overlap with the invoice period, return 0
        if (effectiveStartDate.isAfter(effectiveEndDate)) {
            return 0;
        }

        // Calculate the number of working days in the effective period

        int workingDays = (int) (ChronoUnit.DAYS.between(effectiveStartDate, effectiveEndDate) + 1); // Including both start and end dates

        // Return the total billable hours (9 hours per day)
        return (int) workingDays * HOURS_PER_DAY;
    }


    public int getLeaveDaysWithinPeriod(LocalDate startDate, LocalDate endDate) {
        return (int) leaveDays.stream()
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .count();
    }
    public int getHolidaysWithinPeriod(LocalDate startDate, LocalDate endDate) {
        return (int) holidays.stream()
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .count();
    }

    public int getNonBillableDaysWithinPeriod(LocalDate startDate, LocalDate endDate) {
        return (int) nonBillableDays.stream()
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .count();
    }




}