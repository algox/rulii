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
package org.rulii.validation.rules.duration;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.time.Duration;
import java.util.List;

/**
 * Validation Rule to make sure a {@link Duration} is at most the given maximum (inclusive by
 * default; strictly shorter when exclusive).
 *
 * @author Max Arulananthan
 * @since 2.2
 */
@Rule
@Description("Duration must not be longer than the given maximum.")
public class DurationMaxValidationRule extends ValueValidationRule {

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(Duration.class);

    public static final String ERROR_CODE       = "durationMaxValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must not be longer than {1}.";

    private final Duration max;
    private final boolean inclusive;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the duration to validate
     * @param max the maximum duration
     * @return a new {@link DurationMaxValidationRuleBuilder}
     */
    public static DurationMaxValidationRuleBuilder builder(Function<?> function, Duration max) {
        return new DurationMaxValidationRuleBuilder(function, max);
    }

    DurationMaxValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage,
                              String valueName, Duration max, boolean inclusive) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        Assert.notNull(max, "max cannot be null.");
        this.max = max;
        this.inclusive = inclusive;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof Duration duration))
            throw new ValidationRuleException("DurationMaxValidationRule only applies to Durations."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        int comparison = duration.compareTo(max);
        return inclusive ? comparison <= 0 : comparison < 0;
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("max", max).param("inclusive", inclusive);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public Duration getMax() {
        return max;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    @Override
    public String describe() {
        return getValueName() + " must be " + (inclusive ? "at most " : "shorter than ") + max;
    }

    @Override
    public String toString() {
        return "DurationMaxValidationRule{max=" + max + ", inclusive=" + inclusive + '}';
    }
}
