package com.medexpress.consultation.repository;

import com.medexpress.consultation.dto.ConsultationResponse;

import java.util.Optional;

public interface IConsultationRepository {

    void save(ConsultationResponse response);

    Optional<ConsultationResponse> findById(String consultationId);
}
