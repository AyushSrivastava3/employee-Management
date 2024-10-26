package com.employee_management_system.Employee_Management.Repository;

import com.employee_management_system.Employee_Management.Model.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRepository extends JpaRepository<Leave,Integer> {
}
