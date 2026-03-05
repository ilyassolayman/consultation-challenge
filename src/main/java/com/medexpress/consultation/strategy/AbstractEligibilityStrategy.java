package com.medexpress.consultation.strategy;

import com.medexpress.consultation.dto.AnswerDto;
import com.medexpress.consultation.dto.ConsultationRequest;
import com.medexpress.consultation.dto.ConsultationResponse;
import com.medexpress.consultation.dto.ConsultationStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractEligibilityStrategy implements EligibilityStrategy {

    protected abstract List<EligibilityRule> getRules();

    @Override
    public ConsultationResponse evaluate(ConsultationRequest request) {
        Map<String, Boolean> answers = request.getAnswers().stream()
                .collect(Collectors.toMap(a -> a.getQuestionId().toLowerCase(), AnswerDto::getValue));

        List<EligibilityRule> matchedRules = getRules().stream()
                .filter(rule -> rule.fires(answers))
                .toList();

        ConsultationStatus status = matchedRules.stream()
                .map(EligibilityRule::getSeverity)
                .max(Comparator.naturalOrder())
                .map(RuleSeverity::toConsultationStatus)
                .orElse(ConsultationStatus.APPROVED);

        List<String> clinicalNotes = matchedRules.stream()
                .map(EligibilityRule::getReason)
                .toList();

        return ConsultationResponse.builder()
                .consultationId(UUID.randomUUID().toString())
                .productId(getSupportedProductId())
                .eligible(status == ConsultationStatus.APPROVED || status == ConsultationStatus.APPROVED_WITH_NOTES)
                .status(status)
                .clinicalNotes(clinicalNotes)
                .customerId(request.getCustomerId())
                .build();
    }
}
