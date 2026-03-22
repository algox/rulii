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
package org.rulii.validation.rules.nulll;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.Severity;

import java.util.List;

/**
 * Validation Rule to make sure the value is null.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must be null.")
public class NullValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(Object.class);

    public static final String ERROR_CODE      = "nullValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE = "Value {0} must be null.";

    /**
     * Constructor for NullValidationRule class.
     *
     * @param bindingName the name of the binding to be validated
     */
    public NullValidationRule(String bindingName) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null);
    }

    /**
     * Constructs a new NullValidationRule with the specified binding name and error code.
     *
     * @param bindingName the name of the binding to be validated
     * @param errorCode the error code associated with the validation rule
     */
    public NullValidationRule(String bindingName, String errorCode) {
        this(bindingName, errorCode, Severity.ERROR, null);
    }

    /**
     * Constructor for NullValidationRule class that checks if the value is null.
     *
     * @param bindingName   the name of the binding to be validated
     * @param errorCode     the error code associated with the validation rule
     * @param severity      the severity of the error
     * @param errorMessage  the error message to be displayed if the validation rule fails
     */
    public NullValidationRule(String bindingName, String errorCode, Severity severity, String errorMessage) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    /**
     * Constructs a new NullValidationRule with the specified binding supplier, error code, severity, and error message.
     *
     * @param bindingSupplier the supplier of bindings for rule evaluation
     * @param errorCode the error code associated with the validation rule
     * @param severity the severity of the error
     * @param errorMessage the error message to be displayed if the validation rule fails
     */
    public NullValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity, String errorMessage) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        return value == null;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "NullValidationRule";
    }
}
