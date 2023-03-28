package com.datastax.oss.quarkus.test;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class CheckAmd64Condition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        final var optional = findAnnotation(context.getElement(), SkipIfNotAmd64.class);
        return optional.map((ann) -> {
            String archStr = System.getProperty("os.arch");
            return archStr.equals("amd64") ?
                    enabled("amd64 architecture found") :
                    disabled(String.format("{} architecture found, skipping test", archStr));
        }).orElse(enabled("No annotation, not checking platform"));
    }
}
