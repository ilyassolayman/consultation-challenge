package com.medexpress.consultation.strategy;

import com.medexpress.consultation.dto.ConsultationRequest;
import com.medexpress.consultation.dto.ConsultationResponse;

public interface EligibilityStrategy {

    String getSupportedProductId();

    ConsultationResponse evaluate(ConsultationRequest request);
}
