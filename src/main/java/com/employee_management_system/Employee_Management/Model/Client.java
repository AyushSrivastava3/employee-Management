package com.employee_management_system.Employee_Management.Model;

import com.employee_management_system.Employee_Management.Dtos.Employee_Dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Client {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    private String clientName;
    private String clientAddress;
    private String gstNumber;
    private String accountNumber;
    private String ifscCode;
    private String currency;
    private int totalPendingInvoices;
    private LocalDate createdDate;
    private String location;
    private String timeline;
    private String miscellaneous;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EmployeeClientInfo> resources = new ArrayList<>();

}