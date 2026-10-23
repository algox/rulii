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
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.util.List;

/**
 * Validation Rule to make sure a sequence of digits passes a modulo 10 check digit test with a
 * multiplier for the odd positions (counted from the right) and a weight for the even ones, as
 * EAN and ISBN-13 numbers do (multiplier 3, weight 1). See {@link ModCheck} for how the digits and the
 * check character are located.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
@Rule
@Description("Value must pass the modulo 10 check digit test.")
public class Mod10CheckValidationRule extends ValueValidationRule {

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "mod10CheckValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} has an invalid check digit (modulo 10).";

    private final int multiplier;
    private final int weight;
    private final int startIndex;
    private final int endIndex;
    private final int checkDigitIndex;
    private final boolean ignoreNonDigitCharacters;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link Mod10CheckValidationRuleBuilder}
     */
    public static Mod10CheckValidationRuleBuilder builder(Function<?> function) {
        return new Mod10CheckValidationRuleBuilder(function);
    }

    Mod10CheckValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName,
                             int multiplier, int weight, int startIndex, int endIndex, int checkDigitIndex, boolean ignoreNonDigitCharacters) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        this.multiplier = multiplier;
        this.weight = weight;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.checkDigitIndex = checkDigitIndex;
        this.ignoreNonDigitCharacters = ignoreNonDigitCharacters;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence text))
            throw new ValidationRuleException("Mod10CheckValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        ModCheck.Extraction extraction = ModCheck.extract(text, startIndex, endIndex, checkDigitIndex, ignoreNonDigitCharacters);

        if (extraction == null) return false;

        int check = Character.digit(extraction.checkCharacter(), 10);
        return check >= 0 && check == ModCheck.mod10CheckDigit(extraction.digits(), multiplier, weight);
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("multiplier", multiplier).param("weight", weight);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getWeight() {
        return weight;
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
    public String toString() {
        return "Mod10CheckValidationRule{multiplier=" + multiplier + ", weight=" + weight + ", startIndex=" + startIndex
                + ", endIndex=" + endIndex + ", checkDigitIndex=" + checkDigitIndex + ", ignoreNonDigitCharacters=" + ignoreNonDigitCharacters + '}';
    }
}
