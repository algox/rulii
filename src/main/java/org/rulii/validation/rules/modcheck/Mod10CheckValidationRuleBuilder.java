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

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link Mod10CheckValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class Mod10CheckValidationRuleBuilder
        extends ValueValidationRuleBuilder<Mod10CheckValidationRuleBuilder, Mod10CheckValidationRule> {

    private int multiplier = 3;
    private int weight = 1;
    private int startIndex = 0;
    private int endIndex = Integer.MAX_VALUE;
    private int checkDigitIndex = ModCheck.UNSET;
    private boolean ignoreNonDigitCharacters = true;

    /**
     * Creates a new builder for {@link Mod10CheckValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public Mod10CheckValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(Mod10CheckValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
    }

    /**
     * The multiplier of the odd positions, counted from the right; 3 by default.
     *
     * @param multiplier the multiplier
     * @return this builder
     */
    public Mod10CheckValidationRuleBuilder multiplier(int multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    /**
     * The multiplier of the even positions, counted from the right; 1 by default.
     *
     * @param weight the weight
     * @return this builder
     */
    public Mod10CheckValidationRuleBuilder weight(int weight) {
        this.weight = weight;
        return this;
    }

    /**
     * Where the digits start (inclusive); 0 by default.
     *
     * @param startIndex the index
     * @return this builder
     */
    public Mod10CheckValidationRuleBuilder startIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Where the digits end (exclusive); the end of the value by default.
     *
     * @param endIndex the index
     * @return this builder
     */
    public Mod10CheckValidationRuleBuilder endIndex(int endIndex) {
        this.endIndex = endIndex;
        return this;
    }

    /**
     * Where the check digit is; the last character of the range by default.
     *
     * @param checkDigitIndex the index
     * @return this builder
     */
    public Mod10CheckValidationRuleBuilder checkDigitIndex(int checkDigitIndex) {
        this.checkDigitIndex = checkDigitIndex;
        return this;
    }

    /**
     * Whether non-digit characters among the digits are dropped (true, the default) or fail the check.
     *
     * @param ignoreNonDigitCharacters true to drop them
     * @return this builder
     */
    public Mod10CheckValidationRuleBuilder ignoreNonDigitCharacters(boolean ignoreNonDigitCharacters) {
        this.ignoreNonDigitCharacters = ignoreNonDigitCharacters;
        return this;
    }

    /**
     * Creates a new {@link Mod10CheckValidationRule} configured with this builder's settings.
     *
     * @return a new {@link Mod10CheckValidationRule}
     */
    @Override
    protected Mod10CheckValidationRule createValueValidationRule() {
        return new Mod10CheckValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(),
                multiplier, weight, startIndex, endIndex, checkDigitIndex, ignoreNonDigitCharacters);
    }
}
