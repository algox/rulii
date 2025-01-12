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
package org.rulii.test.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.ParameterResolver;
import org.rulii.context.RuleContext;
import org.rulii.rule.Rule;

import java.util.ArrayList;
import java.util.List;

import static org.rulii.model.condition.Conditions.condition;

/**
 * Tests for Condition/Action execution.
 *
 * @author Max Arulananthan
 */
public class ExecutorTest {

    public ExecutorTest() {
        super();
    }

    @Test
    public void test1() {
        List<Integer> values = new ArrayList<>();

        Rule rule = Rule.builder()
                .name("rule1")
                .given(condition((String x, Integer y, List<String> a) -> y > 10))
                .build();

        boolean result = rule.getCondition().isTrue(x -> "hello world", y -> 20, a -> values);
        Assertions.assertTrue(result);
    }

    @Test
    public void test2() {
        Rule rule = Rule.builder()
                .name("rule1")
                .given(condition((String x, Integer y) -> y > 10))
                .build();
        boolean result = rule.getCondition().isTrue(x -> "hello world", y -> 20);
        Assertions.assertTrue(result);
    }

    @Test
    public void test3() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class);
        bindings.bind("y", int.class, 123);
        bindings.bind("z", String.class, "Hello");

        Rule rule = Rule.builder()
                .name("rule1")
                .given(condition((String x, Integer y) -> y > 10))
                .build();

        RuleContext context = RuleContext.builder()
                .with(bindings)
                .matchUsing(BindingMatchingStrategy.builder().matchByName())
                .build();

        rule.run(context);
    }

    @Test
    public void test4() {
        Rule rule = Rule.builder().with(TestRule1.class)
                .name("rule1")
                .build();

        TestRule1 x = rule.getTarget();
        Assertions.assertNotNull(x);
    }

    @Test
    public void test5() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", Integer.class, 125);

        Rule rule = Rule.builder().with(TestRule5.class)
                .name("rule1")
                .build();

        rule.run(RuleContext.builder()
                .with(bindings)
                .paramResolver(ParameterResolver.builder().build())
                .build());
        Assertions.assertEquals(0, (int) bindings.getValue("x", Integer.class));
    }
}
