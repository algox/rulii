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
package org.rulii.validation.rules.negative;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.util.NumberComparator;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Validation Rule to make sure the value is less than 0.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must be less than 0.")
public class NegativeValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = Arrays.asList(Number.class, CharSequence.class);

    public static final String ERROR_CODE       = "negativeValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must be less than 0.";

    /**
     * Constructs a NegativeValidationRule with the specified binding name, default error code, error severity,
     * and error message.
     *
     * @param bindingName the name of the binding to apply the validation rule
     */
    public NegativeValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    /**
     * Constructs a NegativeValidationRule with the specified binding name and error code.
     * The rule will be set to have an error severity and no custom error message.
     *
     * @param bindingName the name of the binding to apply the validation rule
     * @param errorCode the error code to be used when the rule is violated
     */
    public NegativeValidationRule(String bindingName, String errorCode) {
        this(bindingName, errorCode, Severity.ERROR, null);
    }

    /**
     * Constructs a NegativeValidationRule with the specified binding name, error code, severity, and error message.
     *
     * @param bindingName   the name of the binding to apply the validation rule
     * @param errorCode     the error code to be used when the rule is violated
     * @param severity      the severity of the error
     * @param errorMessage  the error message to display if the validation rule fails
     */
    public NegativeValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    /**
     * Represents a validation rule that checks if a value is negative.
     *
     * @param bindingSupplier The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode The error code associated with the validation rule.
     * @param severity The severity of the error.
     * @param errorMessage The error message that will be displayed if the validation rule fails.
     */
    public NegativeValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity, String errorMessage) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {

        if (value == null) return true;

        Number number = null;

        if (value instanceof Number) number = (Number) value;
        if (value instanceof CharSequence) {
            try {
                number = new BigDecimal(value.toString());
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (number == null)
            throw new ValidationRuleException("NegativeValidationRule only applies to Numbers/CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        Integer result = NumberComparator.signum(number);
        return result == null || result < 0;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "NegativeValidationRule";
    }
}
