package com.dq.empportal.dtos;

import com.dq.empportal.model.EmployeeClientInfo;
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
public class InvoiceResponseDto {
    private Integer id;
    private Integer clientId;
    private String clientName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalAmount; // Total amount for the invoice
    private String status;
    private List<EmployeeClientInfo> employees;
}
