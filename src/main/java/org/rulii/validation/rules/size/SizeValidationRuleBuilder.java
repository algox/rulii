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
package org.rulii.validation.rules.size;

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link SizeValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class SizeValidationRuleBuilder
        extends ValueValidationRuleBuilder<SizeValidationRuleBuilder, SizeValidationRule> {

    private final int min;
    private final int max;

    /**
     * Creates a new builder for {@link SizeValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     * @param min           the minimum size (inclusive)
     * @param max           the maximum size (inclusive)
     */
    public SizeValidationRuleBuilder(Function<?> valueFunction, int min, int max) {
        super(valueFunction);
        this.min = min;
        this.max = max;
        errorCode(SizeValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
        message(SizeValidationRule.DEFAULT_MESSAGE);
    }

    /**
     * Creates a new {@link SizeValidationRule} configured with this builder's settings.
     *
     * @return a new {@link SizeValidationRule}
     */
    @Override
    protected SizeValidationRule createValueValidationRule() {
        return new SizeValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(), min, max);
    }
}
