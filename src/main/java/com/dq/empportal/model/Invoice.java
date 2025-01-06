package com.dq.empportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private LocalDate startDate;
    private LocalDate endDate;
    private String totalAmount;
    private Double cgst;
    private Double sgst;
    private Double igst;
    private String totalAmountAfterGst;
    private String totalAmountAfterGstInWords;
    private String inInr;
    private String currency;// Total amount for the invoice
    private String status;      // e.g., "Paid", "Unpaid"
    private String transactionId;
    private String month;
    @ManyToMany
    private List<EmployeeClientInfo> employeeClientInfos = new ArrayList<>();
    @Column(nullable = false, unique = true)
    private String invoiceNo; // Unique invoice number with dynamic formatting

    @Column(nullable = false)
    private LocalDate dueDate; // Invoice due date (60 days after raise date)

    @Column(length = 500)
    private String comments; // Optional comments

    @Column(nullable = false)
    private LocalDate raisedDate;
}