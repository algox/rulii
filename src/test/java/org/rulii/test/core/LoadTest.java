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
import org.rulii.model.MethodDefinition;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.action.Actions;
import org.rulii.model.condition.Condition;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleDefinition;
import org.rulii.util.TypeReference;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * Tests for loading definitions.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class LoadTest {

    public LoadTest() {
        super();
    }

    @Test
    public void loadTest1() {
        RuleDefinition ruleDef = Rule.builder().with(TestRule1.class).build().getDefinition();
        Assertions.assertEquals("TestRule", ruleDef.getName());
        Assertions.assertEquals("Test Description 1", ruleDef.getDescription());
    }

    @Test
    public void loadTest2() throws NoSuchMethodException {
        RuleDefinition def = Rule.builder().with(TestRule1.class).build().getDefinition();
        Method m = TestRule1.class.getDeclaredMethod("when", int.class, Date.class, List.class);
        Assertions.assertEquals("TestRule", def.getName());
        Assertions.assertNotNull(def.getConditionDefinition());
        Assertions.assertEquals(def.getConditionDefinition().getMethod(), m);
    }

    @Test
    public void loadTest3() {
        RuleDefinition def = Rule.builder().with(TestRule1.class).build().getDefinition();
        Assertions.assertEquals(3, def.getConditionDefinition().getParameterDefinitions().size());
        Assertions.assertEquals(0, def.getConditionDefinition().getParameterDefinitions().get(0).getIndex());
        Assertions.assertEquals("id", def.getConditionDefinition().getParameterDefinitions().get(0).getName());
        Assertions.assertEquals(def.getConditionDefinition().getParameterDefinitions().get(0).getType(), int.class);
        Assertions.assertEquals(0, def.getConditionDefinition().getParameterDefinitions().get(0).getAnnotations().size());
        Assertions.assertEquals(1, def.getConditionDefinition().getParameterDefinitions().get(1).getIndex());
        Assertions.assertEquals("birthDate", def.getConditionDefinition().getParameterDefinitions().get(1).getName());
        Assertions.assertEquals(def.getConditionDefinition().getParameterDefinitions().get(1).getType(), Date.class);
        Assertions.assertEquals(2, def.getConditionDefinition().getParameterDefinitions().get(2).getIndex());
        Assertions.assertEquals("values", def.getConditionDefinition().getParameterDefinitions().get(2).getName());
        Assertions.assertEquals(def.getConditionDefinition().getParameterDefinitions().get(2).getType(), new TypeReference<List<String>>() {
        }.getType());
        Assertions.assertEquals(0, def.getConditionDefinition().getParameterDefinitions().get(2).getAnnotations().size());
    }

    @Test
    public void loadTest4() {
        Rule.builder().with(TestRule2.class).build();
    }

    @Test
    public void loadTest5() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            Rule.builder().with(TestRule3.class).build();
        });
    }

    @Test()
    public void loadTest6() {
        List<MethodDefinition> def = Rule.builder().with(TestRule4.class).build().getDefinition().getThenActionDefinitions();
        Assertions.assertEquals(1, def.size());
        Assertions.assertEquals("calculate", def.get(0).getDescription());
        Assertions.assertEquals("id", def.get(0).getParameterDefinitions().get(0).getName());
        Assertions.assertEquals(def.get(0).getParameterDefinitions().get(0).getType(), int.class);
        Assertions.assertEquals(0, def.get(0).getParameterDefinitions().get(0).getAnnotations().size());
        Assertions.assertEquals("birthDate", def.get(0).getParameterDefinitions().get(1).getName());
        Assertions.assertEquals(def.get(0).getParameterDefinitions().get(1).getType(), Date.class);
        Assertions.assertEquals(0, def.get(0).getParameterDefinitions().get(1).getAnnotations().size());
        Assertions.assertEquals("values", def.get(0).getParameterDefinitions().get(2).getName());
        Assertions.assertEquals(def.get(0).getParameterDefinitions().get(2).getType(), new TypeReference<List<Integer>>() {
        }.getType());
        Assertions.assertEquals(0, def.get(0).getParameterDefinitions().get(2).getAnnotations().size());
    }

    @Test
    public void loadTest7() {
        Rule rule = Rule.builder()
                .name("TestRule2")
                .given(Condition.builder().with((Integer i, String text) -> i > 100 && text != null).build())
                .description("Some rule for testing")
                .then(Actions.EMPTY_ACTION())
                .build();
        RuleDefinition def = rule.getDefinition();

        Assertions.assertEquals(2, def.getConditionDefinition().getParameterDefinitions().size());

        Assertions.assertEquals(0, def.getConditionDefinition().getParameterDefinitions().get(0).getIndex());
        Assertions.assertEquals("i", def.getConditionDefinition().getParameterDefinitions().get(0).getName());
        Assertions.assertEquals(def.getConditionDefinition().getParameterDefinitions().get(0).getType(), Integer.class);
        Assertions.assertEquals(0, def.getConditionDefinition().getParameterDefinitions().get(0).getAnnotations().size());

        Assertions.assertEquals(1, def.getConditionDefinition().getParameterDefinitions().get(1).getIndex());
        Assertions.assertEquals("text", def.getConditionDefinition().getParameterDefinitions().get(1).getName());
        Assertions.assertEquals(def.getConditionDefinition().getParameterDefinitions().get(1).getType(), String.class);
    }

    @Test
    public void loadTest8() {
        Rule.builder()
                .name("rule1")
                .given(Condition.builder().with((Integer a, Date date, String x) -> a != null).build())
                .build();

        Rule.builder()
                .name("rule2")
                .given(Condition.builder().with((Integer a, Date date, String x, String y) -> a != null).build())
                .then(Action.builder().with((Integer y, String z) -> {}).build())
                .then(Action.builder().with((Integer y, String z, Date date) -> {}).build())
                .build();
    }

    @Test
    public void loadTest9() {
        Rule rule = Rule.builder()
                .name("TestRule2")
                .given(Condition.builder().with((Integer i, String text) -> i > 100 && text != null).build())
                .preCondition(Condition.builder().with((Integer x) -> x > 10).build())
                .description("Some rule for testing")
                .then(Actions.EMPTY_ACTION())
                .build();
        RuleDefinition def = rule.getDefinition();

        Assertions.assertNotNull(def.getPreConditionDefinition());
        Assertions.assertEquals("x", def.getPreConditionDefinition().getParameterDefinitions().get(0).getName());
        Assertions.assertEquals(def.getPreConditionDefinition().getParameterDefinitions().get(0).getType(), Integer.class);
    }

    @Test
    public void loadTest10() {
        Rule rule = Rule.builder()
                .with(TestRule5.class)
                .build();
        RuleDefinition def = rule.getDefinition();

        Assertions.assertNotNull(def.getPreConditionDefinition());
        Assertions.assertEquals("x", def.getPreConditionDefinition().getParameterDefinitions().get(0).getName());
        Assertions.assertEquals(def.getPreConditionDefinition().getParameterDefinitions().get(0).getType(), int.class);
    }

    @Test
    public void loadTest11() {
        Rule rule = Rule.builder()
                .with(TestRule4.class)
                .build();
        RuleDefinition def = rule.getDefinition();

        Assertions.assertNull(def.getPreConditionDefinition());
    }

    @Test
    public void loadTest12() {
        Rule rule = Rule.builder()
                .name("TestRule2", "Some rule for testing")
                .given(Condition.builder().with((Integer i, String text) -> i > 100 && text != null).build())
                .then(Actions.EMPTY_ACTION())
                .build();
        RuleDefinition def = rule.getDefinition();

        Assertions.assertNull(def.getPreConditionDefinition());
    }
}
