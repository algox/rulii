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
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationRuleException;
import org.rulii.validation.ValueValidationRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validation Rule to make sure a sequence of digits passes a modulo 11 check digit test: the digits
 * are weighted 2, 3, 4, ... from the right (starting over at 2 past the threshold), and the check
 * value is eleven minus the sum modulo eleven, a check value of ten and eleven being written as a
 * replacement character ({@code X} and {@code 0} by default). See {@link ModCheck} for how the digits
 * and the check character are located.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
@Rule
@Description("Value must pass the modulo 11 check digit test.")
public class Mod11CheckValidationRule extends ValueValidationRule {

    /**
     * The direction the digits are weighted in.
     */
    public enum ProcessingDirection {
        /** The rightmost digit gets weight 2. */
        RIGHT_TO_LEFT,
        /** The leftmost digit gets weight 2. */
        LEFT_TO_RIGHT
    }

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "mod11CheckValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Value {0} has an invalid check digit (modulo 11).";

    private final int threshold;
    private final int startIndex;
    private final int endIndex;
    private final int checkDigitIndex;
    private final boolean ignoreNonDigitCharacters;
    private final char treatCheck10As;
    private final char treatCheck11As;
    private final ProcessingDirection processingDirection;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link Mod11CheckValidationRuleBuilder}
     */
    public static Mod11CheckValidationRuleBuilder builder(Function<?> function) {
        return new Mod11CheckValidationRuleBuilder(function);
    }

    Mod11CheckValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage, String valueName,
                             int threshold, int startIndex, int endIndex, int checkDigitIndex, boolean ignoreNonDigitCharacters,
                             char treatCheck10As, char treatCheck11As, ProcessingDirection processingDirection) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        Assert.notNull(processingDirection, "processingDirection cannot be null.");
        this.threshold = threshold;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.checkDigitIndex = checkDigitIndex;
        this.ignoreNonDigitCharacters = ignoreNonDigitCharacters;
        this.treatCheck10As = treatCheck10As;
        this.treatCheck11As = treatCheck11As;
        this.processingDirection = processingDirection;
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence text))
            throw new ValidationRuleException("Mod11CheckValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        ModCheck.Extraction extraction = ModCheck.extract(text, startIndex, endIndex, checkDigitIndex, ignoreNonDigitCharacters);

        if (extraction == null) return false;

        List<Integer> digits = extraction.digits();

        if (processingDirection == ProcessingDirection.LEFT_TO_RIGHT) {
            digits = new ArrayList<>(digits);
            Collections.reverse(digits);
        }

        int checkValue = ModCheck.mod11CheckValue(digits, threshold);
        char check = extraction.checkCharacter();

        return switch (checkValue) {
            case 10 -> check == treatCheck10As;
            case 11 -> check == treatCheck11As;
            default -> Character.digit(check, 10) == checkValue;
        };
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        builder.param("threshold", threshold);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    public int getThreshold() {
        return threshold;
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

    public char getTreatCheck10As() {
        return treatCheck10As;
    }

    public char getTreatCheck11As() {
        return treatCheck11As;
    }

    public ProcessingDirection getProcessingDirection() {
        return processingDirection;
    }

    @Override
    public String describe() {
        return getValueName() + " must pass the modulo 11 check";
    }

    @Override
    public String toString() {
        return "Mod11CheckValidationRule{threshold=" + threshold + ", startIndex=" + startIndex + ", endIndex=" + endIndex
                + ", checkDigitIndex=" + checkDigitIndex + ", ignoreNonDigitCharacters=" + ignoreNonDigitCharacters
                + ", treatCheck10As=" + treatCheck10As + ", treatCheck11As=" + treatCheck11As + ", processingDirection=" + processingDirection + '}';
    }
}
