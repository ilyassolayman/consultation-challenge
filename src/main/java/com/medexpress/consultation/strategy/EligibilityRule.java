package com.medexpress.consultation.strategy;

import java.util.Map;
import java.util.function.Predicate;

public class EligibilityRule {

    private final Predicate<Map<String, Boolean>> condition;
    private final String reason;
    private final RuleSeverity severity;

    public EligibilityRule(Predicate<Map<String, Boolean>> condition, String reason, RuleSeverity severity) {
        this.condition = condition;
        this.reason = reason;
        this.severity = severity;
    }

    public boolean fires(Map<String, Boolean> answers) {
        return condition.test(answers);
    }

    public String getReason() {
        return reason;
    }

    public RuleSeverity getSeverity() {
        return severity;
    }
}
