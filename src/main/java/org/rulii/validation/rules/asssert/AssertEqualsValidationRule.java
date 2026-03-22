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
 * Validation Rule to make sure the value matches input value.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must match desired value.")
public class AssertEqualsValidationRule extends BindingValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = List.of(Object.class);

    public static final String ERROR_CODE       = "assertEqualsValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value [{0}] must be equal [{1}].";

    private final Object value;

    /**
     * This method creates an instance of AssertEqualsValidationRule with the provided binding name and value.
     *
     * @param bindingName the name of the binding for this validation rule
     * @param value the value that should be matched for validation
     */
    public AssertEqualsValidationRule(String bindingName, Object value) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, value);
    }

    /**
     * Constructs a new AssertEqualsValidationRule with the provided binding name, error code, and value.
     *
     * @param bindingName the name of the binding for this validation rule
     * @param errorCode the error code associated with the validation rule
     * @param value the value that should be matched for validation
     */
    public AssertEqualsValidationRule(String bindingName, String errorCode, Object value) {
        this(bindingName, errorCode, Severity.ERROR, null, value);
    }

    /**
     * Initializes a new instance of AssertEqualsValidationRule with the specified parameters.
     *
     * @param bindingName the name of the binding for this validation rule
     * @param errorCode the error code associated with the validation rule
     * @param severity the severity of the error
     * @param errorMessage the error message to display if the validation fails
     * @param value the value that should be matched for validation
     */
    public AssertEqualsValidationRule(String bindingName, String errorCode,
                                      Severity severity, String errorMessage, Object value) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.value = value;
    }

    /**
     * Constructs an AssertEqualsValidationRule with the specified parameters.
     *
     * @param bindingSupplier the supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode the error code associated with the validation rule.
     * @param severity the severity of the error.
     * @param errorMessage the error message that will be displayed if the validation rule fails.
     * @param value the value that should be matched for validation.
     */
    public AssertEqualsValidationRule(BindingSupplier bindingSupplier, String errorCode,
                                      Severity severity, String errorMessage, Object value) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        this.value = value;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        return Objects.equals(this.value, value);
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
        return "AssertEqualsValidationRule";
    }
}
