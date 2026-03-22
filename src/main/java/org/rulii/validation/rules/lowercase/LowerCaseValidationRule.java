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
package org.rulii.validation.rules.lowercase;

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
 * Validation Rule to make sure the value must be all in lowercase.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must be all in lowercase.")
public class LowerCaseValidationRule extends BindingValidationRule {

    public static final List<Class<?>> SUPPORTED_TYPES    = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "lowerCaseValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must be in lowercase.";

    /**
     * Constructor for LowerCaseValidationRule.
     *
     * @param bindingName The name of the binding to apply the validation rule on.
     */
    public LowerCaseValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    /**
     * Initializes a new LowerCaseValidationRule with the specified binding name and error code.
     *
     * @param bindingName The name of the binding to apply the validation rule on.
     * @param errorCode The error code for this validation rule.
     */
    public LowerCaseValidationRule(String bindingName, String errorCode) {
        this(bindingName, errorCode, Severity.ERROR, null);
    }

    /**
     *
     * Constructor for LowerCaseValidationRule.
     * Validates that the given input value is all in lowercase.
     *
     * @param bindingName The name of the binding to apply the validation rule on.
     * @param errorCode The error code associated with the validation rule.
     * @param severity The severity of the error if the validation rule fails.
     * @param errorMessage The custom error message to display if the validation rule fails.
     */
    public LowerCaseValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    /**
     * Represents a Validation Rule to ensure that the value must be all lowercase.
     *
     * @param bindingSupplier The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode The error code associated with the validation rule.
     * @param severity The severity of the error if the validation rule fails.
     * @param errorMessage The custom error message to display if the validation rule fails.
     */
    public LowerCaseValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity, String errorMessage) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {

        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("LowerCaseValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        return StringUtils.isAllLowerCase((CharSequence) value);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "LowerCaseValidationRule";
    }
}
