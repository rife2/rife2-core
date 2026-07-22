/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

public class DatasourceEnabledIfExtension implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
        "@DatasourceEnabledIf is not present");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<AnnotatedElement> element = context.getElement();
        if (element.isPresent()) {
            Optional<DatasourceEnabledIf> enabled = AnnotationSupport.findAnnotation(element.get(), DatasourceEnabledIf.class);
            if (enabled.isPresent()) {
                if (TestDatasources.ACTIVE_DATASOURCES.containsKey(enabled.get().value())) {
                    return ConditionEvaluationResult.enabled("Datasource is active");
                } else {
                    return ConditionEvaluationResult.disabled("Datasource is inactive");
                }
            }
        }

        return ENABLED;
    }
}