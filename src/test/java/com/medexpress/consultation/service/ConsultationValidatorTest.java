package com.medexpress.consultation.service;

import com.medexpress.consultation.dto.*;
import com.medexpress.consultation.exception.InvalidConsultationRequestException;
import com.medexpress.consultation.repository.IQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultationValidatorTest {

    @Mock
    private IQuestionRepository questionRepository;

    private ConsultationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConsultationValidator(questionRepository);
        when(questionRepository.getQuestionsByProductId("pear-allergy-med")).thenReturn(List.of(
                QuestionDto.builder().id("q1").text("Question one?").build(),
                QuestionDto.builder().id("q2").text("Question two?").build(),
                QuestionDto.builder().id("q3").text("Question three?").build()
        ));
    }

    // --- happy path ---

    @Test
    void validate_withAllRequiredIds_doesNotThrow() {
        ConsultationRequest request = buildRequest("q1", "q2", "q3");

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void validate_withMixedCaseIds_doesNotThrow() {
        ConsultationRequest request = buildRequest("Q1", "Q2", "Q3");

        assertDoesNotThrow(() -> validator.validate(request));
    }

    // --- duplicate IDs ---

    @Test
    void validate_withDuplicateId_throwsWithDuplicateError() {
        ConsultationRequest request = buildRequest("q1", "q1", "q2", "q3");

        InvalidConsultationRequestException ex = assertThrows(
                InvalidConsultationRequestException.class,
                () -> validator.validate(request));

        assertEquals(1, ex.getErrors().size());
        assertEquals(ValidationErrorCode.DUPLICATE_QUESTION_IDS, ex.getErrors().get(0).code());
        assertEquals(List.of("q1"), ex.getErrors().get(0).questionIds());
    }

    @Test
    void validate_withMultipleDuplicateIds_listsAllDuplicates() {
        ConsultationRequest request = buildRequest("q1", "q1", "q2", "q2", "q3");

        InvalidConsultationRequestException ex = assertThrows(
                InvalidConsultationRequestException.class,
                () -> validator.validate(request));

        List<String> duplicates = ex.getErrors().get(0).questionIds();
        assertTrue(duplicates.containsAll(List.of("q1", "q2")));
    }

    // --- unknown IDs ---

    @Test
    void validate_withUnknownId_throwsWithUnknownError() {
        ConsultationRequest request = buildRequest("q1", "q2", "q3", "q99");

        InvalidConsultationRequestException ex = assertThrows(
                InvalidConsultationRequestException.class,
                () -> validator.validate(request));

        assertEquals(1, ex.getErrors().size());
        assertEquals(ValidationErrorCode.UNKNOWN_QUESTION_IDS, ex.getErrors().get(0).code());
        assertEquals(List.of("q99"), ex.getErrors().get(0).questionIds());
    }

    @Test
    void validate_withMultipleUnknownIds_listsAllUnknown() {
        ConsultationRequest request = buildRequest("q1", "q99", "q100");

        InvalidConsultationRequestException ex = assertThrows(
                InvalidConsultationRequestException.class,
                () -> validator.validate(request));

        List<String> unknownIds = ex.getErrors().stream()
                .filter(e -> e.code() == ValidationErrorCode.UNKNOWN_QUESTION_IDS)
                .findFirst().orElseThrow()
                .questionIds();
        assertTrue(unknownIds.containsAll(List.of("q99", "q100")));
    }

    // --- missing IDs ---

    @Test
    void validate_withMissingId_throwsWithMissingError() {
        ConsultationRequest request = buildRequest("q1", "q2");

        InvalidConsultationRequestException ex = assertThrows(
                InvalidConsultationRequestException.class,
                () -> validator.validate(request));

        assertEquals(1, ex.getErrors().size());
        assertEquals(ValidationErrorCode.MISSING_REQUIRED_QUESTION_IDS, ex.getErrors().get(0).code());
        assertEquals(List.of("q3"), ex.getErrors().get(0).questionIds());
    }

    @Test
    void validate_withAllIdsMissing_listsAllMissing() {
        ConsultationRequest request = buildRequest("q1");

        InvalidConsultationRequestException ex = assertThrows(
                InvalidConsultationRequestException.class,
                () -> validator.validate(request));

        List<String> missing = ex.getErrors().stream()
                .filter(e -> e.code() == ValidationErrorCode.MISSING_REQUIRED_QUESTION_IDS)
                .findFirst().orElseThrow()
                .questionIds();
        assertTrue(missing.containsAll(List.of("q2", "q3")));
    }

    // --- multiple violations ---

    @Test
    void validate_withDuplicateAndUnknownAndMissing_reportsAllThreeErrors() {
        // q1 duplicated, q99 unknown, q3 missing
        ConsultationRequest request = buildRequest("q1", "q1", "q2", "q99");

        InvalidConsultationRequestException ex = assertThrows(
                InvalidConsultationRequestException.class,
                () -> validator.validate(request));

        List<ValidationErrorCode> codes = ex.getErrors().stream()
                .map(ValidationError::code)
                .toList();
        assertEquals(3, codes.size());
        assertTrue(codes.contains(ValidationErrorCode.DUPLICATE_QUESTION_IDS));
        assertTrue(codes.contains(ValidationErrorCode.UNKNOWN_QUESTION_IDS));
        assertTrue(codes.contains(ValidationErrorCode.MISSING_REQUIRED_QUESTION_IDS));
    }

    @Test
    void validate_withUnknownAndMissing_reportsBothErrors() {
        ConsultationRequest request = buildRequest("q1", "q2", "q99");

        InvalidConsultationRequestException ex = assertThrows(
                InvalidConsultationRequestException.class,
                () -> validator.validate(request));

        List<ValidationErrorCode> codes = ex.getErrors().stream()
                .map(ValidationError::code)
                .toList();
        assertEquals(2, codes.size());
        assertTrue(codes.contains(ValidationErrorCode.UNKNOWN_QUESTION_IDS));
        assertTrue(codes.contains(ValidationErrorCode.MISSING_REQUIRED_QUESTION_IDS));
    }

    // --- error descriptions ---

    @Test
    void validate_errorIncludesHumanReadableDescription() {
        ConsultationRequest request = buildRequest("q1", "q2");

        InvalidConsultationRequestException ex = assertThrows(
                InvalidConsultationRequestException.class,
                () -> validator.validate(request));

        String description = ex.getErrors().get(0).description();
        assertTrue(description.contains("q3"));
    }

    // --- helpers ---

    private ConsultationRequest buildRequest(String... questionIds) {
        List<AnswerDto> answers = java.util.Arrays.stream(questionIds)
                .map(id -> AnswerDto.builder().questionId(id).value(false).build())
                .toList();
        return ConsultationRequest.builder()
                .productId("pear-allergy-med")
                .customerId("test-customer")
                .answers(answers)
                .build();
    }
}
