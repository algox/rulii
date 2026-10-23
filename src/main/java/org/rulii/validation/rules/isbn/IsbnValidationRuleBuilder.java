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
package org.rulii.validation.rules.isbn;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link IsbnValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class IsbnValidationRuleBuilder
        extends ValueValidationRuleBuilder<IsbnValidationRuleBuilder, IsbnValidationRule> {

    private IsbnValidationRule.Type type = IsbnValidationRule.Type.ISBN_13;

    /**
     * Creates a new builder for {@link IsbnValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public IsbnValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(IsbnValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
    }

    /**
     * The ISBN format; ISBN-13 by default.
     *
     * @param type the format
     * @return this builder
     */
    public IsbnValidationRuleBuilder type(IsbnValidationRule.Type type) {
        Assert.notNull(type, "type cannot be null.");
        this.type = type;
        return this;
    }

    /**
     * Creates a new {@link IsbnValidationRule} configured with this builder's settings.
     *
     * @return a new {@link IsbnValidationRule}
     */
    @Override
    protected IsbnValidationRule createValueValidationRule() {
        return new IsbnValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(), type);
    }
}
