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

    public FutureValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    public FutureValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

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
