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
package org.rulii.validation.rules.alpha;

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
 * Validation Rule to make sure the value only contains unicode letters (or spaces).
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@Rule
@Description("Value can only contain unicode letters/spaces.")
public class AlphaValidationRule extends ValueValidationRule {

    private static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "alphaValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} must only contain unicode letters.";

    private final boolean allowSpace;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link AlphaValidationRuleBuilder}
     */
    public static AlphaValidationRuleBuilder builder(Function<?> function) {
        return new AlphaValidationRuleBuilder(function);
    }

    /**
     * Constructs a new instance of AlphaValidationRule that validates whether a value contains only
     * Unicode letters. Optionally, it can allow spaces in the value being validated.
     *
     * @param valueFunction the function that provides the value to validate
     * @param errorCode the error code to be used if the validation fails
     * @param severity the severity of the error if validation fails
     * @param errorMessage the custom error message to use if validation fails
     * @param allowSpace whether spaces are allowed in the validated value
     */
    AlphaValidationRule(Function<?> valueFunction, String errorCode, Severity severity,
                               String errorMessage, String valueName, boolean allowSpace) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        this.allowSpace = allowSpace;
    }

    /**
     * Validates if the provided value satisfies the AlphaValidationRule criteria,
     * which checks if the value contains only Unicode letters (and spaces if configured to allow spaces).
     *
     * @param ruleContext the context of the validation rule containing related metadata and state
     * @param value the value to be validated; it should either be null or an instance of CharSequence
     * @return {@code true} if the value is valid (null or a CharSequence containing only Unicode letters
     *         or Unicode letters and spaces if spaces are allowed); {@code false} otherwise
     * @throws UnrulyException if the input value is not an instance of CharSequence
     */
    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return true;

        if (!(value instanceof CharSequence))
            throw new UnrulyException("AlphaValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        return isAllowSpace() ? StringUtils.isAlphaSpace((CharSequence) value) : StringUtils.isAlpha((CharSequence) value);
    }

    /**
     * Indicates whether spaces are allowed in the validated value.
     *
     * @return {@code true} if spaces are allowed; {@code false} otherwise.
     */
    public boolean isAllowSpace() {
        return allowSpace;
    }

    /**
     * Retrieves a list of classes that are supported by the validation rule.
     *
     * @return a list of Class objects representing the supported types.
     */
    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String toString() {
        return "AlphaValidationRule{"
                + "allowSpace=" + isAllowSpace()
                + "}";
    }
}
