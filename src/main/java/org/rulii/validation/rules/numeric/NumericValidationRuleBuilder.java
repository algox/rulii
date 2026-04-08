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
package org.rulii.validation.rules.numeric;

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link NumericValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class NumericValidationRuleBuilder
        extends ValueValidationRuleBuilder<NumericValidationRuleBuilder, NumericValidationRule> {

    private boolean allowSpace = false;

    /**
     * Creates a new builder for {@link NumericValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public NumericValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(NumericValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
        message(NumericValidationRule.DEFAULT_MESSAGE);
    }

    /**
     * Configures the rule to allow spaces in the validated value.
     *
     * @return this builder for fluent chaining
     */
    public NumericValidationRuleBuilder allowSpace() {
        this.allowSpace = true;
        return this;
    }

    /**
     * Creates a new {@link NumericValidationRule} configured with this builder's settings.
     *
     * @return a new {@link NumericValidationRule}
     */
    @Override
    protected NumericValidationRule createValueValidationRule() {
        return new NumericValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(), allowSpace);
    }
}
