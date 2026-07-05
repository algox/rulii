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
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.registry.RuleRegistry;
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
}
