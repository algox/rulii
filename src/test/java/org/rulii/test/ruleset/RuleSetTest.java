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
package org.rulii.test.ruleset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.model.condition.Conditions;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;

import java.math.BigDecimal;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;

/**
 * Tests for RuleSets.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class RuleSetTest {

    public RuleSetTest() {
        super();
    }

    @Test
    public void test1() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("y", String.class, "");
        bindings.bind("a", String.class, "");
        bindings.bind("b", String.class, "hello");
        bindings.bind("c", Integer.class, 20);
        bindings.bind("x", BigDecimal.class, new BigDecimal("100.00"));

        Rule rule6 = Rule.builder()
                .name("Rule6")
                .given(Conditions.TRUE())
                .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                .build();

        RuleSet<?> rules = RuleSet.builder()
                .with("RuleSet1", "Test Rule Set")
                .rule(Rule.builder()
                        .name("Rule1")
                            .given(condition((String y) -> y.isEmpty()))
                            .then(action((Binding<Integer> c) -> c.setValue(0)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule2")
                            .given(condition((String a, BigDecimal x) -> x != null))
                            .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule3")
                            .given(condition((String a, String b, Integer c) -> c == 20 && "hello".equals(b)))
                            .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                        .build())
                .rule(rule6)
                .build();

        Rule rule2 = rules.get("Rule2");
        Rule rule3 = rules.get("Rule3");
        rules.run(bindings);

        Assertions.assertNotNull(rule2);
        Assertions.assertNotNull(rule3);
        Assertions.assertEquals(2, (int) bindings.getValue("c", Integer.class));
        Assertions.assertTrue(rule3.getCondition().isTrue(a -> "", b -> "hello", c -> 20));
    }

    @Test
    public void test2() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("y", String.class, "");
        bindings.bind("a", String.class, "");
        bindings.bind("b", String.class, "hello");
        bindings.bind("c", Integer.class, 20);
        bindings.bind("x", BigDecimal.class, new BigDecimal("100.00"));

        RuleSet<?> rules = RuleSet.builder().with("TestRuleSet", "Sample Test RuleSet using a Class")
                .rule(Rule.builder()
                        .name("Rule1")
                            .given(condition((String y) -> y.isEmpty()))
                            .then(action((Binding<Integer> c) -> c.setValue(0)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule2")
                            .given(condition((String a, BigDecimal x) -> x != null))
                            .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule3")
                            .given(condition((String a, String b, Integer c) -> c == 20 && "hello".equals(b)))
                            .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                        .build())
                .rule(Rule.builder()
                        .name("Rule6")
                            .given(Conditions.TRUE())
                            .then(action((Binding<Integer> c) -> c.setValue(c.getValue() + 1)))
                        .build())
                .build();

        Rule rule2 = rules.get("Rule2");
        Rule rule3 = rules.get("Rule3");
        rules.run(bindings);

        Assertions.assertNotNull(rule2);
        Assertions.assertNotNull(rule3);
        Assertions.assertEquals(2, (int) bindings.getValue("c", Integer.class));
        Assertions.assertTrue(rule3.getCondition().isTrue(a -> "", b -> "hello", c -> 20));
    }
}
