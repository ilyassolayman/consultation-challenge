package com.medexpress.consultation.service;

import com.medexpress.consultation.dto.ConsultationRequest;
import com.medexpress.consultation.dto.ConsultationResponse;
import com.medexpress.consultation.exception.ConsultationNotFoundException;
import com.medexpress.consultation.exception.ProductNotFoundException;
import com.medexpress.consultation.repository.IConsultationRepository;
import com.medexpress.consultation.strategy.EligibilityStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsultationService implements IConsultationService {

    private final IConsultationRepository consultationRepository;
    private final Map<String, EligibilityStrategy> strategyByProductId;

    public ConsultationService(IConsultationRepository consultationRepository,
                               List<EligibilityStrategy> strategies) {
        this.consultationRepository = consultationRepository;
        this.strategyByProductId = strategies.stream()
                .collect(Collectors.toMap(EligibilityStrategy::getSupportedProductId, Function.identity()));
    }

    @Override
    public ConsultationResponse getConsultation(String consultationId) {
        log.info("Retrieving consultation for consultationId={}", consultationId);
        return consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ConsultationNotFoundException(consultationId));
    }

    @Override
    public ConsultationResponse submitConsultation(ConsultationRequest request) {
        log.info("Submitting consultation request");
        EligibilityStrategy strategy = strategyByProductId.get(request.getProductId());
        if (strategy == null) {
            throw new ProductNotFoundException(request.getProductId());
        }
        ConsultationResponse response = strategy.evaluate(request);
        consultationRepository.save(response);
        log.info("Consultation submitted: consultationId={}, productId={}, customerId={}, eligible={}",
                response.getConsultationId(), request.getProductId(), request.getCustomerId(), response.isEligible());
        return response;
    }
}
