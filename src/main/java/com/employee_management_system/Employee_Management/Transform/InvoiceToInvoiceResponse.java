package com.employee_management_system.Employee_Management.Transform;

import com.employee_management_system.Employee_Management.Dtos.InvoiceResponseDto;
import com.employee_management_system.Employee_Management.Model.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceToInvoiceResponse {
    public InvoiceResponseDto transform(Invoice invoice){
        InvoiceResponseDto invoiceResponseDto=new InvoiceResponseDto();
        invoiceResponseDto.setId(invoice.getId());
        invoiceResponseDto.setClientId(invoice.getClient().getId());
        invoiceResponseDto.setClientName(invoice.getClient().getClientName());
        invoiceResponseDto.setStartDate(invoice.getStartDate());
        invoiceResponseDto.setEndDate(invoice.getEndDate());
        invoiceResponseDto.setTotalAmount(invoice.getTotalAmount());
        invoiceResponseDto.setStatus(invoice.getStatus());
//        invoiceResponseDto.setEmployees(invoice.getEmployees());
        invoiceResponseDto.setEmployees(invoice.getEmployeeClientInfos());
        return invoiceResponseDto;
    }
}
