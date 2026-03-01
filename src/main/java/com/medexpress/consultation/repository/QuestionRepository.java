package com.medexpress.consultation.repository;

import com.medexpress.consultation.dto.QuestionDto;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class QuestionRepository implements IQuestionRepository {

    private final Map<String, List<QuestionDto>> questionsByProduct = new HashMap<>();

    @PostConstruct
    void init() {
        questionsByProduct.put("pear-allergy-med", List.of(
            QuestionDto.builder()
                .id("q1")
                .text("Have you ever experienced an adverse reaction (e.g. anaphylaxis, severe rash, or swelling) after consuming Genovian Pear or products containing Genovian Pear extract?")
                .build(),
            QuestionDto.builder()
                .id("q2")
                .text("Are you currently taking antihistamines or any other allergy medication prescribed by a doctor?")
                .build(),
            QuestionDto.builder()
                .id("q3")
                .text("Do you have a history of asthma or a respiratory condition that has previously been triggered by food allergens?")
                .build()
        ));
    }

    public List<QuestionDto> getQuestionsByProductId(String productId) {
        return questionsByProduct.getOrDefault(productId, List.of());
    }
}
