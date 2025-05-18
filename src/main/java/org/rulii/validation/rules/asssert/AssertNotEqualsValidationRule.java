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
package org.rulii.validation.rules.asssert;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;

import java.util.List;
import java.util.Objects;

/**
 * Validation Rule to make sure the value does not match input value.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must not match desired value.")
public class AssertNotEqualsValidationRule extends BindingValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = List.of(Object.class);

    public static final String ERROR_CODE       = "assertNotEqualsValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value [{0}] must not equal [{1}].";

    private final Object value;

    /**
     * Constructs a validation rule to ensure that the given value does not match the specified input value.
     *
     * @param bindingName The name of the binding for this validation rule
     * @param value The value that the binding value should not equal
     */
    public AssertNotEqualsValidationRule(String bindingName, Object value) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, value);
    }

    /**
     * Constructs an AssertNotEqualsValidationRule with the provided binding name, error code, and value.
     *
     * @param bindingName The name of the binding for this validation rule
     * @param errorCode The error code associated with the validation rule
     * @param value The value that the binding value should not equal
     */
    public AssertNotEqualsValidationRule(String bindingName, String errorCode, Object value) {
        this(bindingName, errorCode, Severity.ERROR, null, value);
    }

    /**
     * Constructs a validation rule to ensure that the value does not match the specified input value.
     *
     * @param bindingName The name of the binding for this validation rule
     * @param errorCode The error code associated with the validation rule
     * @param severity The severity of the error
     * @param errorMessage The error message that will be displayed if the validation rule fails
     * @param value The value that the binding value should not equal
     */
    public AssertNotEqualsValidationRule(String bindingName, String errorCode,
                                         Severity severity, String errorMessage, Object value) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.value = value;
    }

    /**
     * Constructs an AssertNotEqualsValidationRule with the specified parameters.
     *
     * @param bindingSupplier The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode The error code associated with the validation rule. Not null.
     * @param severity The severity of the error. Not null.
     * @param errorMessage The error message to be displayed if the validation rule fails.
     * @param value The value that the binding value should not equal.
     */
    public AssertNotEqualsValidationRule(BindingSupplier bindingSupplier, String errorCode,
                                         Severity severity, String errorMessage, Object value) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.value = value;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        return !Objects.equals(this.value, value);
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("value", this.value);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "AssertNotEqualsValidationRule";
    }
}
