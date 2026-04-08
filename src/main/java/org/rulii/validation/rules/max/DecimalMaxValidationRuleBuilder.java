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
package org.rulii.validation.rules.max;

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

import java.math.BigDecimal;

/**
 * Builder for {@link DecimalMaxValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DecimalMaxValidationRuleBuilder
        extends ValueValidationRuleBuilder<DecimalMaxValidationRuleBuilder, DecimalMaxValidationRule> {

    private final BigDecimal max;
    private boolean inclusive = true;

    /**
     * Creates a new builder for {@link DecimalMaxValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     * @param max           the maximum allowed decimal value
     */
    public DecimalMaxValidationRuleBuilder(Function<?> valueFunction, BigDecimal max) {
        super(valueFunction);
        this.max = max;
        errorCode(DecimalMaxValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
        message(DecimalMaxValidationRule.DEFAULT_MESSAGE);
    }

    /**
     * Sets whether the maximum value is inclusive in the comparison.
     *
     * @param inclusive {@code true} to allow the value to equal the maximum; {@code false} for strict less-than
     * @return this builder for fluent chaining
     */
    public DecimalMaxValidationRuleBuilder inclusive(boolean inclusive) {
        this.inclusive = inclusive;
        return this;
    }

    /**
     * Creates a new {@link DecimalMaxValidationRule} configured with this builder's settings.
     *
     * @return a new {@link DecimalMaxValidationRule}
     */
    @Override
    protected DecimalMaxValidationRule createValueValidationRule() {
        return new DecimalMaxValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(), max, inclusive);
    }
}
