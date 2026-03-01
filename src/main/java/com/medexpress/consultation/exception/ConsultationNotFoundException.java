package com.medexpress.consultation.exception;

public class ConsultationNotFoundException extends RuntimeException {

    public ConsultationNotFoundException(String consultationId) {
        super("Consultation not found: " + consultationId);
    }
}
