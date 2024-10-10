package com.employee_management_system.Employee_Management.Repository;

import com.employee_management_system.Employee_Management.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    User findByUsername(String username);

    void deleteByUsername(String name);
}
