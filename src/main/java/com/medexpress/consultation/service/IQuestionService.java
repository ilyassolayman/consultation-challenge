package com.medexpress.consultation.service;

import com.medexpress.consultation.dto.QuestionDto;

import java.util.List;

public interface IQuestionService {

    List<QuestionDto> getQuestions(String productId);
}
