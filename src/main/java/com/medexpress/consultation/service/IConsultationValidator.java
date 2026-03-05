package com.medexpress.consultation.service;

import com.medexpress.consultation.dto.ConsultationRequest;

public interface IConsultationValidator {

    void validate(ConsultationRequest request);
}
