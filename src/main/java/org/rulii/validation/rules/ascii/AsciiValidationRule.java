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
package org.rulii.validation.rules.ascii;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.StringUtils;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRule;

import java.util.List;

/**
 * Validation Rule to make sure the value only contains ASCII printable characters.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value can only contains contains ASCII printable characters.")
public class AsciiValidationRule extends ValueValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "asciiValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must only contain ASCII printable characters.";

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link AsciiValidationRuleBuilder}
     */
    public static AsciiValidationRuleBuilder builder(Function<?> function) {
        return new AsciiValidationRuleBuilder(function);
    }

    AsciiValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new UnrulyException("AsciiValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        return StringUtils.isAsciiPrintable((CharSequence) value);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "AsciiValidationRule";
    }
}
