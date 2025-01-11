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

import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.action.Actions;
import org.rulii.model.condition.Condition;
import org.rulii.model.MethodDefinition;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleDefinition;
import org.rulii.util.TypeReference;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * Tests for loading definitions.
 *
 * @author Max Arulananthan
 */
public class LoadTest {

    public LoadTest() {
        super();
    }

    @Test
    public void loadTest1() {
        RuleDefinition ruleDef = Rule.builder().with(TestRule1.class).build().getDefinition();

        Assert.assertTrue("TestRule".equals(ruleDef.getName()));
        Assert.assertTrue("Test Description 1".equals(ruleDef.getDescription()));
    }

    @Test
    public void loadTest2() throws NoSuchMethodException {
        RuleDefinition def = Rule.builder().with(TestRule1.class).build().getDefinition();

        Method m = TestRule1.class.getDeclaredMethod("when", int.class, Date.class, List.class);

        Assert.assertTrue("TestRule".equals(def.getName()));
        Assert.assertTrue(def.getConditionDefinition() != null);
        Assert.assertTrue(def.getConditionDefinition().getMethod().equals(m));
    }

    @Test
    public void loadTest3() {
        RuleDefinition def = Rule.builder().with(TestRule1.class).build().getDefinition();

        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().size() == 3);

        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(0).getIndex() == 0);
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(0).getName().equals("id"));
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(0).getType().equals(int.class));
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(0).getAnnotations().size() == 0);

        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(1).getIndex() == 1);
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(1).getName().equals("birthDate"));
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(1).getType().equals(Date.class));

        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(2).getIndex() == 2);
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(2).getName().equals("values"));
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(2).getType().equals(new TypeReference<List<String>>(){}.getType()));
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(2).getAnnotations().size() == 0);
    }

    @Test
    public void loadTest4() {
        Rule.builder().with(TestRule2.class).build();
    }

    @Test(expected = UnrulyException.class)
    public void loadTest5() {
        Rule.builder().with(TestRule3.class).build();
    }

    @Test()
    public void loadTest6() {
        List<MethodDefinition> def = Rule.builder().with(TestRule4.class).build().getDefinition().getThenActionDefinitions();

        Assert.assertTrue(def.size() == 1);
        Assert.assertTrue(def.get(0).getDescription().equals("calculate"));
        Assert.assertTrue(def.get(0).getParameterDefinitions().get(0).getName().equals("id"));
        Assert.assertTrue(def.get(0).getParameterDefinitions().get(0).getType().equals(int.class));
        Assert.assertTrue(def.get(0).getParameterDefinitions().get(0).getAnnotations().size() == 0);

        Assert.assertTrue(def.get(0).getParameterDefinitions().get(1).getName().equals("birthDate"));
        Assert.assertTrue(def.get(0).getParameterDefinitions().get(1).getType().equals(Date.class));
        Assert.assertTrue(def.get(0).getParameterDefinitions().get(1).getAnnotations().size() == 0);

        Assert.assertTrue(def.get(0).getParameterDefinitions().get(2).getName().equals("values"));
        Assert.assertTrue(def.get(0).getParameterDefinitions().get(2).getType().equals(new TypeReference<List<Integer>>(){}.getType()));
        Assert.assertTrue(def.get(0).getParameterDefinitions().get(2).getAnnotations().size() == 0);
    }

    @Test
    public void loadTest7() {
        Rule rule = Rule.builder()
                .name("TestRule2")
                .given(Condition.builder().with((Integer i, String text) -> i > 100 && text != null).build())
                .description("Some rule for testing")
                .then(Actions.emptyAction())
                .build();
        RuleDefinition def = rule.getDefinition();

        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().size() == 2);

        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(0).getIndex() == 0);
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(0).getName().equals("i"));
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(0).getType().equals(Integer.class));
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(0).getAnnotations().size() == 0);

        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(1).getIndex() == 1);
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(1).getName().equals("text"));
        Assert.assertTrue(def.getConditionDefinition().getParameterDefinitions().get(1).getType().equals(String.class));
    }

    @Test
    public void loadTest8() {
        Rule rule1 = Rule.builder()
                .name("rule1")
                .given(Condition.builder().with((Integer a, Date date, String x) -> a != null).build())
                .build();

        Rule rule2 = Rule.builder()
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
                .pre(Condition.builder().with((Integer x) -> x > 10).build())
                .description("Some rule for testing")
                .then(Actions.emptyAction())
                .build();
        RuleDefinition def = rule.getDefinition();

        Assert.assertTrue(def.getPreConditionDefinition() != null);
        Assert.assertTrue(def.getPreConditionDefinition().getParameterDefinitions().get(0).getName().equals("x"));
        Assert.assertTrue(def.getPreConditionDefinition().getParameterDefinitions().get(0).getType().equals(Integer.class));
    }

    @Test
    public void loadTest10() {
        Rule rule = Rule.builder()
                .with(TestRule5.class)
                .build();
        RuleDefinition def = rule.getDefinition();

        Assert.assertTrue(def.getPreConditionDefinition() != null);
        Assert.assertTrue(def.getPreConditionDefinition().getParameterDefinitions().get(0).getName().equals("x"));
        Assert.assertTrue(def.getPreConditionDefinition().getParameterDefinitions().get(0).getType().equals(int.class));
    }

    @Test
    public void loadTest11() {
        Rule rule = Rule.builder()
                .with(TestRule4.class)
                .build();
        RuleDefinition def = rule.getDefinition();

        Assert.assertTrue(def.getPreConditionDefinition() == null);
    }

    @Test
    public void loadTest12() {
        Rule rule = Rule.builder()
                .name("TestRule2", "Some rule for testing")
                .given(Condition.builder().with((Integer i, String text) -> i > 100 && text != null).build())
                .then(Actions.emptyAction())
                .build();
        RuleDefinition def = rule.getDefinition();

        Assert.assertTrue(def.getPreConditionDefinition() == null);
    }
}
