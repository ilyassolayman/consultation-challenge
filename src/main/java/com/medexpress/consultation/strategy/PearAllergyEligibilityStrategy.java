package com.medexpress.consultation.strategy;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PearAllergyEligibilityStrategy extends AbstractEligibilityStrategy {

    private static final String PRODUCT_ID = "pear-allergy-med";

    private static final List<EligibilityRule> RULES = List.of(
            new EligibilityRule(
                    answers -> answers.getOrDefault("q1", false),
                    "Patient has reported a prior adverse reaction to Genovian Pear extract. Prescribing this medication is contraindicated.",
                    RuleSeverity.CRITICAL
            ),
            new EligibilityRule(
                    answers -> answers.getOrDefault("q3", false),
                    "Patient has a history of food-allergen-triggered respiratory conditions. A doctor will review the case before a prescription can be issued.",
                    RuleSeverity.MEDIUM
            ),
            new EligibilityRule(
                    answers -> answers.getOrDefault("q2", false),
                    "Patient is currently taking antihistamines or allergy medication. A note has been added for the reviewing doctor.",
                    RuleSeverity.LOW
            )
    );

    @Override
    public String getSupportedProductId() {
        return PRODUCT_ID;
    }

    @Override
    protected List<EligibilityRule> getRules() {
        return RULES;
    }
}
