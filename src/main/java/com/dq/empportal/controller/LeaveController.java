package com.dq.empportal.controller;

import com.dq.empportal.model.Leave;
import com.dq.empportal.model.LeaveRequest;
import com.dq.empportal.model.EmployeeClientInfo;
import com.dq.empportal.repository.EmployeeClientInfoRepository;
import com.dq.empportal.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/leaves")
public class LeaveController {
    @Autowired
    EmployeeClientInfoRepository employeeClientInfoRepository;
    @Autowired
    LeaveRepository leaveRepository;
    @PostMapping("/submitLeave")
    public ResponseEntity<String> submitLeaveRequest(@RequestBody LeaveRequest request) {
        EmployeeClientInfo employeeClientInfo = employeeClientInfoRepository.findById(request.getEmployeeClientInfoId())
                .orElseThrow(() -> new RuntimeException("EmployeeClientInfo not found"));

        // Create a leave object but don't add it to EmployeeClientInfo yet
        Leave leave = new Leave();
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setType(request.getType());
        leave.setReason(request.getReason());
        leave.setStatus("Pending");  // Status is pending for now

        leave.setEmployeeClientInfo(employeeClientInfo); // Associate leave with employee-client info
        leaveRepository.save(leave);  // Save the leave request with status pending

        return ResponseEntity.ok("Leave request submitted successfully and is pending approval.");
    }

    @PostMapping("/approveLeave/{leaveId}")
    public ResponseEntity<String> approveLeave(@PathVariable int leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!"Pending".equals(leave.getStatus())) {
            return ResponseEntity.badRequest().body("Leave request is not pending approval.");
        }

        leave.setStatus("Approved");

        EmployeeClientInfo employeeClientInfo = leave.getEmployeeClientInfo();
        employeeClientInfo.addApprovedLeave(leave);  // Add approved leave and recalculate leave days

        employeeClientInfoRepository.save(employeeClientInfo);  // Save updated employee-client info
        leaveRepository.save(leave);  // Save the leave with updated status

        return ResponseEntity.ok("Leave request approved successfully.");
    }


    @PostMapping("/rejectLeave/{leaveId}")
    public ResponseEntity<String> rejectLeave(@PathVariable Integer leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!leave.getStatus().equals("Pending")) {
            return ResponseEntity.badRequest().body("Leave request is not pending approval.");
        }

        leave.setStatus("Rejected");  // Update status to rejected
        leaveRepository.save(leave);  // Save the leave request with rejected status

        return ResponseEntity.ok("Leave request rejected.");
    }
}
