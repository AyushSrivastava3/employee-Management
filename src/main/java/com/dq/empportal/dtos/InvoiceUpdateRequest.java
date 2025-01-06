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
public class InvoiceUpdateRequest {
    private String status;
    private String comments;
    private String transactionId;
    private String invoiceNo;
    private LocalDate raisedDate;
    private LocalDate dueDate;
    private String totalAmount;
    private String inInr;
    private String month;


}
