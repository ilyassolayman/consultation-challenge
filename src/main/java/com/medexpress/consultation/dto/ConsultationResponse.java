package com.medexpress.consultation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationResponse {

    private String consultationId;
    private String productId;
    private boolean eligible;
    private ConsultationStatus status;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> clinicalNotes;
    private String customerId;
}
