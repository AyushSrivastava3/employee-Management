package com.employee_management_system.Employee_Management.Repository;

import com.employee_management_system.Employee_Management.Model.Client;
import com.employee_management_system.Employee_Management.Model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Invoice findByClientAndStartDateAndEndDate(Client client, LocalDate startDate, LocalDate endDate);

}