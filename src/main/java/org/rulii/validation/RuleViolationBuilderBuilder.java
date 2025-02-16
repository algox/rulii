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

import org.rulii.lib.spring.util.Assert;

/**
 * The RuleViolationBuilderBuilder class is a singleton class used to build RuleViolationBuilder objects.
 * It provides methods to create instances of RuleViolationBuilder.
 * RuleViolationBuilder objects are used to build RuleViolation objects and set various attributes such as error code,
 * severity, error message, default message, and parameters.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public final class RuleViolationBuilderBuilder {

    private static final RuleViolationBuilderBuilder instance = new RuleViolationBuilderBuilder();

    private RuleViolationBuilderBuilder() {
        super();
    }

    /**
     * Returns an instance of RuleViolationBuilderBuilder.
     *
     * @return an instance of RuleViolationBuilderBuilder
     */
    public static RuleViolationBuilderBuilder getInstance() {
        return instance;
    }

    /**
     * Creates a new RuleViolationBuilder object with the specified rule name.
     *
     * @param ruleName The name of the rule.
     * @return A RuleViolationBuilder object.
     */
    public RuleViolationBuilder with(String ruleName) {
        return new RuleViolationBuilder(ruleName);
    }

    /**
     * Builds a RuleViolation object based on the provided rule name and error code.
     *
     * @param ruleName The name of the rule.
     * @param errorCode The violation error code.
     * @return The constructed RuleViolation object.
     */
    public RuleViolation build(String ruleName, String errorCode) {
        return with(ruleName, errorCode).build();
    }

    /**
     * Builds a RuleViolation object with the specified rule name, error code, and message.
     *
     * @param ruleName The name of the rule.
     * @param errorCode The violation error code.
     * @param message The error message.
     * @return The constructed RuleViolation object.
     */
    public RuleViolation build(String ruleName, String errorCode, String message) {
        return with(ruleName, errorCode).errorMessage(message).build();
    }

    /**
     * Creates a new RuleViolationBuilder object with the specified rule name.
     *
     * @param ruleName The name of the rule.
     * @param errorCode violation error code.
     * @return A RuleViolationBuilder object.
     */
    public RuleViolationBuilder with(String ruleName, String errorCode) {
        RuleViolationBuilder builder = with(ruleName);
        return builder.errorCode(errorCode);
    }

    /**
     * Builds a RuleViolationBuilder object with the given ValidationRule.
     *
     * @param rule The ValidationRule to use for building the RuleViolationBuilder.
     * @return A RuleViolationBuilder object.
     * @throws IllegalArgumentException if the rule is null.
     */
    public RuleViolationBuilder with(ValidationRule rule) {
        Assert.notNull(rule, "rule cannot be null.");
        return new RuleViolationBuilder(rule.getName())
                .errorCode(rule.getErrorCode())
                .severity(rule.getSeverity())
                .errorMessage(rule.getErrorMessage())
                .defaultMessage(rule.getDefaultMessage());
    }
}
