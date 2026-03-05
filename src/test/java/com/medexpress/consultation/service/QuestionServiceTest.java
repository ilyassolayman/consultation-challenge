package com.medexpress.consultation.service;

import com.medexpress.consultation.dto.QuestionDto;
import com.medexpress.consultation.exception.ProductNotFoundException;
import com.medexpress.consultation.repository.IQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private IQuestionRepository questionRepository;

    private QuestionService service;

    @BeforeEach
    void setUp() {
        service = new QuestionService(questionRepository);
    }

    @Test
    void getQuestions_delegatesToRepository() {
        List<QuestionDto> expected = List.of(
                QuestionDto.builder().id("q1").text("Question one?").build()
        );
        when(questionRepository.getQuestionsByProductId("pear-allergy-med")).thenReturn(expected);

        List<QuestionDto> result = service.getQuestions("pear-allergy-med");

        assertEquals(expected, result);
        verify(questionRepository).getQuestionsByProductId("pear-allergy-med");
    }

    @Test
    void getQuestions_throwsForUnknownProductId() {
        when(questionRepository.getQuestionsByProductId("unknown-product")).thenReturn(List.of());

        ProductNotFoundException ex = assertThrows(
                ProductNotFoundException.class,
                () -> service.getQuestions("unknown-product")
        );

        assertEquals("Product not found for productId: unknown-product", ex.getMessage());
    }
}
