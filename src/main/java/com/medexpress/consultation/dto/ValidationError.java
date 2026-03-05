package com.medexpress.consultation.dto;

import java.util.List;

public record ValidationError(ValidationErrorCode code, String description, List<String> questionIds) {
}
