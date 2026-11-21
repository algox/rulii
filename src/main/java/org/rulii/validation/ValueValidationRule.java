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

import org.rulii.annotation.Given;
import org.rulii.annotation.Otherwise;
import org.rulii.annotation.Param;
import org.rulii.annotation.PreCondition;
import org.rulii.bind.match.MatchByNameThenTypeMatchingStrategy;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;
import org.rulii.rule.ClassBasedRuleBuilder;
import org.rulii.rule.RuleExecutionStatus;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ValueValidationRule extends ValidationRule {

    private final Function<?> valueFunction;
    private final String valueName;
    private final Set<Class<?>> supportedTypes;
    private boolean failOnNull = true;
    private Map<String, Object> parameters = Map.of();

    public ValueValidationRule(Function<?> valueFunction, String errorCode, Severity severity, String errorMessage,
                               String defaultMessage, String valueName) {
        super(errorCode, severity, errorMessage, defaultMessage);
        Assert.notNull(valueFunction, "valueFunction cannot be null.");
        this.valueFunction = valueFunction;
        this.valueName = valueName != null ? valueName : "value";
        this.supportedTypes = new HashSet<>(getSupportedTypes());
    }

    /**
     * Checks the type of the value in the given RuleContext against the supported types.
     * A null value passes the check when {@code failOnNull} is true (default), letting the
     * rule condition decide the outcome; when {@code failOnNull} is false a null value fails
     * the precondition and the rule is skipped.
     *
     * @param ruleContext The RuleContext containing the value to check.
     * @return true if the value passes the null handling and type check, false otherwise.
     */
    @PreCondition
    public boolean checkType(@Param(matchUsing = MatchByNameThenTypeMatchingStrategy.class) RuleContext ruleContext) {
        Object value = getValue(ruleContext);
        if (value == null) return failOnNull;
        return isSupported(value.getClass());
    }

    /**
     * Determines whether the given RuleContext is valid by calling the overloaded method with the ruleContext and the binding value.
     *
     * @param ruleContext The RuleContext to validate.
     * @return True if the ruleContext is valid, false otherwise.
     */
    @Given
    public boolean isValid(@Param(matchUsing = MatchByNameThenTypeMatchingStrategy.class) RuleContext ruleContext) {
        return isValid(ruleContext, getValue(ruleContext));
    }

    /**
     * Executes the "otherwise" action of a rule. This method is used as the "else" condition in a rule and is executed when the rule condition evaluates to false.
     *
     * @param ruleContext      The RuleContext object containing the value to check.
     * @param ruleViolations  The RuleViolations object to add any potential violations to.
     */
    @Otherwise
    public void otherwise(@Param(matchUsing = MatchByNameThenTypeMatchingStrategy.class) RuleContext ruleContext,
                          @Param(matchUsing = MatchByNameThenTypeMatchingStrategy.class) RuleViolations ruleViolations) {
        if (ruleContext == null) throw new UnrulyException("RuleContext not defined.");
        if (ruleViolations == null) throw new UnrulyException("RuleViolations not defined. Please define org.rulii.validation.RuleViolations binding and try again.");

        ruleViolations.add(buildViolation(ruleContext, getValue(ruleContext)));
    }

    /**
     * Decides a value the way a run of this rule decides the value it reads, without running the rule: no value
     * function, no parameter matching, no trace. A null value is {@code SKIPPED} unless the rule fails on null
     * ({@link #isFailOnNull()}), in which case the rule decides; a value of a type the rule does not support
     * ({@link #isSupported(Class)}) is {@code SKIPPED}; any other value is {@code PASS} or {@code FAIL} as
     * {@link #isValid(RuleContext, Object)} has it. Nothing is reported: a caller that gets {@code FAIL} builds the
     * violation with {@link #buildViolation(RuleContext, Object)}.
     *
     * <p>For callers that already hold the value and run many rules against it, such as a bean validator. It is
     * the rule's own decision only when the rule class leaves {@link #checkType(RuleContext)},
     * {@link #isValid(RuleContext)} and {@link #otherwise(RuleContext, RuleViolations)} as they are defined here.</p>
     *
     * @param ruleContext the context the rule would run in; never null.
     * @param value the value to decide; may be null.
     * @return {@code PASS}, {@code FAIL} or {@code SKIPPED}; never {@code ERROR}, a failing check throws.
     * @since 2.3
     */
    public RuleExecutionStatus check(RuleContext ruleContext, Object value) {
        if (value == null ? !failOnNull : !isSupported(value.getClass())) return RuleExecutionStatus.SKIPPED;
        return isValid(ruleContext, value) ? RuleExecutionStatus.PASS : RuleExecutionStatus.FAIL;
    }

    /**
     * Builds the violation this rule reports for a value that failed it: the validated value as the first
     * parameter, the parameters configured on the builder, then whatever the rule adds
     * ({@link #customizeViolation(RuleContext, RuleViolationBuilder)}), with the message resolved through the
     * given context. This is what {@link #otherwise(RuleContext, RuleViolations)} adds to the collector.
     *
     * @param ruleContext the context the rule runs in; never null.
     * @param value the value that failed.
     * @return the violation; never null.
     * @since 2.3
     */
    public RuleViolation buildViolation(RuleContext ruleContext, Object value) {
        RuleViolationBuilder builder = RuleViolation.builder().with(this);
        // The validated value is always the first parameter; a configured parameter of the same name is dropped.
        builder.param(getValueName(), value);
        for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
            if (!parameter.getKey().equals(getValueName())) builder.param(parameter.getKey(), parameter.getValue());
        }
        customizeViolation(ruleContext, builder);
        return builder.build(ruleContext);
    }

    /**
     * Describes what this rule checks, naming the value: {@code code size must be between 3 and 20}. A rule built
     * without a description gets this one. The built-in rules each say what they check with their own parameters;
     * the default is the {@link org.rulii.annotation.Description} of the rule class, else its simple name.
     *
     * @return the description; never null.
     */
    public String describe() {
        String result = ClassBasedRuleBuilder.getRuleDescription(getClass());
        return result != null ? result : getClass().getSimpleName();
    }

    /**
     * Customizes a RuleViolationBuilder object based on the ruleContext and adds any additional information necessary.
     *
     * @param ruleContext The RuleContext object containing the value being validated.
     * @param builder The RuleViolationBuilder object to be customized.
     */
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {}

    /**
     * Checks if the given value is valid according to the rule context.
     *
     * @param ruleContext The RuleContext containing the value to be checked. Must not be null.
     * @param value       The value to be checked for validity.
     * @return true if the value is valid, false otherwise.
     */
    protected abstract boolean isValid(RuleContext ruleContext, Object value);

    /**
     * Retrieves the list of supported types for the validation rule.
     *
     * @return A list of Class objects representing the supported types.
     */
    public abstract List<Class<?>> getSupportedTypes();

    /**
     *
     * Checks if the given type is supported by the validation rule.
     *
     * @param type The Class object representing the type to check.
     * @return true if the type is supported, false otherwise.
     */
    public boolean isSupported(Class<?> type) {
        boolean result = false;

        if (supportedTypes == null || supportedTypes.isEmpty()) return false;
        if (supportedTypes.contains(type)) return true;

        for (Class<?> c : supportedTypes) {
            if (c.isAssignableFrom(type)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Determines how this rule treats a null value: passed on to the rule condition
     * (and typically failing validation), or skipped via the precondition.
     *
     * @return true if a null value proceeds to the rule condition (default); false if a null value skips the rule.
     */
    public boolean isFailOnNull() {
        return failOnNull;
    }

    /**
     * Sets the null handling behavior of this rule. Invoked by the builder during build.
     *
     * @param failOnNull true to pass null values on to the rule condition (default); false to skip the rule on null values.
     */
    void setFailOnNull(boolean failOnNull) {
        this.failOnNull = failOnNull;
    }

    /**
     * The parameters every violation of this rule reports beside the validated value and the rule's
     * own parameters, as configured on the builder: what a message template may refer to, over and
     * above what the rule itself knows.
     *
     * @return the parameters, in configuration order; empty by default.
     * @since 2.2
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Sets the configured violation parameters. Invoked by the builder during build.
     *
     * @param parameters the parameters; copied.
     */
    void setParameters(Map<String, Object> parameters) {
        Assert.notNull(parameters, "parameters cannot be null.");
        this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(parameters));
    }

    public Function<?> getValueFunction() {
        return valueFunction;
    }

    protected Object getValue(RuleContext ruleContext) {
        return valueFunction.run(ruleContext);
    }

    /**
     * The binding name of the value this rule validates.
     *
     * @return value name; never null.
     */
    public String getValueName() {
        return valueName;
    }

    /**
     * Coerces the given value into a Number. Returns null if the value is neither a Number
     * nor a parseable CharSequence.
     *
     * @param value the value to coerce.
     * @return the coerced Number, or null if it could not be coerced.
     */
    protected static Number toNumber(Object value) {
        if (value instanceof Number) return (Number) value;
        if (value instanceof CharSequence) {
            try {
                return new BigDecimal(value.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
