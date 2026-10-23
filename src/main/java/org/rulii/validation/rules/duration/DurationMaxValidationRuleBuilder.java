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
package org.rulii.validation.rules.duration;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

import java.time.Duration;

/**
 * Builder for {@link DurationMaxValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class DurationMaxValidationRuleBuilder
        extends ValueValidationRuleBuilder<DurationMaxValidationRuleBuilder, DurationMaxValidationRule> {

    private final Duration max;
    private boolean inclusive = true;

    /**
     * Creates a new builder for {@link DurationMaxValidationRule}.
     *
     * @param valueFunction the function that supplies the duration to validate
     * @param max the maximum duration
     */
    public DurationMaxValidationRuleBuilder(Function<?> valueFunction, Duration max) {
        super(valueFunction);
        Assert.notNull(max, "max cannot be null.");
        this.max = max;
        errorCode(DurationMaxValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
    }

    /**
     * Whether the maximum itself passes; true by default.
     *
     * @param inclusive true for at most the maximum, false for strictly shorter
     * @return this builder
     */
    public DurationMaxValidationRuleBuilder inclusive(boolean inclusive) {
        this.inclusive = inclusive;
        return this;
    }

    /**
     * Creates a new {@link DurationMaxValidationRule} configured with this builder's settings.
     *
     * @return a new {@link DurationMaxValidationRule}
     */
    @Override
    protected DurationMaxValidationRule createValueValidationRule() {
        return new DurationMaxValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(), max, inclusive);
    }
}
