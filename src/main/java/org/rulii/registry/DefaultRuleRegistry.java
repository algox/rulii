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

package org.rulii.registry;

import org.rulii.model.Runnable;
import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the RuleRegistry interface that manages rules and rule sets.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultRuleRegistry implements MutableRuleRegistry {

    private static final Log logger = LogFactory.getLog(DefaultRuleRegistry.class);

    private final Map<String, Runnable<?>> registry = new ConcurrentHashMap<>();

    public DefaultRuleRegistry() {
        super();
    }

    @Override
    public boolean isNameInUse(String name) {
        Assert.notNull(name, "name cannot be null.");
        return registry.containsKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends Runnable<R>> T get(String name) {
        Assert.notNull(name, "name cannot be null.");
        return (T) registry.get(name);
    }

    @Override
    public List<Rule> getRules() {
        return filter(Rule.class);
    }

    @Override
    public List<RuleSet> getRuleSets() {
        return filter(RuleSet.class);
    }

    @Override
    public int getCount() {
        return registry.size();
    }

    @Override
    public void register(Runnable<?> r) {
        Assert.notNull(r, "r cannot be null.");

        if (isNameInUse(r.getName())) {
            throw new AlreadyRegisteredException(r.getName(), r);
        }

        registry.putIfAbsent(r.getName(), r);

        if (logger.isDebugEnabled()) {
            logger.debug("Runnable [" + r.getClass().getSimpleName() + "] Registered as [" + r.getName() + "]");
        }
    }

    /**
     * Filters the registry to retrieve a list of objects of a specific type.
     *
     * @param <T> the type of the objects to filter
     * @param type the class object representing the type to filter for
     * @return a list of objects of the specified type from the registry
     */
    @SuppressWarnings("unchecked")
    private <T extends Runnable<?>> List<T> filter(Class<T> type) {
        List<T> result = new ArrayList<>();

        registry.values()
                .stream()
                .filter(r -> type.isAssignableFrom(r.getClass()))
                .forEach(r -> result.add((T) r));

        return Collections.unmodifiableList(result);
    }

    @Override
    public String toString() {
        return "DefaultRuleRegistry{" +
                "registry=" + registry +
                '}';
    }
}

