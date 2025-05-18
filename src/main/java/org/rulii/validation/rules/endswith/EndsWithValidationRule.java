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
package org.rulii.validation.rules.endswith;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;

import java.util.Arrays;
import java.util.List;

/**
 * Validation Rule to make sure the value must end with one of the given suffixes.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must end with one of the given suffixes.")
public class EndsWithValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "endsWithValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must end with one of the given suffixes {1}.";

    private final String[] suffixes;

    /**
     * Constructs a new EndsWithValidationRule with the specified binding name and suffixes.
     *
     * @param bindingName the name of the binding to validate (not null)
     * @param suffixes the suffixes that the value must end with (not null, not empty)
     */
    public EndsWithValidationRule(String bindingName, String...suffixes) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, suffixes);
    }

    /**
     * Constructs a new EndsWithValidationRule instance with the provided parameters.
     *
     * @param bindingName the name of the binding to validate (not null)
     * @param errorCode the error code associated with this validation rule
     * @param suffixes the suffixes that the value must end with (not null)
     */
    public EndsWithValidationRule(String bindingName, String errorCode, List<String> suffixes) {
        this(bindingName, errorCode, Severity.ERROR, null, suffixes.toArray(new String[0]));
    }

    /**
     * Constructs a new EndsWithValidationRule.
     *
     * @param bindingName the name of the binding to validate (not null)
     * @param errorCode the error code associated with this validation rule
     * @param severity the severity of the error (not null)
     * @param errorMessage the error message that will be displayed if the validation rule fails
     * @param suffixes the suffixes that the value must end with (not null)
     */
    public EndsWithValidationRule(String bindingName, String errorCode, Severity severity,
                                  String errorMessage, String...suffixes) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        Assert.notNull(suffixes, "suffixes cannot be null.");
        this.suffixes = suffixes;
    }

    /**
     *
     * Constructs a new EndsWithValidationRule with the specified binding supplier, error code, severity, error message, and suffixes.
     *
     * @param bindingSupplier the supplier of bindings for rule evaluation (not null)
     * @param errorCode the error code associated with this validation rule
     * @param severity the severity of the error
     * @param errorMessage the error message that will be displayed if the validation rule fails
     * @param suffixes the suffixes that the value must end with (not null, not empty)
     */
    public EndsWithValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                                  String errorMessage, String...suffixes) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        Assert.notNull(suffixes, "suffixes cannot be null.");
        Assert.isTrue(suffixes.length > 0, "there must be at least one suffixes.");
        this.suffixes = suffixes;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {

        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new UnrulyException("EndsWithValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        boolean result = false;
        String stringValue = value.toString();

        for (String suffix : suffixes) {
            if (stringValue.endsWith(suffix)) {
                result = true;
                break;
            }
        }

        return result;
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("suffixes", Arrays.toString(suffixes));
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public String[] getSuffixes() {
        return suffixes;
    }

    @Override
    public String toString() {
        return "EndsWithValidationRule{"
                + "suffixes=" + Arrays.toString(suffixes)
                + "}";
    }
}
