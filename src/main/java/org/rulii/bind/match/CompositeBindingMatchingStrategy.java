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
package org.rulii.bind.match;

import org.rulii.bind.Bindings;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Composite Strategy class that aggregates the matches by running the given delegator Strategies. The exit point can
 * be setup during the creation of the CompositeBindingMatchingStrategy.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see BindingMatchingStrategy
 *
 */
public class CompositeBindingMatchingStrategy implements BindingMatchingStrategy {

    private static final Log logger = LogFactory.getLog(CompositeBindingMatchingStrategy.class);

    private final boolean stopWhenMatched;
    private final BindingMatchingStrategy[] strategies;

    /**
     * Creates CompositeBindingMatchingStrategy with the given delegating sub BindingMatchingStrategies.
     *
     * @param stopWhenMatched determines if this CompositeBindingMatchingStrategy will stop once a match(es) are found.
     * @param strategies delegating sub BindingMatchingStrategies
     */
    public CompositeBindingMatchingStrategy(boolean stopWhenMatched, BindingMatchingStrategy...strategies) {
        super();
        Assert.notNull(strategies, "strategies cannot be null");
        Assert.isTrue(strategies.length > 1, "CompositeBindingMatchingStrategy takes in at least 2 BindingMatchingStrategies");
        this.stopWhenMatched = stopWhenMatched;
        this.strategies = strategies;
    }

    @Override
    public <T> List<BindingMatch<T>> match(Bindings bindings, String name, Type type, boolean containsGenericInfo) {
        Assert.hasText(name, "name cannot be null/empty.");
        Assert.notNull(type, "type cannot be null/empty.");

        List<BindingMatch<T>> result = new LinkedList<>();

        for (BindingMatchingStrategy strategy : strategies) {
            // Add all the matches
            result.addAll(strategy.match(bindings, name, type, containsGenericInfo));
            // Check to see if we should stop
            if (stopWhenMatched && !result.isEmpty()) break;
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public String getDescription() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("CompositeBindingMatchingStrategy(");

        for (int i = 0; i < strategies.length; i++) {
            result.append(strategies[i]);
            if (i < strategies.length - 1) result.append(",");
        }

        result.append(")");
        return result.toString();
    }
}
