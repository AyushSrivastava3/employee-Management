package com.dq.empportal.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Employee_Dto {
    private Integer emp_id;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String alternateMobileNumber;
    private String personalEmail;
    private String professionalEmail;

    private String clientName;
    private Integer clientId;
    private String hourlyRate;
    private Integer leaveDay;
    private LocalDate joiningDate_toClient;
}
