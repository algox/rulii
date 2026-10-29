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

import org.rulii.annotation.Description;
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.validation.EmailValidator;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRule;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation Rule to make sure the value must match an email regex format.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
@Rule
@Description("Value must match an email regex pattern.")
public class EmailValidationRule extends ValueValidationRule {

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "emailValidationRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Invalid email address {0}.";

    private final boolean allowLocal;
    private final boolean allowTopLevelDomain;
    private final boolean anyDomain;
    private final String pattern;
    private final int flags;
    private final Pattern compiledPattern;
    private final EmailValidator validator;

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate
     * @return a new {@link EmailValidationRuleBuilder}
     */
    public static EmailValidationRuleBuilder builder(Function<?> function) {
        return new EmailValidationRuleBuilder(function);
    }

    EmailValidationRule(Function<?> valueFunction, String errorCode, Severity severity,
                        String errorMessage, String valueName, boolean allowLocal, boolean allowTopLevelDomain) {
        this(valueFunction, errorCode, severity, errorMessage, valueName, allowLocal, allowTopLevelDomain, null, 0);
    }

    EmailValidationRule(Function<?> valueFunction, String errorCode, Severity severity,
                        String errorMessage, String valueName, boolean allowLocal, boolean allowTopLevelDomain,
                        String pattern, int flags) {
        this(valueFunction, errorCode, severity, errorMessage, valueName, allowLocal, allowTopLevelDomain, pattern, flags, false);
    }

    EmailValidationRule(Function<?> valueFunction, String errorCode, Severity severity,
                        String errorMessage, String valueName, boolean allowLocal, boolean allowTopLevelDomain,
                        String pattern, int flags, boolean anyDomain) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
        this.allowLocal = allowLocal;
        this.allowTopLevelDomain = allowTopLevelDomain;
        this.anyDomain = anyDomain;
        this.pattern = pattern;
        this.flags = flags;
        this.compiledPattern = pattern != null ? Pattern.compile(pattern, flags) : null;
        this.validator = anyDomain ? new AnyDomainEmailValidator() : EmailValidator.getInstance(allowLocal, allowTopLevelDomain);
    }

    /**
     * A well-formed address that, when a pattern is configured, also matches it entirely; one
     * verdict for both checks.
     */
    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;

        if (!(value instanceof CharSequence))
            throw new UnrulyException("EmailValidationRule only applies to CharSequences."
                    + "Supplied Class [" + value.getClass() + "] value [" + value + "]");

        String text = value.toString();
        return validator.isValid(text) && (compiledPattern == null || compiledPattern.matcher(text).matches());
    }

    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        if (pattern != null) builder.param("pattern", pattern);
    }

    public boolean isAllowLocal() {
        return allowLocal;
    }

    public boolean isAllowTopLevelDomain() {
        return allowTopLevelDomain;
    }

    /**
     * Whether the domain part is judged by shape alone (host name labels or an IP literal, no
     * top-level domain registry, local names allowed), as the Bean Validation reference
     * implementation does; false (default) consults the registry and the {@code allowLocal} /
     * {@code allowTopLevelDomain} settings.
     *
     * @return true for shape-only domain validation.
     */
    public boolean isAnyDomain() {
        return anyDomain;
    }

    /**
     * The regular expression the address must also match entirely; null when none is configured.
     *
     * @return the pattern, or null.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * The {@link Pattern} flags the pattern is compiled with.
     *
     * @return the flags; 0 for none.
     */
    public int getFlags() {
        return flags;
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public String describe() {
        return getValueName() + " must be a valid email address";
    }

    @Override
    public String toString() {
        return "EmailValidationRule{" +
                "allowLocal=" + allowLocal +
                ", allowTopLevelDomain=" + allowTopLevelDomain +
                (anyDomain ? ", anyDomain=true" : "") +
                (pattern != null ? ", pattern='" + pattern + "', flags=" + flags : "") +
                '}';
    }
}
