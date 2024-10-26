package com.dq.empportal.repository;

import com.dq.empportal.model.Client;
import com.dq.empportal.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Invoice findByClientAndStartDateAndEndDate(Client client, LocalDate startDate, LocalDate endDate);

}