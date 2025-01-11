/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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
        return new RuleSetBuilder<T>(name, description);
    }
}
