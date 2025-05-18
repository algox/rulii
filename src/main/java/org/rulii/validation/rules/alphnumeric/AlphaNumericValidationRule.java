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
package org.rulii.validation.rules.alphnumeric;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.StringUtils;
import org.rulii.model.UnrulyException;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;

import java.util.List;

/**
 * Validation Rule to make sure the value only contains alphanumeric letters (or spaces).
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value can only contain unicode alphanumeric letters/spaces.")
public class AlphaNumericValidationRule extends BindingValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "alphaNumericValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must only contain alphanumeric letters.";

    private final boolean allowSpace;

    /**
     * Constructs a new AlphaNumericValidationRule.
     *
     * @param bindingName The name of the binding associated with this validation rule.
     */
    public AlphaNumericValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, true);
    }

    /**
     * Constructs a new AlphaNumericValidationRule.
     *
     * @param bindingName The name of the binding associated with this validation rule.
     * @param errorCode The error code for the validation rule.
     * @param allowSpace A boolean indicating whether spaces are allowed in the value.
     */
    public AlphaNumericValidationRule(String bindingName, String errorCode,  boolean allowSpace) {
        this(bindingName, errorCode, Severity.ERROR, null, allowSpace);
    }

    /**
     * Constructs a new AlphaNumericValidationRule.
     *
     * @param bindingName The name of the binding associated with this validation rule.
     * @param errorCode The error code for the validation rule.
     * @param severity The severity of the error.
     * @param errorMessage The error message that will be displayed if the validation rule fails.
     * @param allowSpace A boolean indicating whether spaces are allowed in the value.
     */
    public AlphaNumericValidationRule(String bindingName, String errorCode, Severity severity,
                                      String errorMessage, boolean allowSpace) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowSpace = allowSpace;
    }

    /**
     * Represents a validation rule for ensuring that the provided input consists of only alphanumeric characters.
     * Spaces may be allowed based on the specified parameter.
     * Extends BindingValidationRule for binding validation.
     *
     * @param bindingSupplier The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode The error code associated with the validation rule.
     * @param severity The severity of the error.
     * @param errorMessage The error message to display if the validation rule fails.
     * @param allowSpace A boolean indicating whether spaces are allowed in the input.
     */
    public AlphaNumericValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                                      String errorMessage, boolean allowSpace) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowSpace = allowSpace;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new UnrulyException("AlphaNumericValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        return isAllowSpace() ? StringUtils.isAlphanumericSpace((CharSequence) value) : StringUtils.isAlphanumeric((CharSequence) value);
    }

    public boolean isAllowSpace() {
        return allowSpace;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "AlphaNumericValidationRule{"
                + "isAllowSpace=" + isAllowSpace()
                + "}";
    }
}
