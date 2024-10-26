//package com.employee_management_system.Employee_Management.Transform;
//
//import com.employee_management_system.Employee_Management.Dtos.Employee_Dto;
//import com.employee_management_system.Employee_Management.Model.Employee;
//import com.employee_management_system.Employee_Management.Model.EmployeeClientInfo;
//import org.springframework.stereotype.Component;
//
//@Component
//public class EmployeeToEmployeeClientInfo {
//    public EmployeeClientInfo transform(Employee employee){
//        EmployeeClientInfo employeeClientInfo = new EmployeeClientInfo();
//        employeeClientInfo.setEmpId(employee.getId());
//        employeeClientInfo.setFirstName(employee.getFirstName());
//        employeeClientInfo.setLastName(employee.getLastName());
//        employeeClientInfo.setClientId(employee.getClientId());
//        employeeClientInfo.setClientName(employee.getClientName());
//        employeeClientInfo.setHourlyRate(employee.getHourlyRate());
//        employeeClientInfo.setJoiningDate_toClient(employee.getJoiningDate_toClient());
//        employeeClientInfo.setLeaveDay(employee.getLeaveDay());
//        employeeClientInfo.setMobileNumber(employee.getMobileNumber());
//        employeeClientInfo.setAlternateMobileNumber(employee.getAlternateMobileNumber());
//        employeeClientInfo.setPersonalEmail(employee.getPersonalEmail());
//        employeeClientInfo.setProfessionalEmail(employee.getProfessionalEmail());
//        return employeeClientInfo;
//    }
//}
