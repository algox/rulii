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
import org.rulii.context.RuleContext;
import org.rulii.model.Runnable;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.registry.RuleRegistry;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleDefinition;
import org.rulii.rule.RuleResult;
import org.rulii.ruleset.RuleSet;

import java.util.Collections;
import java.util.List;

/**
 * Tests for RuleRegistry default methods.
 *
 * @author Max Arulananthan
 */
public class RuleRegistryTest {

    public RuleRegistryTest() {
        super();
    }

    private static Rule ruleWithTarget(Object target) {
        return new Rule() {
            @Override
            public RuleResult run(RuleContext ruleContext) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Condition getPreCondition() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Condition getCondition() {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<Action> getActions() {
                throw new UnsupportedOperationException();
            }

            @Override
            public RuleDefinition getDefinition() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Action getOtherwiseAction() {
                throw new UnsupportedOperationException();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <R> R getTarget() {
                return (R) target;
            }
        };
    }

    private static RuleRegistry registryOf(List<Rule> rules) {
        return new RuleRegistry() {
            @Override
            public boolean isNameInUse(String name) {
                return false;
            }

            @Override
            public int getCount() {
                return rules.size();
            }

            @Override
            public List<Rule> getRules() {
                return rules;
            }

            @Override
            @SuppressWarnings("rawtypes")
            public List<RuleSet> getRuleSets() {
                return Collections.emptyList();
            }

            @Override
            public <R, T extends Runnable<R>> T get(String name) {
                return null;
            }
        };
    }

    @Test
    public void testGetRulesInPackage_targetClassWithNullPackage_isSkippedNotNPE() {
        // Array classes have no java.lang.Package - Class.getPackage() returns null for them.
        Rule arrayTargetRule = ruleWithTarget(new int[0]);
        Rule normalTargetRule = ruleWithTarget(this);
        RuleRegistry registry = registryOf(List.of(arrayTargetRule, normalTargetRule));

        List<Rule> matches = Assertions.assertDoesNotThrow(
                () -> registry.getRulesInPackage(getClass().getPackage().getName()));

        Assertions.assertEquals(1, matches.size());
        Assertions.assertSame(normalTargetRule, matches.get(0));
    }

    // =========================================================================
    // Builder + typed lookups against a real registry
    // =========================================================================

    private static RuleRegistry buildPopulatedRegistry() {
        Rule lambdaRule = Rule.builder().name("lambdaRule")
                .given(org.rulii.model.condition.Conditions.condition(() -> true)).build();
        Rule classRule = Rule.builder().build(ClassRuleA.class);
        RuleSet<?> ruleSet = RuleSet.builder().with("aRuleSet").rule(lambdaRule).build();
        RuleFlow<?> ruleFlow = RuleFlow.builder().name("aRuleFlow").bind(x -> 1).build();

        return RuleRegistry.builder()
                .register(lambdaRule)
                .register(classRule)
                .register(ruleSet)
                .register(ruleFlow)
                .build();
    }

    @Test
    public void testBuilder_registersAndBuilds() {
        RuleRegistry registry = buildPopulatedRegistry();
        Assertions.assertEquals(4, registry.getCount());
        Assertions.assertTrue(registry.isNameInUse("lambdaRule"));
    }

    @Test
    public void testGetWithType() {
        RuleRegistry registry = buildPopulatedRegistry();

        Assertions.assertNotNull(registry.get("lambdaRule", Rule.class));
        // Registered under that name, but not of the requested type.
        Assertions.assertNull(registry.get("lambdaRule", RuleSet.class));
        // Not registered at all.
        Assertions.assertNull(registry.get("noSuchName", Rule.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.get("lambdaRule", null));
    }

    @Test
    public void testGetRuleByName() {
        RuleRegistry registry = buildPopulatedRegistry();
        Rule rule = registry.getRule("lambdaRule");
        Assertions.assertNotNull(rule);
        Assertions.assertEquals("lambdaRule", rule.getName());
        Assertions.assertNull(registry.getRule("noSuchRule"));
    }

    @Test
    public void testGetRuleSetByName() {
        RuleRegistry registry = buildPopulatedRegistry();
        Assertions.assertNotNull(registry.getRuleSet("aRuleSet"));
        // A Rule name is not a RuleSet.
        Assertions.assertNull(registry.getRuleSet("lambdaRule"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.getRuleSet(" "));
    }

    @Test
    public void testGetRuleFlowByName() {
        RuleRegistry registry = buildPopulatedRegistry();
        Assertions.assertNotNull(registry.getRuleFlow("aRuleFlow"));
        Assertions.assertNull(registry.getRuleFlow("lambdaRule"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.getRuleFlow(""));
    }

    @Test
    public void testGetRuleFlows_defaultImplementation_isEmpty() {
        // The interface default (used by registries that don't override it) returns an empty list.
        Assertions.assertTrue(registryOf(List.of()).getRuleFlows().isEmpty());
    }

    @Test
    public void testGetRuleByClass_uniqueMatch() {
        RuleRegistry registry = buildPopulatedRegistry();
        Rule rule = registry.getRule(ClassRuleA.class);
        Assertions.assertNotNull(rule);
        Assertions.assertInstanceOf(ClassRuleA.class, rule.getTarget());
    }

    @Test
    public void testGetRuleByClass_noMatch_returnsNull() {
        RuleRegistry registry = buildPopulatedRegistry();
        Assertions.assertNull(registry.getRule(String.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.getRule((Class<?>) null));
    }

    @Test
    public void testGetRuleByClass_multipleMatches_throwsUnrulyException() {
        // Two distinct rules sharing the same target class - lookup by class cannot be unique.
        Rule rule1 = ruleWithTarget(new ClassRuleA());
        Rule rule2 = ruleWithTarget(new ClassRuleA());
        RuleRegistry registry = registryOf(List.of(rule1, rule2));

        Assertions.assertThrows(UnrulyException.class, () -> registry.getRule(ClassRuleA.class));
    }

    @Test
    public void testGetRulesByClass_exactMatchOnly() {
        RuleRegistry registry = buildPopulatedRegistry();
        // Targets are subtypes of Object, but matching is by exact class - no assignability.
        Assertions.assertTrue(registry.getRules(Object.class).isEmpty());
        Assertions.assertEquals(1, registry.getRules(ClassRuleA.class).size());
    }

    @Test
    public void testGetRulesByPredicate() {
        RuleRegistry registry = buildPopulatedRegistry();
        List<Rule> all = registry.getRules(r -> true);
        Assertions.assertEquals(2, all.size());
        Assertions.assertTrue(registry.getRules(r -> false).isEmpty());
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> registry.getRules((java.util.function.Predicate<Rule>) null));
    }

    @Test
    public void testGetRulesByPredicate_emptyRegistry_shortCircuits() {
        List<Rule> empty = List.of();
        // The default method returns the empty list itself without streaming.
        Assertions.assertSame(empty, registryOf(empty).getRules(r -> true));
    }

    @Test
    public void testGetRulesInPackage_blankPackageName_throws() {
        RuleRegistry registry = buildPopulatedRegistry();
        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.getRulesInPackage("  "));
    }

    @org.rulii.annotation.Rule
    public static class ClassRuleA {

        public ClassRuleA() {
            super();
        }

        @org.rulii.annotation.Given
        public boolean when() {
            return true;
        }
    }
}
