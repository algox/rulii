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
    private boolean anyDomain = false;
    private String pattern = null;
    private int flags = 0;

    /**
     * Creates a new builder for {@link EmailValidationRule}.
     *
     * @param valueFunction the function that supplies the value to validate
     */
    public EmailValidationRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(EmailValidationRule.ERROR_CODE);
        severity(Severity.ERROR);
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
     * Judges the domain part by shape alone: host name labels or an IP address in brackets, no
     * top-level domain registry, local and single-label hosts allowed ({@code user@localhost},
     * {@code bob@intranet}, a reserved or newly delegated top-level domain), as the Bean Validation
     * reference implementation accepts them. The local part is validated as before. Without it the
     * registry is consulted and {@link #allowLocal()} / {@link #allowTopLevelDomain()} apply.
     *
     * @return this builder for fluent chaining
     * @since 2.2
     */
    public EmailValidationRuleBuilder anyDomain() {
        this.anyDomain = true;
        return this;
    }

    /**
     * Requires the address to also match the given regular expression entirely, on top of being
     * well-formed; the rule reports it as the {@code pattern} violation parameter. One rule, one
     * verdict: an address failing both checks is reported once.
     *
     * @param pattern the regular expression; null for none.
     * @return this builder for fluent chaining
     */
    public EmailValidationRuleBuilder pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * The {@link java.util.regex.Pattern} flags to compile the pattern with.
     *
     * @param flags the flags; 0 for none.
     * @return this builder for fluent chaining
     */
    public EmailValidationRuleBuilder flags(int flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Creates a new {@link EmailValidationRule} configured with this builder's settings.
     *
     * @return a new {@link EmailValidationRule}
     */
    @Override
    protected EmailValidationRule createValueValidationRule() {
        return new EmailValidationRule(getValueFunction(), getErrorCode(), getSeverity(), getErrorMessage(), getValueName(),
                allowLocal, allowTopLevelDomain, pattern, flags, anyDomain);
    }
}
