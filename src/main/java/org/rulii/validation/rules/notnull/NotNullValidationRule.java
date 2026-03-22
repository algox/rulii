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
package org.rulii.validation.rules.notnull;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;

import java.util.List;

/**
 * Validation Rule to make sure the value is not null.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value cannot be null.")
public class NotNullValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(Object.class);

    public static final String ERROR_CODE       = "notNullValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value must not be null.";

    /**
     * Constructs a new NotNullValidationRule with the specified binding name.
     *
     * @param bindingName the name of the binding to apply the validation rule to
     */
    public NotNullValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    /**
     * Constructs a new NotNullValidationRule with the specified binding name, error code, and default severity.
     *
     * @param bindingName the name of the binding to apply the validation rule to
     * @param errorCode the error code to be associated with the validation rule
     */
    public NotNullValidationRule(String bindingName, String errorCode) {
        this(bindingName, errorCode, Severity.ERROR, null);
    }

    /**
     * Constructs a new NotNullValidationRule with the specified parameters.
     *
     * @param bindingName the name of the binding to apply the validation rule to
     * @param errorCode the error code to be associated with the validation rule
     * @param severity the severity of the error
     * @param errorMessage the error message that will be displayed if the validation rule fails
     */
    public NotNullValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    /**
     * Constructs a new NotNullValidationRule with the specified parameters.
     *
     * @param bindingSupplier the supplier of bindings for rule evaluation, must not be null
     * @param errorCode the error code associated with the validation rule
     * @param severity the severity of the error
     * @param errorMessage the error message that will be displayed if the validation rule fails
     */
    public NotNullValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity, String errorMessage) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        return value != null;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "NotNullValidationRule";
    }
}
