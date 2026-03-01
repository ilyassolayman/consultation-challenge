package com.medexpress.consultation.service;

import com.medexpress.consultation.dto.QuestionDto;
import com.medexpress.consultation.exception.ProductNotFoundException;
import com.medexpress.consultation.repository.IQuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class QuestionService implements IQuestionService {

    private final IQuestionRepository questionRepository;

    public QuestionService(IQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public List<QuestionDto> getQuestions(String productId) {
        log.info("Retrieving questions for productId={}", productId);
        List<QuestionDto> questions = questionRepository.getQuestionsByProductId(productId);
        if (questions.isEmpty()) {
            throw new ProductNotFoundException(productId);
        }
        return questions;
    }
}
