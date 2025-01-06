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
public class InvoiceDto {
    private Integer id;
    private Integer clientId;
    private String clientName;
    private LocalDate raisedDate;
    private String month;
    private String totalAmount; // Total amount for the invoice
    private String inInr;
    private String invoiceNo;
    private LocalDate dueDate;
    private String status;
    private String transactionId;
    private String comments;


}
