package com.medexpress.consultation.service;

import com.medexpress.consultation.dto.ConsultationRequest;
import com.medexpress.consultation.dto.ValidationError;
import com.medexpress.consultation.dto.ValidationErrorCode;
import com.medexpress.consultation.exception.InvalidConsultationRequestException;
import com.medexpress.consultation.repository.IQuestionRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class ConsultationValidator {

    private final IQuestionRepository questionRepository;

    public ConsultationValidator(IQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public void validate(ConsultationRequest request) {
        List<String> requiredQuestionIds = getRequiredQuestionIds(request.getProductId());
        List<String> submittedIds = getSubmittedQuestionIds(request);

        List<ValidationError> violations = new ArrayList<>();

        checkDuplicates(submittedIds).ifPresent(violations::add);
        checkUnknownIds(submittedIds, requiredQuestionIds).ifPresent(violations::add);
        checkMissingIds(submittedIds, requiredQuestionIds).ifPresent(violations::add);

        if (!violations.isEmpty()) {
            throw new InvalidConsultationRequestException(violations);
        }
    }

    private List<String> getRequiredQuestionIds(String productId) {
        return questionRepository.getQuestionsByProductId(productId)
                .stream()
                .map(q -> q.getId().toLowerCase())
                .toList();
    }

    private List<String> getSubmittedQuestionIds(ConsultationRequest request) {
        return request.getAnswers().stream()
                .map(a -> a.getQuestionId().toLowerCase())
                .toList();
    }

    private Optional<ValidationError> checkDuplicates(List<String> submittedIds) {
        Set<String> seen = new HashSet<>();
        List<String> duplicates = submittedIds.stream()
                .filter(id -> !seen.add(id))
                .distinct()
                .toList();
        if (duplicates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ValidationError(
                ValidationErrorCode.DUPLICATE_QUESTION_IDS,
                "Duplicate question IDs submitted: " + duplicates,
                duplicates));
    }

    private Optional<ValidationError> checkUnknownIds(List<String> submittedIds, List<String> requiredQuestionIds) {
        Set<String> requiredQuestionIdSet = new HashSet<>(requiredQuestionIds);
        List<String> unknown = submittedIds.stream()
                .distinct()
                .filter(id -> !requiredQuestionIdSet.contains(id))
                .sorted()
                .toList();
        if (unknown.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ValidationError(
                ValidationErrorCode.UNKNOWN_QUESTION_IDS,
                "Unknown question IDs: " + unknown,
                unknown));
    }

    private Optional<ValidationError> checkMissingIds(List<String> submittedIds, List<String> requiredQuestionIds) {
        Set<String> submittedSet = new HashSet<>(submittedIds);
        List<String> missing = requiredQuestionIds.stream()
                .filter(id -> !submittedSet.contains(id))
                .toList();
        if (missing.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ValidationError(
                ValidationErrorCode.MISSING_REQUIRED_QUESTION_IDS,
                "Missing required question IDs: " + missing,
                missing));
    }
}
