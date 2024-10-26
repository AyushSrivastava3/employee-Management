package com.employee_management_system.Employee_Management.Repository;


import com.employee_management_system.Employee_Management.Model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client,Integer> {


    // Query to find clients within a date range using JPQL
    @Query("SELECT c FROM Client c WHERE c.createdDate >= :startDate AND c.createdDate < :endDate")
    List<Client> findClientWithinDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT c FROM Client c WHERE c.createdDate >= :todayStart")
    List<Client> findClientAddedToday(@Param("todayStart") LocalDate todayStart);

    List<Client> findByCreatedDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT c FROM Client c WHERE c.createdDate = :date")
    List<Client> findByCreatedDate(@Param("date") LocalDate date);

}