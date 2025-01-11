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
 */
@Rule
@Description("Value is less than 0.")
public class NegativeValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = Arrays.asList(Number.class, CharSequence.class);

    public static final String ERROR_CODE       = "rulii.validation.rules.NegativeValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value must be less than 0. Given {0}.";

    public NegativeValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    public NegativeValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

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
        return result == null ? true : result < 0;
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
