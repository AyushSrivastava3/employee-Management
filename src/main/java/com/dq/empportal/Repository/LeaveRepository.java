package com.dq.empportal.Repository;

import com.dq.empportal.Model.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRepository extends JpaRepository<Leave,Integer> {
}
