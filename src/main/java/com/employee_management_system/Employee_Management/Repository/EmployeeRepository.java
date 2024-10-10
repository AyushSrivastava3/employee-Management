package com.employee_management_system.Employee_Management.Repository;

import com.employee_management_system.Employee_Management.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
}
