package com.medexpress.consultation.repository;

import com.medexpress.consultation.dto.QuestionDto;

import java.util.List;

public interface IQuestionRepository {

    List<QuestionDto> getQuestionsByProductId(String productId);
}
