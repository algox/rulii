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

package org.rulii.registry;

import org.rulii.model.Runnable;
import org.rulii.lib.spring.util.Assert;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * RuleRegistry defines the interface for managing rules and rule sets.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface RuleRegistry {

    /**
     * Creates a new instance of {@link RuleRegistryBuilder}.
     *
     * @return the new instance of {@link RuleRegistryBuilder}.
     */
    static RuleRegistryBuilder builder() {
        return new RuleRegistryBuilder();
    }

    /**
     * Checks whether a name is already in use.
     *
     * @param name the name to check
     * @return true if the name is in use, false otherwise
     */
    boolean isNameInUse(String name);

    /**
     * Retrieves the count of something.
     *
     * @return the count of something.
     */
    int getCount();

    /**
     * Retrieves the list of rules from the rule registry.
     *
     * @return the list of rules.
     */
    List<Rule> getRules();

    /**
     * Retrieves the list of rule sets from the rule registry.
     *
     * @return the list of rule sets.
     */
    List<RuleSet> getRuleSets();

    /**
     * Retrieves a Runnable object from the rule registry using its name.
     *
     * @param <R> the return type of the Runnable.
     * @param <T> a subtype of Runnable.
     * @param name the name of the Runnable.
     * @return a Runnable object with the specified name, or null if not found.
     */
    <R, T extends Runnable<R>> T get(String name);

    /**
     * Retrieves a Runnable object from the rule registry using its name and type.
     *
     * @param <R> the return type of the Runnable.
     * @param <T> a subtype of Runnable.
     * @param name the name of the Runnable.
     * @param type the type of the Runnable to retrieve.
     * @return a Runnable object with the specified name and type, or null if not found.
     */
    @SuppressWarnings("unchecked")
    default <R, T extends Runnable<R>> T get(String name, Class<T> type) {
        Assert.notNull(type, "type cannot be null.");
        Runnable<R> result = get(name);
        if (result == null) return null;
        return (type.isAssignableFrom(result.getClass())) ? (T) result : null;
    }

    /**
     * Retrieves a rule object from the rule registry using its name.
     *
     * @param name the name of the rule.
     * @return a rule object with the specified name, or null if not found.
     */
    default Rule getRule(String name) {
        return get(name, Rule.class);
    }

    /**
     * Retrieves a list of Rule objects that have the specified ruleClass.
     *
     * @param <T>       the ruleClass type
     * @param ruleClass the ruleClass to search for
     * @return a list of Rule objects with the specified ruleClass
     * @throws IllegalArgumentException if ruleClass is null
     */
    default <T> List<Rule> getRule(Class<T> ruleClass) {
        Assert.notNull(ruleClass, "ruleClass cannot be null.");
        return getRules((Rule rule) -> rule.getDefinition().getRuleClass().equals(ruleClass));
    }

    /**
     * Retrieves a list of Rule objects from the RuleRegistry that match the given filter.
     *
     * @param filter the filter to be used for matching rules
     * @return a list of Rule objects that match the given filter
     * @throws IllegalArgumentException if filter is null
     */
    default List<Rule> getRules(Predicate<Rule> filter) {
        Assert.notNull(filter, "filter cannot be null.");

        List<Rule> rules = getRules();
        if (rules == null || rules.isEmpty()) return rules;

        List<Rule> result = rules.stream()
                .filter(filter)
                .toList();
        return Collections.unmodifiableList(result);
    }

    /**
     * Retrieves a list of Rule objects from the RuleRegistry that belong to the specified package.
     *
     * @param packageName the name of the package to get rules from
     * @return a list of Rule objects that belong to the specified package
     * @throws IllegalArgumentException if packageName is empty or null
     */
    default List<Rule> getRulesInPackage(String packageName) {
        Assert.hasText(packageName, "packageName cannot be empty/null.");
        return getRules(r -> r.getTarget() != null && packageName.equals(r.getTarget().getClass().getPackage().getName()));
    }

    /**
     * Retrieves a RuleSet from the RuleRegistry using its name.
     *
     * @param name the name of the RuleSet being retrieved
     * @return the RuleSet with the specified name
     * @throws IllegalArgumentException if the name is empty or null
     */
    default RuleSet getRuleSet(String name) {
        Assert.hasText(name, "name cannot be empty/null.");
        return get(name, RuleSet.class);
    }
}
