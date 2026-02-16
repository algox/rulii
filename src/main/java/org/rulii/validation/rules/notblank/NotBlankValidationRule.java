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
package org.rulii.validation.rules.notblank;

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
 * Validation Rule to make sure the value is not blank (i.e. it has some text).
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value cannot be blank.")
public class NotBlankValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "notBlankValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value must not be blank.";

    /**
     * Constructor for creating a NotBlankValidationRule instance.
     *
     * @param bindingName the name of the binding to apply the validation rule to
     */
    public NotBlankValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    /**
     * Initializes a new NotBlankValidationRule with the provided binding name and error code.
     *
     * @param bindingName the name of the binding to apply the validation rule to
     * @param errorCode the error code to be set if the validation fails
     */
    public NotBlankValidationRule(String bindingName, String errorCode) {
        this(bindingName, errorCode, Severity.ERROR, null);
    }

    /**
     * Constructs a NotBlankValidationRule with the specified parameters.
     *
     * @param bindingName the name of the binding to apply the validation rule to
     * @param errorCode the error code to be set if the validation fails
     * @param severity the severity of the error
     * @param errorMessage the error message to be displayed if the validation rule fails
     */
    public NotBlankValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    /**
     * Constructs a new NotBlankValidationRule with the provided parameters.
     *
     * @param bindingSupplier the supplier of bindings for rule evaluation
     * @param errorCode the error code associated with the validation rule
     * @param severity the severity of the error
     * @param errorMessage the error message to be displayed if the validation rule fails
     */
    public NotBlankValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity, String errorMessage) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("NotBlankValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "]");

        // Make sure there some text
        return StringUtils.isNotBlank((CharSequence) value);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "NotBlankValidationRule";
    }
}
