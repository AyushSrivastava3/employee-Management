package com.dq.empportal.repository;

import com.dq.empportal.model.Client;
import com.dq.empportal.model.EmployeeClientInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeClientInfoRepository extends JpaRepository<EmployeeClientInfo,Integer> {
    @Query("SELECT e FROM EmployeeClientInfo e WHERE e.client = :client AND e.joiningDateToClient BETWEEN :startDate AND :endDate")
    List<EmployeeClientInfo> findByClientAndJoiningDateToClient(
            @Param("client") Client client,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e FROM EmployeeClientInfo e WHERE e.client = :client " +
            "AND e.joiningDateToClient <= :endDate " + // Joined before or during the invoice period
            "AND (e.endDate IS NULL OR e.endDate >= :startDate)") // Still working for the client during the invoice period
    List<EmployeeClientInfo> findActiveEmployeesByClientForInvoice(
            @Param("client") Client client,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
