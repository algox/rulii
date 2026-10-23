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
package org.rulii.validation.rules.codepointlength;

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

import java.text.Normalizer;

/**
 * Builder for {@link CodePointLengthValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class CodePointLengthValidationRuleBuilder
        extends ValueValidationRuleBuilder<CodePointLengthValidationRuleBuilder, CodePointLengthValidationRule> {

    private final int min;
    private final int max;
    private Normalizer.Form normalization;

    /**
     * Creates a new builder for {@link CodePointLengthValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     * @param min the minimum number of code points (inclusive)
     * @param max the maximum number of code points (inclusive)
     */
    public CodePointLengthValidationRuleBuilder(Function<?> valueFunction, int min, int max) {
        super(valueFunction);
        this.min = min;
        this.max = max;
        errorCode(CodePointLengthValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
    }

    /**
     * Normalizes the value to the given form before counting; none by default.
     *
     * @param normalization the form; null to count the value as is
     * @return this builder
     */
    public CodePointLengthValidationRuleBuilder normalization(Normalizer.Form normalization) {
        this.normalization = normalization;
        return this;
    }

    /**
     * Creates a new {@link CodePointLengthValidationRule} configured with this builder's settings.
     *
     * @return a new {@link CodePointLengthValidationRule}
     */
    @Override
    protected CodePointLengthValidationRule createValueValidationRule() {
        return new CodePointLengthValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(),
                getValueName(), min, max, normalization);
    }
}
