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
package org.rulii.validation.rules.notblank;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.StringUtils;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.util.List;

/**
 * Validation Rule to make sure the value is not blank (i.e. it has some text).
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value cannot be blank.")
public class NotBlankValidationRule extends ValueValidationRule {

    public static List<Class<?>> SUPPORTED_TYPES    = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "notBlankValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value must not be blank.";

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link NotBlankValidationRuleBuilder}
     */
    public static NotBlankValidationRuleBuilder builder(Function<?> function) {
        return new NotBlankValidationRuleBuilder(function);
    }

    NotBlankValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence))
            throw new ValidationRuleException("NotBlankValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "]");

        // Make sure there some text
        return StringUtils.isNotBlank((CharSequence) value);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "NotBlankValidationRule";
    }
}
