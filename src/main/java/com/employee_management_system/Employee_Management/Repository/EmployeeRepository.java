package com.employee_management_system.Employee_Management.Repository;

import com.employee_management_system.Employee_Management.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
    @Query("SELECT e FROM Employee e WHERE e.dateOfJoining <= :endDate " +
            "AND (e.dateOfResigning IS NULL OR e.dateOfResigning >= :startDate)")
    List<Employee> findActiveEmployeesInMonth(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
}
