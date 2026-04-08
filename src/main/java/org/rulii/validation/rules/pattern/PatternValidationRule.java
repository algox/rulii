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
package org.rulii.validation.rules.pattern;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.validation.RegexValidator;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.util.List;

/**
 * Validation Rule to make sure the value must match one of the given regex patterns.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must match the given regex pattern.")
public class PatternValidationRule extends ValueValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "patternValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must match the required regex pattern {1}.";

    private final String pattern;
    private final boolean caseSensitive;
    private final RegexValidator validator;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @param pattern  the regex pattern that the value must match
     * @return a new {@link PatternValidationRuleBuilder}
     */
    public static PatternValidationRuleBuilder builder(Function<?> function, String pattern) {
        return new PatternValidationRuleBuilder(function, pattern);
    }

    PatternValidationRule(Function<?> valueFunction, String errorCode, Severity severity,
                          String errorMessage, String valueName, boolean caseSensitive, String pattern) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        Assert.notNull(pattern, "pattern cannot be null.");
        this.pattern = pattern;
        this.caseSensitive = caseSensitive;
        this.validator = new RegexValidator(pattern, caseSensitive);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("PatternValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        return validator.isValid(value.toString());
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("pattern", pattern);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    @Override
    public String toString() {
        return "PatternValidationRule{" +
                "pattern=" + pattern +
                ", caseSensitive=" + caseSensitive +
                '}';
    }
}
