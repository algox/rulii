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
package org.rulii.validation.rules.alpha;

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
 * Validation Rule to make sure the value only contains unicode letters (or spaces).
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
@Description("Value can only contain unicode letters/spaces.")
public class AlphaValidationRule extends BindingValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "alphaValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must only contain unicode letters.";

    private final boolean allowSpace;

    /**
     * Constructs a new AlphaValidationRule with the specified binding name, using default error code, severity, and error message.
     *
     * @param bindingName the name of the binding to apply the validation rule on
     */
    public AlphaValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, true);
    }

    /**
     * Constructor for creating a new AlphaValidationRule instance with the provided binding name, error code, and flag to allow spaces.
     *
     * @param bindingName the name of the binding to apply the validation rule on
     * @param errorCode the error code to be used if validation fails
     * @param allowSpace a boolean flag indicating whether spaces are allowed in the value
     */
    public AlphaValidationRule(String bindingName, String errorCode, boolean allowSpace) {
        this(bindingName, errorCode, Severity.ERROR, null, allowSpace);
    }

    /**
     * Constructs a new AlphaValidationRule instance with the specified parameters.
     *
     * @param bindingName the name of the binding to apply the validation rule on
     * @param errorCode the error code to be used if validation fails
     * @param severity the severity of the error
     * @param errorMessage the error message that will be displayed if the validation rule fails
     * @param allowSpace a boolean flag indicating whether spaces are allowed in the value
     */
    public AlphaValidationRule(String bindingName, String errorCode, Severity severity,
                               String errorMessage, boolean allowSpace) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowSpace = allowSpace;
    }

    /**
     * Constructs a new AlphaValidationRule with the specified binding supplier, error code, severity, error message, and flag to allow spaces.
     *
     * @param bindingSupplier The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode The error code associated with the validation rule.
     * @param severity The severity of the error.
     * @param errorMessage The error message that will be displayed if the validation rule fails.
     * @param allowSpace A boolean flag indicating whether spaces are allowed in the value.
     */
    public AlphaValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                               String errorMessage, boolean allowSpace) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.allowSpace = allowSpace;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new
                    UnrulyException("AlphaValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        return isAllowSpace() ? StringUtils.isAlphaSpace((CharSequence) value) : StringUtils.isAlpha((CharSequence) value);
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
        return "AlphaValidationRule{"
                + "allowSpace=" + isAllowSpace()
                + "}";
    }
}
