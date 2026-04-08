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
package org.rulii.validation.rules.min;

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

import java.math.BigDecimal;

/**
 * Builder for {@link DecimalMinValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DecimalMinValidationRuleBuilder
        extends ValueValidationRuleBuilder<DecimalMinValidationRuleBuilder, DecimalMinValidationRule> {

    private final BigDecimal min;
    private boolean inclusive = true;

    /**
     * Creates a new builder for {@link DecimalMinValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     * @param min           the minimum allowed decimal value
     */
    public DecimalMinValidationRuleBuilder(Function<?> valueFunction, BigDecimal min) {
        super(valueFunction);
        this.min = min;
        errorCode(DecimalMinValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
        message(DecimalMinValidationRule.DEFAULT_MESSAGE);
    }

    /**
     * Sets whether the minimum value is inclusive in the comparison.
     *
     * @param inclusive {@code true} to allow the value to equal the minimum; {@code false} for strict greater-than
     * @return this builder for fluent chaining
     */
    public DecimalMinValidationRuleBuilder inclusive(boolean inclusive) {
        this.inclusive = inclusive;
        return this;
    }

    /**
     * Creates a new {@link DecimalMinValidationRule} configured with this builder's settings.
     *
     * @return a new {@link DecimalMinValidationRule}
     */
    @Override
    protected DecimalMinValidationRule createValueValidationRule() {
        return new DecimalMinValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(),
                getValueName(), min, inclusive);
    }
}
