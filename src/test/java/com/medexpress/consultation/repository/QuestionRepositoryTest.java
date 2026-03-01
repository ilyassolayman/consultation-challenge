package com.medexpress.consultation.repository;

import com.medexpress.consultation.dto.QuestionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionRepositoryTest {

    private QuestionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new QuestionRepository();
        repository.init();
    }

    // --- Known product ---

    @Test
    void getQuestionsByProductId_returnsThreeQuestionsForPearAllergyMed() {
        List<QuestionDto> questions = repository.getQuestionsByProductId("pear-allergy-med");

        assertEquals(3, questions.size());
    }

    @Test
    void getQuestionsByProductId_returnsQuestionsWithCorrectIds() {
        List<QuestionDto> questions = repository.getQuestionsByProductId("pear-allergy-med");

        assertEquals("q1", questions.get(0).getId());
        assertEquals("q2", questions.get(1).getId());
        assertEquals("q3", questions.get(2).getId());
    }

    @Test
    void getQuestionsByProductId_returnsQuestionsWithNonBlankText() {
        List<QuestionDto> questions = repository.getQuestionsByProductId("pear-allergy-med");

        questions.forEach(q -> {
            assertNotNull(q.getText());
            assertTrue(q.getText().length() > 0, "Question " + q.getId() + " must have non-blank text");
        });
    }

    // --- Unknown product ---

    @Test
    void getQuestionsByProductId_returnsEmptyListForUnknownProduct() {
        List<QuestionDto> questions = repository.getQuestionsByProductId("unknown-product");

        assertNotNull(questions);
        assertTrue(questions.isEmpty());
    }

    @Test
    void getQuestionsByProductId_returnsEmptyListForNullProductId() {
        List<QuestionDto> questions = repository.getQuestionsByProductId(null);

        assertNotNull(questions);
        assertTrue(questions.isEmpty());
    }
}
