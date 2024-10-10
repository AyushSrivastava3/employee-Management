package com.employee_management_system.Employee_Management.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String alternateMobileNumber;
    private String personalEmail;
    private String professionalEmail;
    private LocalDate dateOfJoining;
    private int totalLeavesAvailable;
    private int leavesTakenThisMonth;
    private double salaryPerAnnum;

    private LocalDate dateOfBirth;
    private String gender;
    private String homeAddress;
    private String aadharNumber;
    private String panNumber;

    private String jobTitle;
    private String department;
    private String managerName;
    private String employmentType;
    private String employeeStatus;
    private String workLocation;
    private String currentLocation;
    private String employeeGrade;

    // Payroll & Salary Information
    private String payroll;
    private String bankAccountDetails;
    private LocalDate payDate;
    private String taxInformation;

    // Nominee
    private String nomineeName;
    private String nomineeBankAccountDetails;

    // Insurance, leaves, and other benefits
    private String insurancePlanDetails;
    private int leavesBalance;
    private String otherBenefits;

    // Work hours, leave requests, attendance, timesheets
    private String workHours;
    private String leaveRequests;
    private String attendanceTracking;
    private String timesheets;

    // Laptop details
    private String laptopDetails;
    private String laptopDeliveryAddress;
    private LocalDate laptopReceivedDate;
    private String laptopBills;


}