package com.dq.empportal.Transform;

import com.dq.empportal.Dtos.InvoiceResponseDto;
import com.dq.empportal.Model.Invoice;
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
