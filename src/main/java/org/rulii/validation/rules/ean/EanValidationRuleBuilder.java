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
package org.rulii.validation.rules.ean;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link EanValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class EanValidationRuleBuilder
        extends ValueValidationRuleBuilder<EanValidationRuleBuilder, EanValidationRule> {

    private EanValidationRule.Type type = EanValidationRule.Type.EAN13;

    /**
     * Creates a new builder for {@link EanValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public EanValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(EanValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
    }

    /**
     * The EAN format; EAN-13 by default.
     *
     * @param type the format
     * @return this builder
     */
    public EanValidationRuleBuilder type(EanValidationRule.Type type) {
        Assert.notNull(type, "type cannot be null.");
        this.type = type;
        return this;
    }

    /**
     * Creates a new {@link EanValidationRule} configured with this builder's settings.
     *
     * @return a new {@link EanValidationRule}
     */
    @Override
    protected EanValidationRule createValueValidationRule() {
        return new EanValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(), type);
    }
}
