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
package org.rulii.validation.rules.notblank;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link NotBlankValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class NotBlankValidationRuleBuilder
        extends ValueValidationRuleBuilder<NotBlankValidationRuleBuilder, NotBlankValidationRule> {

    /**
     * Creates a new builder for {@link NotBlankValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public NotBlankValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(NotBlankValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
        message(NotBlankValidationRule.DEFAULT_MESSAGE);
    }

    /**
     * Disallowed with {@code false} for this rule: a presence rule that skips null values
     * could never fail.
     *
     * @param failOnNull must be true; false is not allowed for this rule
     * @return this builder for fluent chaining
     */
    @Override
    public NotBlankValidationRuleBuilder failOnNull(boolean failOnNull) {
        Assert.isTrue(failOnNull, "failOnNull(false) is not allowed on [notBlankValidationRule]. A presence rule that skips null values could never fail.");
        return super.failOnNull(failOnNull);
    }

    /**
     * Creates a new {@link NotBlankValidationRule} configured with this builder's settings.
     *
     * @return a new {@link NotBlankValidationRule}
     */
    @Override
    protected NotBlankValidationRule createValueValidationRule() {
        return new NotBlankValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName());
    }
}
