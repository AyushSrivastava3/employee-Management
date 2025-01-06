package com.dq.empportal.dtos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmployeeClientInfoDTO {
    private Integer id;
    private String employeeName;
    private Integer hourlyRate;
    private Integer totalBillableHours;
    private List<LocalDate> leaveDays;
    private List<LocalDate> holidays;
    private List<LocalDate> nonBillableDays;
    private List<LocalDate> weekendDays;
}
