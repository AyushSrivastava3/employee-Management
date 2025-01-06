package com.dq.empportal.exception;

public class DuplicateInvoiceException extends RuntimeException {
    public DuplicateInvoiceException(String message) {
        super(message);
    }
}
