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
package org.rulii.ruleset;

import org.rulii.bind.Bindings;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.model.action.Action;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.ValidationException;

/**
 * RuleSetBuilderBuilder is a final class that provides a builder pattern implementation
 * to construct RuleSet objects. It contains methods to create a RuleSetBuilder instance
 * with a given name and optional description, and to configure different aspects of the
 * RuleSet such as preCondition, stopCondition, initializer, finalizer, resultExtractor,
 * and rules. RuleSetBuilderBuilder follows a fluent interface style allowing method chaining
 * for easy and concise configuration of RuleSet objects.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class RuleSetBuilderBuilder {

    /**
     * Singleton instance of RuleSetBuilderBuilder class used to access and construct RuleSet objects.
     */
    private static final RuleSetBuilderBuilder instance = new RuleSetBuilderBuilder();

    private RuleSetBuilderBuilder() {
        super();
    }

    /**
     * Returns the singleton instance of RuleSetBuilderBuilder for accessing and constructing RuleSet objects.
     *
     * @return the singleton instance of RuleSetBuilderBuilder
     */
    public static RuleSetBuilderBuilder getInstance() {
        return instance;
    }

    /**
     * Constructs a new RuleSetBuilder with the given name.
     *
     * @param name the name to set for the RuleSetBuilder
     * @return a new RuleSetBuilder instance with the specified name
     */
    public <T> RuleSetBuilder<T> with(String name) {
        return new RuleSetBuilder<T>(name);
    }

    /**
     * Constructs a new RuleSetBuilder instance with the given name and description.
     *
     * @param <T> the type of the RuleSet object
     * @param name the name to set for the RuleSetBuilder
     * @param description the description to set for the RuleSetBuilder
     * @return a new RuleSetBuilder instance with the specified name and description
     */
    public <T> RuleSetBuilder<T> with(String name, String description) {
        return new RuleSetBuilder<>(name, description);
    }

    /**
     * Builds a RuleSetBuilder with the provided rules.
     *
     * @param rules the RuleSet to use for building the RuleSetBuilder
     * @return a new RuleSetBuilder instance with the specified rules
     */
    public <T> RuleSetBuilder<T> with(RuleSet<T> rules) {
        return new RuleSetBuilder<>(rules);
    }

    public RuleSetBuilder<RuleSetExecutionStatus> validating(String name) {
        return validating(name, null);
    }

    /**
     * Creates a Validating RuleSetBuilder with the given name and description, adding an initializer to bind ruleViolations and a finalizer to check for any errors.
     *
     * @param name the name of the RuleSetBuilder to validate
     * @param description the description of the RuleSetBuilder to validate
     * @return a RuleSetBuilder instance with added initializer and finalizer for validation
     * @throws ValidationException of there are any validation errors during the run.
     */
    public RuleSetBuilder<RuleSetExecutionStatus> validating(String name, String description) {
        RuleSetBuilder<RuleSetExecutionStatus> result = with(name, description);
        // Add the error container.
        result.initializer(Action.builder().with((Bindings bindings) -> bindings.bind("ruleViolations", new RuleViolations()))
                .param(0).matchUsing(MatchByTypeMatchingStrategy.class).build()
                .build());
        // Throw a ValidationException if there are any errors during the run.
        result.finalizer(Action.builder().with((Bindings bindings) -> {
            RuleViolations ruleViolations = bindings.getValue("ruleViolations");
            if (ruleViolations.hasErrors()) throw new ValidationException(ruleViolations);
        }).param(0).matchUsing(MatchByTypeMatchingStrategy.class).build()
                .build());

        return result;
    }
}
