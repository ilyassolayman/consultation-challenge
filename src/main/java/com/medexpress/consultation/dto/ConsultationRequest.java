package com.medexpress.consultation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRequest {

    @NotBlank
    private String productId;

    @NotBlank
    private String customerId;

    @Valid
    @NotEmpty
    private List<AnswerDto> answers;
}
