package com.medexpress.consultation.exception;

import com.medexpress.consultation.dto.ValidationError;

import java.util.List;

public class InvalidConsultationRequestException extends RuntimeException {

    private final List<ValidationError> errors;

    public InvalidConsultationRequestException(List<ValidationError> errors) {
        super("Consultation request validation failed");
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
