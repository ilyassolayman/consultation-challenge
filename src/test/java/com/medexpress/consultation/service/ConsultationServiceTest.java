package com.medexpress.consultation.service;

import com.medexpress.consultation.dto.AnswerDto;
import com.medexpress.consultation.dto.ConsultationRequest;
import com.medexpress.consultation.dto.ConsultationResponse;
import com.medexpress.consultation.dto.ConsultationStatus;
import com.medexpress.consultation.repository.IConsultationRepository;
import com.medexpress.consultation.exception.ConsultationNotFoundException;
import com.medexpress.consultation.exception.ProductNotFoundException;
import com.medexpress.consultation.strategy.EligibilityStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock
    private IConsultationRepository consultationRepository;

    @Mock
    private EligibilityStrategy strategy;

    private ConsultationService service;

    @BeforeEach
    void setUp() {
        when(strategy.getSupportedProductId()).thenReturn("pear-allergy-med");
        service = new ConsultationService(consultationRepository, List.of(strategy));
    }

    // --- getConsultation ---

    @Test
    void getConsultation_returnsConsultation() {
        ConsultationResponse response = buildResponse("pear-allergy-med");
        when(consultationRepository.findById("cons-123")).thenReturn(Optional.of(response));

        ConsultationResponse result = service.getConsultation("cons-123");

        assertSame(response, result);
    }

    @Test
    void getConsultation_throwsForUnknownId() {
        when(consultationRepository.findById("unknown-id")).thenReturn(Optional.empty());

        ConsultationNotFoundException ex = assertThrows(
                ConsultationNotFoundException.class,
                () -> service.getConsultation("unknown-id")
        );

        assertEquals("Consultation not found: unknown-id", ex.getMessage());
    }

    // --- submitConsultation: routing ---

    @Test
    void submitConsultation_routesToCorrectStrategy() {
        ConsultationRequest request = buildRequest("pear-allergy-med");
        ConsultationResponse response = buildResponse("pear-allergy-med");
        when(strategy.evaluate(request)).thenReturn(response);

        service.submitConsultation(request);

        verify(strategy).evaluate(request);
    }

    @Test
    void submitConsultation_throwsForUnknownProductId() {
        ConsultationRequest request = buildRequest("unknown-product");

        ProductNotFoundException ex = assertThrows(
                ProductNotFoundException.class,
                () -> service.submitConsultation(request)
        );

        assertEquals("Product not found: unknown-product", ex.getMessage());
        verifyNoInteractions(consultationRepository);
    }

    // --- submitConsultation: persistence ---

    @Test
    void submitConsultation_savesResponseToRepository() {
        ConsultationRequest request = buildRequest("pear-allergy-med");
        ConsultationResponse response = buildResponse("pear-allergy-med");
        when(strategy.evaluate(request)).thenReturn(response);

        service.submitConsultation(request);

        verify(consultationRepository).save(response);
    }

    @Test
    void submitConsultation_returnsResponseFromStrategy() {
        ConsultationRequest request = buildRequest("pear-allergy-med");
        ConsultationResponse response = buildResponse("pear-allergy-med");
        when(strategy.evaluate(request)).thenReturn(response);

        ConsultationResponse result = service.submitConsultation(request);

        assertSame(response, result);
    }

    // --- Helpers ---

    private ConsultationRequest buildRequest(String productId) {
        return ConsultationRequest.builder()
                .productId(productId)
                .customerId("test-customer")
                .answers(List.of(AnswerDto.builder().questionId("q1").value(false).build()))
                .build();
    }

    private ConsultationResponse buildResponse(String productId) {
        return ConsultationResponse.builder()
                .consultationId("cons-123")
                .productId(productId)
                .eligible(true)
                .status(ConsultationStatus.APPROVED)
                .clinicalNotes(List.of())
                .build();
    }
}
