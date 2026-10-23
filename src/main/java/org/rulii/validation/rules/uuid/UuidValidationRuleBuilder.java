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
package org.rulii.validation.rules.uuid;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link UuidValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class UuidValidationRuleBuilder
        extends ValueValidationRuleBuilder<UuidValidationRuleBuilder, UuidValidationRule> {

    private boolean allowEmpty = false;
    private boolean allowNil = true;
    private int[] versions = UuidValidationRule.DEFAULT_VERSIONS;
    private int[] variants = UuidValidationRule.DEFAULT_VARIANTS;
    private UuidValidationRule.LetterCase letterCase = UuidValidationRule.LetterCase.LOWER_CASE;

    /**
     * Creates a new builder for {@link UuidValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public UuidValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(UuidValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
    }

    /**
     * Whether the empty string passes; false by default.
     *
     * @param allowEmpty true to accept the empty string
     * @return this builder
     */
    public UuidValidationRuleBuilder allowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
        return this;
    }

    /**
     * Whether the nil UUID (all zeros) passes; true by default.
     *
     * @param allowNil true to accept the nil UUID
     * @return this builder
     */
    public UuidValidationRuleBuilder allowNil(boolean allowNil) {
        this.allowNil = allowNil;
        return this;
    }

    /**
     * The accepted versions; 1 to 5 by default.
     *
     * @param versions the versions
     * @return this builder
     */
    public UuidValidationRuleBuilder versions(int... versions) {
        Assert.notNull(versions, "versions cannot be null.");
        this.versions = versions;
        return this;
    }

    /**
     * The accepted variants; 0 to 2 by default.
     *
     * @param variants the variants
     * @return this builder
     */
    public UuidValidationRuleBuilder variants(int... variants) {
        Assert.notNull(variants, "variants cannot be null.");
        this.variants = variants;
        return this;
    }

    /**
     * The letter case of the hex digits; lower case by default.
     *
     * @param letterCase the letter case
     * @return this builder
     */
    public UuidValidationRuleBuilder letterCase(UuidValidationRule.LetterCase letterCase) {
        Assert.notNull(letterCase, "letterCase cannot be null.");
        this.letterCase = letterCase;
        return this;
    }

    /**
     * Creates a new {@link UuidValidationRule} configured with this builder's settings.
     *
     * @return a new {@link UuidValidationRule}
     */
    @Override
    protected UuidValidationRule createValueValidationRule() {
        return new UuidValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(),
                allowEmpty, allowNil, versions, variants, letterCase);
    }
}
