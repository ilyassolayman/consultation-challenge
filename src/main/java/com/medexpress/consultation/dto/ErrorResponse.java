package com.medexpress.consultation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ValidationError> errors;
    private Instant timestamp;
}
