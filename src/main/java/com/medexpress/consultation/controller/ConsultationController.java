package com.medexpress.consultation.controller;

import com.medexpress.consultation.dto.ConsultationRequest;
import com.medexpress.consultation.dto.ConsultationResponse;
import com.medexpress.consultation.dto.QuestionDto;
import com.medexpress.consultation.service.IConsultationService;
import com.medexpress.consultation.service.IQuestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ConsultationController {

    private final IQuestionService questionService;
    private final IConsultationService consultationService;

    public ConsultationController(IQuestionService questionService,
                                  IConsultationService consultationService) {
        this.questionService = questionService;
        this.consultationService = consultationService;
    }

    @GetMapping("/products/{productId}/questions")
    public ResponseEntity<List<QuestionDto>> getQuestions(@PathVariable String productId) {
        return ResponseEntity.ok(questionService.getQuestions(productId));
    }

    @GetMapping("/consultations/{consultationId}")
    public ResponseEntity<ConsultationResponse> getConsultation(@PathVariable String consultationId) {
        return ResponseEntity.ok(consultationService.getConsultation(consultationId));
    }

    @PostMapping("/consultations")
    public ResponseEntity<ConsultationResponse> submitConsultation(
            @Valid @RequestBody ConsultationRequest request) {
        ConsultationResponse response = consultationService.submitConsultation(request);
        URI location = URI.create("/api/v1/consultations/" + response.getConsultationId());
        return ResponseEntity.created(location).body(response);
    }

}
