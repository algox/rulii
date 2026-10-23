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

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link Mod11CheckValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class Mod11CheckValidationRuleBuilder
        extends ValueValidationRuleBuilder<Mod11CheckValidationRuleBuilder, Mod11CheckValidationRule> {

    private int threshold = Integer.MAX_VALUE;
    private int startIndex = 0;
    private int endIndex = Integer.MAX_VALUE;
    private int checkDigitIndex = ModCheck.UNSET;
    private boolean ignoreNonDigitCharacters = false;
    private char treatCheck10As = 'X';
    private char treatCheck11As = '0';
    private Mod11CheckValidationRule.ProcessingDirection processingDirection = Mod11CheckValidationRule.ProcessingDirection.RIGHT_TO_LEFT;

    /**
     * Creates a new builder for {@link Mod11CheckValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public Mod11CheckValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(Mod11CheckValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
    }

    /**
     * The largest weight before starting over at 2; no limit by default.
     *
     * @param threshold the threshold
     * @return this builder
     */
    public Mod11CheckValidationRuleBuilder threshold(int threshold) {
        this.threshold = threshold;
        return this;
    }

    /**
     * Where the digits start (inclusive); 0 by default.
     *
     * @param startIndex the index
     * @return this builder
     */
    public Mod11CheckValidationRuleBuilder startIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Where the digits end (exclusive); the end of the value by default.
     *
     * @param endIndex the index
     * @return this builder
     */
    public Mod11CheckValidationRuleBuilder endIndex(int endIndex) {
        this.endIndex = endIndex;
        return this;
    }

    /**
     * Where the check digit is; the last character of the range by default.
     *
     * @param checkDigitIndex the index
     * @return this builder
     */
    public Mod11CheckValidationRuleBuilder checkDigitIndex(int checkDigitIndex) {
        this.checkDigitIndex = checkDigitIndex;
        return this;
    }

    /**
     * Whether non-digit characters among the digits are dropped or fail the check (false, the default).
     *
     * @param ignoreNonDigitCharacters true to drop them
     * @return this builder
     */
    public Mod11CheckValidationRuleBuilder ignoreNonDigitCharacters(boolean ignoreNonDigitCharacters) {
        this.ignoreNonDigitCharacters = ignoreNonDigitCharacters;
        return this;
    }

    /**
     * The character standing for a check value of 10; {@code X} by default.
     *
     * @param treatCheck10As the character
     * @return this builder
     */
    public Mod11CheckValidationRuleBuilder treatCheck10As(char treatCheck10As) {
        this.treatCheck10As = treatCheck10As;
        return this;
    }

    /**
     * The character standing for a check value of 11; {@code 0} by default.
     *
     * @param treatCheck11As the character
     * @return this builder
     */
    public Mod11CheckValidationRuleBuilder treatCheck11As(char treatCheck11As) {
        this.treatCheck11As = treatCheck11As;
        return this;
    }

    /**
     * The direction the digits are weighted in; right to left by default.
     *
     * @param processingDirection the direction
     * @return this builder
     */
    public Mod11CheckValidationRuleBuilder processingDirection(Mod11CheckValidationRule.ProcessingDirection processingDirection) {
        Assert.notNull(processingDirection, "processingDirection cannot be null.");
        this.processingDirection = processingDirection;
        return this;
    }

    /**
     * Creates a new {@link Mod11CheckValidationRule} configured with this builder's settings.
     *
     * @return a new {@link Mod11CheckValidationRule}
     */
    @Override
    protected Mod11CheckValidationRule createValueValidationRule() {
        return new Mod11CheckValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(),
                threshold, startIndex, endIndex, checkDigitIndex, ignoreNonDigitCharacters, treatCheck10As, treatCheck11As, processingDirection);
    }
}
