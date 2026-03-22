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
package org.rulii.validation.rules.numeric;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.StringUtils;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;

import java.util.List;

/**
 * Validation Rule to make sure the value is numeric.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must be numeric.")
public class NumericValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "numericValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must be numeric.";

    private final boolean allowSpace;

    /**
     * Constructs a NumericValidationRule with the specified binding name.
     *
     * @param bindingName the name of the binding
     */
    public NumericValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, false);
    }

    /**
     * Constructs a NumericValidationRule with the specified binding name, error code, and option to allow spaces in the value.
     *
     * @param bindingName the name of the binding
     * @param errorCode the error code to be used for validation failure
     * @param allowSpace flag indicating whether spaces are allowed in the value
     */
    public NumericValidationRule(String bindingName, String errorCode, boolean allowSpace) {
        this(bindingName, errorCode, Severity.ERROR, null, allowSpace);
    }

    /**
     * Constructs a NumericValidationRule with the specified parameters.
     *
     * @param bindingName the name of the binding
     * @param errorCode the error code to be used for validation failure
     * @param severity the severity of the error
     * @param errorMessage the error message that will be displayed if the validation rule fails
     * @param allowSpace flag indicating whether spaces are allowed in the value
     */
    public NumericValidationRule(String bindingName, String errorCode, Severity severity,
                                 String errorMessage, boolean allowSpace) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowSpace = allowSpace;
    }

    /**
     * Represents a validation rule specifically designed for checking numeric values.
     *
     * @param bindingSupplier The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode The error code associated with the validation rule.
     * @param severity The severity of the error.
     * @param errorMessage The error message that will be displayed if the validation rule fails.
     * @param allowSpace Flag indicating whether spaces are allowed in the numeric value.
     */
    public NumericValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                                 String errorMessage, boolean allowSpace) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowSpace = allowSpace;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("UpperCaseValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        return isAllowSpace() ? StringUtils.isNumericSpace((CharSequence) value) : StringUtils.isNumeric((CharSequence) value);
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
        return "NumericValidationRule{"
                + "allowSpace=" + isAllowSpace()
                + "}";
    }
}
