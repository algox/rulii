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
package org.rulii.validation.rules.decimal;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Validation Rule to make sure the value is a decimal.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
@Description("Value must be a valid decimal.")
public class DecimalValidationRule extends BindingValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "rulii.validation.rules.DecimalValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value must be a valid decimal. Given {0}.";

    private final boolean allowSpace;

    public DecimalValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, true);
    }

    public DecimalValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage, boolean allowSpace) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowSpace = allowSpace;
    }

    public DecimalValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                                 String errorMessage, boolean allowSpace) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowSpace = allowSpace;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("DecimalValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        String stringValue = isAllowSpace() ? value.toString().trim() : value.toString();

        try {
            new BigDecimal(stringValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public boolean isAllowSpace() {
        return allowSpace;
    }

    @Override
    public String toString() {
        return "DecimalValidationRule{" +
                "allowSpace=" + allowSpace +
                '}';
    }
}
