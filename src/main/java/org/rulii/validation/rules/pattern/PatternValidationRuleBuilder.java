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
package org.rulii.validation.rules.pattern;

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link PatternValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class PatternValidationRuleBuilder
        extends ValueValidationRuleBuilder<PatternValidationRuleBuilder, PatternValidationRule> {

    private final String pattern;
    private boolean caseSensitive = true;
    private int flags = 0;

    /**
     * Creates a new builder for {@link PatternValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     * @param pattern       the regex pattern that the value must match
     */
    public PatternValidationRuleBuilder(Function<?> valueFunction, String pattern) {
        super(valueFunction);
        this.pattern = pattern;
        errorCode(PatternValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
        message(PatternValidationRule.DEFAULT_MESSAGE);
    }

    /**
     * Sets whether the pattern matching is case-sensitive.
     *
     * @param caseSensitive {@code true} for case-sensitive matching (default); {@code false} for case-insensitive
     * @return this builder for fluent chaining
     */
    public PatternValidationRuleBuilder caseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    /**
     * Sets the regex flags used when compiling the pattern — {@link java.util.regex.Pattern}
     * constants, e.g. {@code Pattern.MULTILINE | Pattern.DOTALL}. Combines with
     * {@link #caseSensitive(boolean)}: case-insensitive matching adds
     * {@link java.util.regex.Pattern#CASE_INSENSITIVE} to the given flags.
     *
     * @param flags the {@link java.util.regex.Pattern} flag bits to compile with; default none
     * @return this builder for fluent chaining
     */
    public PatternValidationRuleBuilder flags(int flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Creates a new {@link PatternValidationRule} configured with this builder's settings.
     *
     * @return a new {@link PatternValidationRule}
     */
    @Override
    protected PatternValidationRule createValueValidationRule() {
        return new PatternValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(), caseSensitive, pattern, flags);
    }
}
