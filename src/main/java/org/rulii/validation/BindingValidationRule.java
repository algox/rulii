/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.context.RuleContext;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;

import java.util.List;

/**
 * Abstract class representing a binding validation rule.
 * A binding validation rule is a rule that validates a value associated with a binding.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public abstract class BindingValidationRule extends ValidationRule {

    private static final Log LOG = LogFactory.getLog(BindingValidationRule.class);

    private final BindingSupplier bindingSupplier;

    /**
     * This class represents a validation rule used for binding validation.
     *
     * @param bindingName      The name of the binding to validate.
     * @param errorCode        The error code associated with the validation rule.
     * @param severity         The severity of the error.
     * @param defaultMessage  The default error message to be used if errorMessage is null.
     */
    public BindingValidationRule(String bindingName, String errorCode, Severity severity, String defaultMessage) {
        this(bindingName, errorCode, severity, null, defaultMessage);
    }

    /**
     * This class represents a validation rule used for binding validation.
     *
     * @param bindingName      The name of the binding to validate.
     * @param errorCode        The error code associated with the validation rule.
     * @param severity         The severity of the error.
     * @param errorMessage     The error message that will be displayed if the validation rule fails.
     * @param defaultMessage  The default error message to be used if errorMessage is null.
     */
    public BindingValidationRule(String bindingName, String errorCode, Severity severity,
                                 String errorMessage, String defaultMessage) {
        this(new SimpleBindingSupplier(bindingName) , errorCode, severity, errorMessage, defaultMessage);
    }

    /**
     * Represents a validation rule used for binding validation.
     *
     * @param bindingSupplier  The supplier of bindings for rule evaluation. Must not be null.
     * @param errorCode        The error code associated with the validation rule.
     * @param severity         The severity of the error.
     * @param errorMessage     The error message that will be displayed if the validation rule fails.
     * @param defaultMessage  The default error message to be used if errorMessage is null.
     */
    public BindingValidationRule(BindingSupplier bindingSupplier, String errorCode, Severity severity,
                                 String errorMessage, String defaultMessage) {
        super(errorCode, severity, errorMessage, defaultMessage);
        Assert.notNull(bindingSupplier, "bindingSupplier cannot be null.");
        this.bindingSupplier = bindingSupplier;
    }

    /**
     * Checks the type of the value in the given RuleContext against the supported types.
     *
     * @param ruleContext The RuleContext containing the value to check.
     * @return true if the value is null or its type is supported, false otherwise.
     */
    @PreCondition
    public boolean checkType(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext) {
        if (ruleContext == null) throw new IllegalStateException("RuleContext not defined.");

        Object value = getBindingValue(ruleContext);

        boolean result = value == null || isSupported(value.getClass());
        if (!result) LOG.warn("value is null or not supported.");
        return result;
    }

    /**
     * Determines whether the given RuleContext is valid by calling the overloaded method with the ruleContext and the binding value.
     *
     * @param ruleContext The RuleContext to validate.
     * @return True if the ruleContext is valid, false otherwise.
     */
    @Given
    public boolean isValid(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext) {
        if (ruleContext == null) throw new IllegalStateException("RuleContext not defined.");
        return isValid(ruleContext, getBindingValue(ruleContext));
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
        if (ruleContext == null) throw new IllegalStateException("RuleContext not defined.");
        if (ruleViolations == null) throw new IllegalStateException("RuleViolations not defined. Please define org.rulii.validation.RuleViolations binding and try again.");

        Object value = getBindingValue(ruleContext);
        RuleViolationBuilder builder = RuleViolation.builder().with(this);
        builder.param(bindingSupplier.getName(ruleContext.getBindings()), value);

        String parentName = bindingSupplier.getParentName(ruleContext.getBindings());
        if (parentName != null) builder.param("parent", parentName);

        customizeViolation(ruleContext, builder);
        //ruleContext.getMessageResolver(), ruleContext.getMessageFormatter(), ruleContext.getLocale())
        ruleViolations.add(builder.build());
    }

    /**
     * Checks if the given value is valid according to the rule context.
     *
     * @param ruleContext The RuleContext containing the value to be checked. Must not be null.
     * @param value       The value to be checked for validity.
     * @return true if the value is valid, false otherwise.
     */
    protected abstract boolean isValid(RuleContext ruleContext, Object value);

    /**
     * Customizes a RuleViolationBuilder object based on the ruleContext and adds any additional information necessary.
     *
     * @param ruleContext The RuleContext object containing the value being validated.
     * @param builder The RuleViolationBuilder object to be customized.
     */
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {}

    /**
     * Retrieves the binding supplier associated with this validation rule.
     *
     * @return The binding supplier.
     */
    protected BindingSupplier getBindingSupplier() {
        return bindingSupplier;
    }

    /**
     * Retrieves the value of the binding from the provided RuleContext.
     *
     * @param ruleContext The RuleContext containing the binding value. Must not be null.
     * @return The value of the binding.
     */
    protected Object getBindingValue(RuleContext ruleContext) {
        return bindingSupplier.getValue(ruleContext.getBindings());
    }

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

        for (Class<?> c : getSupportedTypes()) {
            if (c.isAssignableFrom(type)) {
                result = true;
                break;
            }
        }

        return result;
    }
}
