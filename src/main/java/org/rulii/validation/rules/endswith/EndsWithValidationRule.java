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
package org.rulii.validation.rules.endswith;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRule;

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
public class EndsWithValidationRule extends ValueValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "endsWithValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must end with one of the given suffixes {1}.";

    private final String[] suffixes;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @param suffixes one or more suffixes that the value must end with
     * @return a new {@link EndsWithValidationRuleBuilder}
     */
    public static EndsWithValidationRuleBuilder builder(Function<?> function, String... suffixes) {
        return new EndsWithValidationRuleBuilder(function, suffixes);
    }

    EndsWithValidationRule(Function<?> valueFunction, String errorCode, Severity severity,
                           String errorMessage, String valueName, String... suffixes) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        Assert.notNull(suffixes, "suffixes cannot be null.");
        this.suffixes = suffixes;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

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
