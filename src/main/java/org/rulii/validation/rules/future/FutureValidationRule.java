/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
package org.rulii.validation.rules.future;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.util.TimeComparator;
import org.rulii.validation.*;

import java.sql.Date;
import java.time.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Validation Rule to make sure the value is in the future.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must be in the future.")
public class FutureValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = Arrays.asList(Calendar.class, Date.class, java.util.Date.class, Instant.class,
            LocalDate.class, LocalDateTime.class, LocalTime.class, MonthDay.class, OffsetDateTime.class,
            OffsetTime.class, Year.class, YearMonth.class, ZonedDateTime.class);

    public static final String ERROR_CODE       = "futureValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must be in the future. Current clock {1}.";

    /**
     * Constructs a new FutureValidationRule with the provided binding name.
     *
     * @param bindingName the name of the binding to apply the rule to
     */
    public FutureValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    /**
     * Constructs a new FutureValidationRule with the provided binding name, error code, and sets severity to ERROR if not specified.
     *
     * @param bindingName the name of the binding to apply the rule to
     * @param errorCode the error code to be used for this rule
     */
    public FutureValidationRule(String bindingName, String errorCode) {
        this(bindingName, errorCode, Severity.ERROR, null);
    }

    /**
     * Constructs a new FutureValidationRule with the provided parameters.
     *
     * @param bindingName the name of the binding to apply the rule to
     * @param errorCode the error code to be used for this rule
     * @param severity the severity level of the error
     * @param errorMessage the error message to be displayed if the validation rule fails
     */
    public FutureValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    /**
     * Constructs a new FutureValidationRule with the provided parameters.
     *
     * @param bindingSupplier The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode The error code associated with the validation rule.
     * @param severity The severity of the error.
     * @param errorMessage The error message that will be displayed if the validation rule fails.
     */
    public FutureValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity, String errorMessage) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {

        if (value == null) return true;

        Integer result = TimeComparator.compare(value, ruleContext.getClock());

        if (result == null) {
            throw new ValidationRuleException("FutureRule only applies to Date related classes. Supported Types ["
                    + SUPPORTED_TYPES + "] Supplied Class [" + value.getClass() + "] value [" + value + "]");
        }

        return result > 0;
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("clock", ruleContext.getClock());
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "FutureValidationRule";
    }
}
