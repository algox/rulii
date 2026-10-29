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
package org.rulii.validation.rules.modcheck;

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.util.List;

/**
 * Validation Rule to make sure a sequence of digits passes the Luhn (modulo 10) check digit test, as
 * credit card numbers do. See {@link ModCheck} for how the digits and the check character are located.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
@Rule
@Description("Value must pass the Luhn check digit test.")
public class LuhnCheckValidationRule extends ValueValidationRule {

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "luhnCheckValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} has an invalid check digit (Luhn modulo 10).";

    private final int startIndex;
    private final int endIndex;
    private final int checkDigitIndex;
    private final boolean ignoreNonDigitCharacters;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link LuhnCheckValidationRuleBuilder}
     */
    public static LuhnCheckValidationRuleBuilder builder(Function<?> function) {
        return new LuhnCheckValidationRuleBuilder(function);
    }

    LuhnCheckValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName,
                            int startIndex, int endIndex, int checkDigitIndex, boolean ignoreNonDigitCharacters) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.checkDigitIndex = checkDigitIndex;
        this.ignoreNonDigitCharacters = ignoreNonDigitCharacters;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence text))
            throw new ValidationRuleException("LuhnCheckValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        ModCheck.Extraction extraction = ModCheck.extract(text, startIndex, endIndex, checkDigitIndex, ignoreNonDigitCharacters);

        if (extraction == null) return false;

        int check = Character.digit(extraction.checkCharacter(), 10);
        return check >= 0 && check == ModCheck.luhnCheckDigit(extraction.digits());
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getCheckDigitIndex() {
        return checkDigitIndex;
    }

    public boolean isIgnoreNonDigitCharacters() {
        return ignoreNonDigitCharacters;
    }

    @Override
    public String describe() {
        return getValueName() + " must pass the Luhn check";
    }

    @Override
    public String toString() {
        return "LuhnCheckValidationRule{startIndex=" + startIndex + ", endIndex=" + endIndex + ", checkDigitIndex=" + checkDigitIndex
                + ", ignoreNonDigitCharacters=" + ignoreNonDigitCharacters + '}';
    }
}
