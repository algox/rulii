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
package org.rulii.test.registry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.model.Runnable;
import org.rulii.model.function.Function;
import org.rulii.registry.DefaultRuleRegistry;
import org.rulii.registry.RuleRegistry;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleset.RuleSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Tests {@link RuleRegistry#getNames()} and the default {@link RuleRegistry#getRuleFlows()}.
 *
 * @author Max Arulananthan
 * @since 2.1
 */
public class RuleRegistryNamesTest {

    private static Rule rule(String name) {
        return Rule.builder().name(name).given(condition(() -> true)).build();
    }

    @Test
    public void defaultRegistryListsEveryKeyInOrder() {
        DefaultRuleRegistry registry = new DefaultRuleRegistry();
        Assertions.assertTrue(registry.getNames().isEmpty());

        registry.register(rule("zebraRule"));
        registry.register(RuleSet.builder().with("alphaSet").build());
        registry.register(RuleFlow.builder().name("midFlow").bind("a", 1).build());
        Function<Integer> fn = Function.builder().with(() -> 1).name("plainFunction").build();
        registry.register(fn);

        Set<String> names = registry.getNames();
        Assertions.assertEquals(List.of("alphaSet", "midFlow", "plainFunction", "zebraRule"), new ArrayList<>(names));
        Assertions.assertEquals(registry.getCount(), names.size());
        for (String name : names) {
            Assertions.assertNotNull(registry.get(name), name);
        }
        Assertions.assertThrows(UnsupportedOperationException.class, () -> names.add("x"));

        // A snapshot: later registrations don't show up in an earlier result.
        registry.register(rule("lateRule"));
        Assertions.assertFalse(names.contains("lateRule"));
        Assertions.assertTrue(registry.getNames().contains("lateRule"));
    }

    @Test
    public void defaultGetRuleFlowsDerivesFromNamesAndGet() {
        Map<String, Runnable<?>> store = new LinkedHashMap<>();
        store.put("r", rule("r"));
        store.put("f1", RuleFlow.builder().name("f1").bind("a", 1).build());
        store.put("s", RuleSet.builder().with("s").build());
        store.put("f2", RuleFlow.builder().name("f2").bind("b", 2).build());

        RuleRegistry registry = new RuleRegistry() {
            @Override
            public boolean isNameInUse(String name) {
                return store.containsKey(name);
            }

            @Override
            public int getCount() {
                return store.size();
            }

            @Override
            public List<Rule> getRules() {
                return List.of();
            }

            @Override
            @SuppressWarnings("rawtypes")
            public List<RuleSet> getRuleSets() {
                return List.of();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <R, T extends Runnable<R>> T get(String name) {
                return (T) store.get(name);
            }

            @Override
            public Set<String> getNames() {
                return store.keySet();
            }
        };

        List<RuleFlow<?>> flows = registry.getRuleFlows();
        Assertions.assertEquals(2, flows.size());
        Assertions.assertEquals("f1", flows.get(0).getName());
        Assertions.assertEquals("f2", flows.get(1).getName());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> flows.add(null));
        Assertions.assertSame(store.get("f1"), registry.getRuleFlow("f1"));
    }
}
