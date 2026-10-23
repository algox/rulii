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
 * Builder for {@link DurationMinValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class DurationMinValidationRuleBuilder
        extends ValueValidationRuleBuilder<DurationMinValidationRuleBuilder, DurationMinValidationRule> {

    private final Duration min;
    private boolean inclusive = true;

    /**
     * Creates a new builder for {@link DurationMinValidationRule}.
     *
     * @param valueFunction the function that supplies the duration to validate
     * @param min the minimum duration
     */
    public DurationMinValidationRuleBuilder(Function<?> valueFunction, Duration min) {
        super(valueFunction);
        Assert.notNull(min, "min cannot be null.");
        this.min = min;
        errorCode(DurationMinValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
    }

    /**
     * Whether the minimum itself passes; true by default.
     *
     * @param inclusive true for at least the minimum, false for strictly longer
     * @return this builder
     */
    public DurationMinValidationRuleBuilder inclusive(boolean inclusive) {
        this.inclusive = inclusive;
        return this;
    }

    /**
     * Creates a new {@link DurationMinValidationRule} configured with this builder's settings.
     *
     * @return a new {@link DurationMinValidationRule}
     */
    @Override
    protected DurationMinValidationRule createValueValidationRule() {
        return new DurationMinValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(), min, inclusive);
    }
}
