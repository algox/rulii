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
package org.rulii.validation.rules.max;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.util.NumberComparator;
import org.rulii.validation.*;

import java.util.List;

/**
 * Validation Rule to make sure the value is less the desired Max.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value is less than the desired Max.")
public class MaxValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(Number.class, CharSequence.class);

    public static final String ERROR_CODE       = "maxValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must not be less than or equal to {1}.";

    private final long max;

    public MaxValidationRule(String bindingName, long max) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, max);
    }

    public MaxValidationRule(String bindingName, String errorCode, Severity severity,
                             String errorMessage, long max) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.max = max;
    }

    public MaxValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity, String errorMessage, long max) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.max = max;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {

        if (value == null) return true;

        Number number = null;

        if (value instanceof Number) number = (Number) value;
        if (value instanceof CharSequence) {
            try {
                number = Long.valueOf(value.toString());
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (number == null)
            throw new ValidationRuleException("MaxValidationRule only applies to Numbers/CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        Integer result = NumberComparator.compare(number, max);
        return result == null || result <= 0;
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("max", max);
    }

    public long getMax() {
        return max;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "MaxValidationRule{" +
                "max=" + max +
                '}';
    }
}
