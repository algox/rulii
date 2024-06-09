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

import org.rulii.lib.spring.util.Assert;

/**
 * BindingMatchingStrategy Builder.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see BindingMatchingStrategy
 */
public final class BindingMatchingStrategyBuilder {

    private static final BindingMatchingStrategyBuilder INSTANCE = new BindingMatchingStrategyBuilder();

    public BindingMatchingStrategyBuilder() {
        super();
    }

    public static BindingMatchingStrategyBuilder getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the matching strategy for the given type.
     *
     * @param type matching strategy type.
     * @return binding strategy.
     */
    public BindingMatchingStrategy build(BindingMatchingStrategyType type) {
        Assert.notNull(type, "type cannot be null.");
        return type.getStrategy();
    }

    /**
     * Returns a composite matching strategy composing of all the given strategies.
     *
     * @param stopWhenMatched stop when a match is found.
     * @param strategies child matching strategies.
     * @return composite binding strategy.
     */
    public BindingMatchingStrategy buildComposite(boolean stopWhenMatched, BindingMatchingStrategy...strategies) {
        return new CompositeBindingMatchingStrategy(stopWhenMatched, strategies);
    }

    /**
     * Returns the match by name strategy.
     *
     * @return binding strategy.
     */
    public BindingMatchingStrategy matchByName() {
        return BindingMatchingStrategyType.MATCH_BY_NAME.getStrategy();
    }

    /**
     * Returns the match by type strategy.
     *
     * @return binding strategy.
     */

    public BindingMatchingStrategy matchByType() {
        return BindingMatchingStrategyType.MATCH_BY_TYPE.getStrategy();
    }

    /**
     * Returns the match by name and type strategy.
     *
     * @return binding strategy.
     */
    public BindingMatchingStrategy matchByNameAndType() {
        return BindingMatchingStrategyType.MATCH_BY_NAME_AND_TYPE.getStrategy();
    }

    /**
     * Returns the match by name first if none are found then use type strategy.
     *
     * @return binding strategy.
     */
    public BindingMatchingStrategy matchByNameThenByType() {
        return BindingMatchingStrategyType.MATCH_BY_NAME_THEN_BY_TYPE.getStrategy();
    }

    /**
     * Returns the match by name and type first if none are found then use just type strategy.
     *
     * @return binding strategy.
     */
    public BindingMatchingStrategy matchByNameAndTypeThenByJustType() {
        return BindingMatchingStrategyType.MATCH_BY_NAME_AND_TYPE_THEN_BY_JUST_BY_TYPE.getStrategy();
    }

    /**
     * Builds the default BindingMatchingStrategy.
     *
     * @return BindingMatchingStrategy.
     */
    public BindingMatchingStrategy build() {
        return build(BindingMatchingStrategyType.getDefault());
    }
}
