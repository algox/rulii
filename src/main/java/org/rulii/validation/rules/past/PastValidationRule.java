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
package org.rulii.validation.rules.past;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.model.function.Function;
import org.rulii.util.TimeComparator;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.sql.Date;
import java.time.*;
import java.util.Calendar;
import java.util.List;

/**
 * Validation Rule to make sure the value is in the past.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must be in the past.")
public class PastValidationRule extends ValueValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(Calendar.class, Date.class, java.util.Date.class, Instant.class,
            LocalDate.class, LocalDateTime.class, LocalTime.class, MonthDay.class, OffsetDateTime.class,
            OffsetTime.class, Year.class, YearMonth.class, ZonedDateTime.class);

    public static final String ERROR_CODE       = "pastValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must be in the past. Current clock {1}.";

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the date/time value to validate
     * @return a new {@link PastValidationRuleBuilder}
     */
    public static PastValidationRuleBuilder builder(Function<?> function) {
        return new PastValidationRuleBuilder(function);
    }

    PastValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {

        if (value == null) return true;

        Integer result = TimeComparator.compare(value, ruleContext.getClock());

        if (result == null) {
            throw new ValidationRuleException("PastValidationRule only applies to Date related classes. Like ["
                    + SUPPORTED_TYPES + "] Supplied Class [" + value.getClass() + "] value [" + value + "]");
        }

        return result < 0;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    protected void customizeViolation(RuleContext context, RuleViolationBuilder builder) {
        builder.param("clock", context.getClock());
    }

    @Override
    public String toString() {
        return "PastValidationRule";
    }
}
