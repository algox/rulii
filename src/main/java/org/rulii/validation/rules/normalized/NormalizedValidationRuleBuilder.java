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
package org.rulii.validation.rules.normalized;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

import java.text.Normalizer;

/**
 * Builder for {@link NormalizedValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class NormalizedValidationRuleBuilder
        extends ValueValidationRuleBuilder<NormalizedValidationRuleBuilder, NormalizedValidationRule> {

    private Normalizer.Form form = Normalizer.Form.NFC;

    /**
     * Creates a new builder for {@link NormalizedValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public NormalizedValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(NormalizedValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
    }

    /**
     * The normalization form the value must be in; NFC by default.
     *
     * @param form the form
     * @return this builder
     */
    public NormalizedValidationRuleBuilder form(Normalizer.Form form) {
        Assert.notNull(form, "form cannot be null.");
        this.form = form;
        return this;
    }

    /**
     * Creates a new {@link NormalizedValidationRule} configured with this builder's settings.
     *
     * @return a new {@link NormalizedValidationRule}
     */
    @Override
    protected NormalizedValidationRule createValueValidationRule() {
        return new NormalizedValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(), form);
    }
}
