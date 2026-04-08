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
package org.rulii.validation.rules.email;

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Builder for {@link EmailValidationRule}. Use {@link #build()} to create the configured {@link org.rulii.rule.Rule}.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class EmailValidationRuleBuilder
        extends ValueValidationRuleBuilder<EmailValidationRuleBuilder, EmailValidationRule> {

    private boolean allowLocal = false;
    private boolean allowTopLevelDomain = false;

    /**
     * Creates a new builder for {@link EmailValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public EmailValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(EmailValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
        message(EmailValidationRule.DEFAULT_MESSAGE);
    }

    /**
     * Configures the rule to allow local email addresses (addresses without a domain).
     *
     * @return this builder for fluent chaining
     */
    public EmailValidationRuleBuilder allowLocal() {
        this.allowLocal = true;
        return this;
    }

    /**
     * Configures the rule to allow top-level domain email addresses (e.g. {@code user@com}).
     *
     * @return this builder for fluent chaining
     */
    public EmailValidationRuleBuilder allowTopLevelDomain() {
        this.allowTopLevelDomain = true;
        return this;
    }

    /**
     * Creates a new {@link EmailValidationRule} configured with this builder's settings.
     *
     * @return a new {@link EmailValidationRule}
     */
    @Override
    protected EmailValidationRule createValueValidationRule() {
        return new EmailValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(), allowLocal, allowTopLevelDomain);
    }
}
