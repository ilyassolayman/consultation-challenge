package com.medexpress.consultation.strategy;

import com.medexpress.consultation.dto.ConsultationStatus;

public enum RuleSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public ConsultationStatus toConsultationStatus() {
        return switch (this) {
            case CRITICAL, HIGH -> ConsultationStatus.REJECTED;
            case MEDIUM -> ConsultationStatus.PENDING_CLINICAL_REVIEW;
            case LOW -> ConsultationStatus.APPROVED_WITH_NOTES;
        };
    }
}
