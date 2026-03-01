package com.medexpress.consultation.service;

import com.medexpress.consultation.dto.ConsultationRequest;
import com.medexpress.consultation.dto.ConsultationResponse;

public interface IConsultationService {

    ConsultationResponse getConsultation(String consultationId);

    ConsultationResponse submitConsultation(ConsultationRequest request);

}