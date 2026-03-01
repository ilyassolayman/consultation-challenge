package com.medexpress.consultation.repository;

import com.medexpress.consultation.dto.ConsultationResponse;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ConsultationRepository implements IConsultationRepository {

    private final ConcurrentHashMap<String, ConsultationResponse> consultations = new ConcurrentHashMap<>();

    public void save(ConsultationResponse response) {
        consultations.put(response.getConsultationId(), response);
    }

    public Optional<ConsultationResponse> findById(String consultationId) {
        return Optional.ofNullable(consultations.get(consultationId));
    }
}
