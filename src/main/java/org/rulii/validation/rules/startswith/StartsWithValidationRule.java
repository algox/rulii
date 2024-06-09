/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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
import org.rulii.validation.BindingSupplier;
import org.rulii.validation.BindingValidationRule;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;

import java.util.Arrays;
import java.util.List;

/**
 * Validation Rule to make sure the value must start with one of the given prefixes.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
@Description("Value must start with one of the given prefixes.")
public class StartsWithValidationRule extends BindingValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = Arrays.asList(CharSequence.class);

    public static final String ERROR_CODE       = "rulii.validation.rules.StartsWithValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value must start with one of the given prefixes {1}. Given {0}.";

    private String[] prefixes;

    public StartsWithValidationRule(String bindingName, String...prefixes) {
        this(bindingName, ERROR_CODE, Severity.ERROR, null, prefixes);
    }

    public StartsWithValidationRule(String bindingName, String errorCode, Severity severity,
                                    String errorMessage, String...prefixes) {
        super(bindingName, errorCode, severity, errorMessage, DEFAULT_MESSAGE);
        Assert.notNull(prefixes, "prefixes cannot be null.");
        this.prefixes = prefixes;
    }

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
