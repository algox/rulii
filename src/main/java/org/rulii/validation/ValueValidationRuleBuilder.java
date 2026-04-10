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
package org.rulii.validation;

import org.rulii.lib.spring.util.Assert;
import org.rulii.model.function.Function;
import org.rulii.rule.ClassBasedRuleBuilder;
import org.rulii.rule.Rule;

/**
 * Abstract base builder for {@link ValueValidationRule} subclasses. Provides fluent configuration
 * of the error code, severity, error message, and value name, and delegates rule creation to the
 * concrete subclass via {@link #createValueValidationRule()}. Use {@link #build()} to produce the
 * configured {@link org.rulii.rule.Rule}.
 *
 * @param <T> the concrete builder type (for fluent chaining)
 * @param <V> the concrete {@link ValueValidationRule} type produced by this builder
 * @author Max Arulananthan
 * @since 1.2
 */
public abstract class ValueValidationRuleBuilder<T extends ValueValidationRuleBuilder<T, V>, V extends ValueValidationRule> {

    public static final String DEFAULT_VALUE_NAME = "value";

    private String name;
    private String description;
    private final Function<?> valueFunction;
    private String errorCode;
    private Severity severity = Severity.ERROR;
    private String errorMessage;
    private String valueName;

    /**
     * Creates a new builder configured with the given value function.
     * If {@code valueFunction} is a {@link BindingFunction}, the binding name is automatically
     * extracted and used as the value name in rule violations.
     *
     * @param valueFunction the function that supplies the value to be validated; must not be null
     */
    protected ValueValidationRuleBuilder(Function<?> valueFunction) {
        super();
        Assert.notNull(valueFunction, "valueFunction cannot be null.");
        this.valueFunction = valueFunction instanceof BindingFunction ? ((BindingFunction<?>) valueFunction).getTargetFunction() : valueFunction;
        this.valueName = valueFunction instanceof BindingFunction ? ((BindingFunction<?>) valueFunction).getBindingName() : DEFAULT_VALUE_NAME;
    }

    @SuppressWarnings("unchecked")
    public T name(String name) {
        this.name = name;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T description(String description) {
        this.description = description;
        return (T) this;
    }

    /**
     * Sets the error code used in rule violations.
     *
     * @param errorCode the error code string; overrides the rule's default error code
     * @return this builder for fluent chaining
     */
    @SuppressWarnings("unchecked")
    public T errorCode(String errorCode) {
        Assert.hasText(errorCode, "errorCode cannot be null or empty.");
        this.errorCode = errorCode;
        return (T) this;
    }

    /**
     * Sets the severity of the rule violation.
     *
     * @param severity the violation severity; must not be null
     * @return this builder for fluent chaining
     */
    @SuppressWarnings("unchecked")
    public T severity(Severity severity) {
        Assert.notNull(severity, "severity cannot be null.");
        this.severity = severity;
        return (T) this;
    }

    /**
     * Sets a custom error message for rule violations, overriding the default message.
     *
     * @param errorMessage the custom error message
     * @return this builder for fluent chaining
     */
    @SuppressWarnings("unchecked")
    public T message(String errorMessage) {
        Assert.hasText(errorMessage, "errorMessage cannot be null or empty.");
        this.errorMessage = errorMessage;
        return (T) this;
    }

    /**
     * Sets the value name used as the parameter key in rule violations.
     *
     * @param valueName the parameter key name; defaults to the binding name or {@code "value"}
     * @return this builder for fluent chaining
     */
    @SuppressWarnings("unchecked")
    public T valueName(String valueName) {
        Assert.hasText(valueName, "valueName cannot be null or empty.");
        this.valueName = valueName;
        return (T) this;
    }

    protected String getErrorCode() {
        return errorCode;
    }

    protected Severity getSeverity() {
        return severity;
    }

    protected String getErrorMessage() {
        return errorMessage;
    }

    protected Function<?> getValueFunction() {
        return valueFunction;
    }

    public String getValueName() {
        return valueName;
    }

    /**
     * Creates the concrete {@link ValueValidationRule} instance configured by this builder.
     * Subclasses must implement this method to instantiate their specific rule type.
     *
     * @return a new {@link ValueValidationRule} configured with this builder's settings
     */
    protected abstract V createValueValidationRule();

    /**
     * Builds and returns a {@link Rule} wrapping the configured {@link ValueValidationRule}.
     *
     * @return the fully configured {@link org.rulii.rule.Rule}
     */
    public Rule build() {
        ClassBasedRuleBuilder<?> builder = Rule.builder().with(createValueValidationRule());
        if (name != null) builder.name(name);
        builder.description(description);
        return builder.build();
    }
}
