/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rulii.validation.rules.asssert;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRule;

import java.util.List;
import java.util.Objects;

/**
 * Validation Rule to make sure the value does not match input value.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must not match desired value.")
public class AssertNotEqualsValidationRule extends ValueValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = List.of(Object.class);

    public static final String ERROR_CODE       = "assertNotEqualsValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value [{0}] must not equal [{1}].";

    private final Object value;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @param value    the value that the supplied value must not equal
     * @return a new {@link AssertNotEqualsValidationRuleBuilder}
     */
    public static AssertNotEqualsValidationRuleBuilder builder(Function<?> function, Object value) {
        return new AssertNotEqualsValidationRuleBuilder(function, value);
    }

    AssertNotEqualsValidationRule(Function<?> valueFunction, String errorCode, Severity severity,
                                  String errorMessage, String valueName, Object value) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        this.value = value;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        return !Objects.equals(this.value, value);
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("value", this.value);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "AssertNotEqualsValidationRule";
    }
}
