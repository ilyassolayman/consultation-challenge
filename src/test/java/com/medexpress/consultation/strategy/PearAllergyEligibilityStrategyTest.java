package com.medexpress.consultation.strategy;

import com.medexpress.consultation.dto.AnswerDto;
import com.medexpress.consultation.dto.ConsultationRequest;
import com.medexpress.consultation.dto.ConsultationResponse;
import com.medexpress.consultation.dto.ConsultationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PearAllergyEligibilityStrategyTest {

    private PearAllergyEligibilityStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PearAllergyEligibilityStrategy();
    }

    // --- getSupportedProductId ---

    @Test
    void getSupportedProductId_returnsPearAllergyMed() {
        assertEquals("pear-allergy-med", strategy.getSupportedProductId());
    }

    // --- APPROVED: no rules fire ---

    @Test
    void evaluate_withNoContraindications_returnsApproved() {
        ConsultationRequest request = requestWithAnswers(false, false, false);

        ConsultationResponse response = strategy.evaluate(request);

        assertTrue(response.isEligible());
        assertEquals(ConsultationStatus.APPROVED, response.getStatus());
        assertEquals("pear-allergy-med", response.getProductId());
        assertNotNull(response.getConsultationId());
        assertTrue(response.getClinicalNotes().isEmpty());
    }

    // --- APPROVED_WITH_NOTES: only LOW rule fires (q2) ---

    @Test
    void evaluate_whenOnlyAntihistamines_returnsApprovedWithNotes() {
        ConsultationRequest request = requestWithAnswers(false, true, false);

        ConsultationResponse response = strategy.evaluate(request);

        assertTrue(response.isEligible());
        assertEquals(ConsultationStatus.APPROVED_WITH_NOTES, response.getStatus());
        assertEquals(1, response.getClinicalNotes().size());
        assertTrue(response.getClinicalNotes().get(0).toLowerCase().contains("antihistamine"));
    }

    // --- PENDING_CLINICAL_REVIEW: only MEDIUM rule fires (q3) ---

    @Test
    void evaluate_whenRespiratoryHistory_returnsPendingClinicalReview() {
        ConsultationRequest request = requestWithAnswers(false, false, true);

        ConsultationResponse response = strategy.evaluate(request);

        assertFalse(response.isEligible());
        assertEquals(ConsultationStatus.PENDING_CLINICAL_REVIEW, response.getStatus());
        assertEquals(1, response.getClinicalNotes().size());
        assertTrue(response.getClinicalNotes().get(0).toLowerCase().contains("respiratory"));
    }

    // --- REJECTED: CRITICAL rule fires (q1) ---

    @Test
    void evaluate_whenPriorAdverseReaction_returnsRejected() {
        ConsultationRequest request = requestWithAnswers(true, false, false);

        ConsultationResponse response = strategy.evaluate(request);

        assertFalse(response.isEligible());
        assertEquals(ConsultationStatus.REJECTED, response.getStatus());
        assertEquals(1, response.getClinicalNotes().size());
        assertTrue(response.getClinicalNotes().get(0).toLowerCase().contains("adverse reaction"));
    }

    // --- Multiple rules: max severity wins, all reasons collected ---

    @Test
    void evaluate_whenQ1AndQ3BothFire_rejectsAndReturnsBothReasons() {
        ConsultationRequest request = requestWithAnswers(true, false, true);

        ConsultationResponse response = strategy.evaluate(request);

        assertFalse(response.isEligible());
        assertEquals(ConsultationStatus.REJECTED, response.getStatus());
        assertEquals(2, response.getClinicalNotes().size());
        assertTrue(response.getClinicalNotes().stream().anyMatch(r -> r.toLowerCase().contains("adverse reaction")));
        assertTrue(response.getClinicalNotes().stream().anyMatch(r -> r.toLowerCase().contains("respiratory")));
    }

    @Test
    void evaluate_whenQ2AndQ3BothFire_pendingClinicalReviewWithBothReasons() {
        ConsultationRequest request = requestWithAnswers(false, true, true);

        ConsultationResponse response = strategy.evaluate(request);

        assertFalse(response.isEligible());
        assertEquals(ConsultationStatus.PENDING_CLINICAL_REVIEW, response.getStatus());
        assertEquals(2, response.getClinicalNotes().size());
    }

    // --- Response always has a unique consultationId ---

    @Test
    void evaluate_alwaysGeneratesAConsultationId() {
        ConsultationRequest r1 = requestWithAnswers(false, false, false);
        ConsultationRequest r2 = requestWithAnswers(false, false, false);

        String id1 = strategy.evaluate(r1).getConsultationId();
        String id2 = strategy.evaluate(r2).getConsultationId();

        assertNotNull(id1);
        assertNotNull(id2);
        assertFalse(id1.equals(id2), "Each evaluation should produce a unique consultationId");
    }

    // --- Helper ---

    private ConsultationRequest requestWithAnswers(boolean q1, boolean q2, boolean q3) {
        return ConsultationRequest.builder()
                .productId("pear-allergy-med")
                .customerId("test-customer")
                .answers(List.of(
                        AnswerDto.builder().questionId("q1").value(q1).build(),
                        AnswerDto.builder().questionId("q2").value(q2).build(),
                        AnswerDto.builder().questionId("q3").value(q3).build()
                ))
                .build();
    }
}
