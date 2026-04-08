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
import org.rulii.bind.NoSuchBindingException;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.model.function.Function;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ValueValidationRule extends ValidationRule {

    private final Function<?> valueFunction;
    private final String valueName;
    private final Set<Class<?>> supportedTypes;

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
     *
     * @param ruleContext The RuleContext containing the value to check.
     * @return true if the value is null or its type is supported, false otherwise.
     */
    @PreCondition
    public boolean checkType(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext) {
        try {
            Object value = getValue(ruleContext);
            return value == null || isSupported(value.getClass());
        } catch (NoSuchBindingException e) {
            return false;
        }
    }

    /**
     * Determines whether the given RuleContext is valid by calling the overloaded method with the ruleContext and the binding value.
     *
     * @param ruleContext The RuleContext to validate.
     * @return True if the ruleContext is valid, false otherwise.
     */
    @Given
    public boolean isValid(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext) {
        return isValid(ruleContext, getValue(ruleContext));
    }

    /**
     * Executes the "otherwise" action of a rule. This method is used as the "else" condition in a rule and is executed when the rule condition evaluates to false.
     *
     * @param ruleContext      The RuleContext object containing the value to check.
     * @param ruleViolations  The RuleViolations object to add any potential violations to.
     */
    @Otherwise
    public void otherwise(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext,
                          @Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleViolations ruleViolations) {
        if (ruleContext == null) throw new UnrulyException("RuleContext not defined.");
        if (ruleViolations == null) throw new UnrulyException("RuleViolations not defined. Please define org.rulii.validation.RuleViolations binding and try again.");

        Object value = getValue(ruleContext);
        RuleViolationBuilder builder = RuleViolation.builder().with(this);
        builder.param(getValueName(), value);
        customizeViolation(ruleContext, builder);
        ruleViolations.add(builder.build(ruleContext));
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

    public Function<?> getValueFunction() {
        return valueFunction;
    }

    protected Object getValue(RuleContext ruleContext) {
        return valueFunction.run(ruleContext);
    }

    protected String getValueName() {
        return valueName;
    }
}
