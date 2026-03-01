package com.medexpress.consultation.exception;

public class ConsultationsNotFoundException extends RuntimeException {

    public ConsultationsNotFoundException(String customerId) {
        super("Consultations not found for customer: " + customerId);
    }
}
