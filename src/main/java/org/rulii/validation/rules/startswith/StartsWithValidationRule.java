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
package org.rulii.validation.rules.startswith;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.validation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Validation Rule to make sure the value must start with one of the given prefixes.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must start with one of the given prefixes.")
public class StartsWithValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "startsWithValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must start with one of the given prefixes {1}.";

    private final String[] prefixes;

    /**
     * Creates a StartsWithValidationRule with the given binding name and prefixes to validate against.
     *
     * @param bindingName the name of the binding to apply the validation rule
     * @param prefixes the prefixes that the value must start with to pass validation
     */
    public StartsWithValidationRule(String bindingName, String...prefixes) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, prefixes);
    }

    /**
     * Creates a StartsWithValidationRule with the given binding name, error code, and prefixes to validate against.
     *
     * @param bindingName the name of the binding to apply the validation rule
     * @param errorCode the error code to use if the validation fails
     * @param prefixes the prefixes that the value must start with to pass validation
     */
    public StartsWithValidationRule(String bindingName, String errorCode, List<String> prefixes) {
        this(bindingName, errorCode, Severity.ERROR, null, prefixes.toArray(new String[0]));
    }

    /**
     * Creates a StartsWithValidationRule with the given binding name, error code, severity, error message,
     * and prefixes to validate against.
     *
     * @param bindingName the name of the binding to apply the validation rule
     * @param errorCode the error code to use if the validation fails
     * @param severity the severity of the error
     * @param errorMessage the error message that will be displayed if the validation rule fails
     * @param prefixes the prefixes that the value must start with to pass validation
     */
    public StartsWithValidationRule(String bindingName, String errorCode, Severity severity,
                                    String errorMessage, String...prefixes) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        Assert.notNull(prefixes, "prefixes cannot be null.");
        this.prefixes = prefixes;
    }

    /**
     * Represents a validation rule that checks if the value starts with any of the specified prefixes.
     *
     * @param bindingSupplier The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode The error code associated with the validation rule.
     * @param severity The severity of the error.
     * @param errorMessage The error message that will be displayed if the validation rule fails.
     * @param prefixes The prefixes that the value must start with to pass validation.
     */
    public StartsWithValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                                    String errorMessage, String... prefixes) {
        super(bindingSupplier, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        Assert.notNull(prefixes, "prefixes cannot be null.");
        this.prefixes = prefixes;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {

        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("StartsWithValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        boolean result = false;
        String stringValue = value.toString();

        for (String prefix : prefixes) {
            if (stringValue.startsWith(prefix)) {
                result = true;
                break;
            }
        }

        return result;
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("prefixes", Arrays.toString(prefixes));
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public String[] getPrefixes() {
        return prefixes;
    }

    @Override
    public String toString() {
        return "EndsWithValidationRule{"
                + "prefixes=" + Arrays.toString(prefixes)
                + "}";
    }
}
