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
package org.rulii.bind.match;

/**
 * Convenient way to map between a Name to the BindingMatchingStrategy.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public enum BindingMatchingStrategyType {

    // Searches for Bindings with the given name.
    MATCH_BY_NAME (new MatchByNameMatchingStrategy()),

    // Searches for Bindings with the given type.
    MATCH_BY_TYPE (new SmartMatchByTypeMatchingStrategy()),

    // Searches for Bindings with the given name & type
    MATCH_BY_NAME_AND_TYPE (new MatchByNameAndTypeMatchingStrategy()),

    // Searches for Bindings with the given name & type if nothing is found then continues to search by Type
    MATCH_BY_NAME_AND_TYPE_THEN_BY_JUST_BY_TYPE (
            new CompositeBindingMatchingStrategy(true,
                    new MatchByNameAndTypeMatchingStrategy(),
                    new SmartMatchByTypeMatchingStrategy())),

    // Searches for Bindings with the given name if nothing is found then continues to search by Type
    MATCH_BY_NAME_THEN_BY_TYPE (
            new CompositeBindingMatchingStrategy(true,
                    new MatchByNameMatchingStrategy(),
                    new SmartMatchByTypeMatchingStrategy()));

    private final BindingMatchingStrategy strategy;

    BindingMatchingStrategyType(BindingMatchingStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Retrieves the Strategy that is associated to this Type.
     *
     * @return associated BindingMatchingStrategy
     */
    public BindingMatchingStrategy getStrategy() {
        return strategy;
    }

    /**
     * Returns the Class of the Strategy;
     *
     * @return Strategy class.
     */
    public Class<? extends BindingMatchingStrategy> getStrategyClass() {
        return strategy.getClass();
    }

    /**
     * Returns the default Strategy Type.
     *
     * @return default strategy type.
     */
    public static BindingMatchingStrategyType getDefault() {
        return MATCH_BY_NAME_THEN_BY_TYPE;
    }
}
