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
package org.rulii.test.action;

import org.rulii.annotation.Condition;
import org.rulii.annotation.Function;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.action.TriAction;
import org.rulii.util.TypeReference;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for ActionBuilder.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class ActionBuilderTest {

    public ActionBuilderTest() {
        super();
    }

    @Test
    public void testNoArg() {
        Action action = Action.builder()
                .with(() -> {
                    return;
                })
                .name("action0")
                .build();

        Assert.assertTrue(action.getDefinition().getName().equals("action0"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 0);
    }

    @Test
    public void test1Arg() {
        Action action = Action.builder()
                .with((String x) -> {
                    return;
                })
                .description("Action with one arg")
                .build();

        Assert.assertTrue(action.getDefinition().getDescription().equals("Action with one arg"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 1);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(0).getName().equals("x"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(0).getType().equals(String.class));
    }

    @Test
    public void test2Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value) -> {
                    return;
                })
                .build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 2);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(1).getName().equals("value"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(1).getType().equals(BigDecimal.class));
    }

    @Test
    public void test3Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c) -> {
                    return;
                })
                .build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 3);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(2).getName().equals("c"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(2).getType().equals(Integer.class));
    }

    @Test
    public void test4Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c, Float d) -> {
                    return;
                })
                .param("d")
                    //.optional(true)
                    .build()
                .build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 4);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(3).getName().equals("d"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(3).getType().equals(Float.class));
    }

    @Test
    public void test5Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c, Float d, Boolean flag) -> {
                    return;
                })
                .param("d")
                    .build()
                .param("flag")
                    .defaultValueText("yes")
                    .build()
                .build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 5);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(4).getName().equals("flag"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(4).getType().equals(Boolean.class));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(4).getDefaultValueText().equals("yes"));
    }

    @Test
    public void test6Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c, Float d,
                       Boolean flag, Binding<String> bindingValue) -> {
                    bindingValue.setValue("Hello world!");
                })
                .build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 6);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(5).getName().equals("bindingValue"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(5).getType().equals(Binding.class));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(5).isBindingType());
    }

    @Test
    public void test7Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c, Float d,
                       Boolean flag, Binding<String> bindingValue, List<Integer> listArg) -> {
                    bindingValue.setValue("Hello world!");
                })
                .build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 7);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(6).getName().equals("listArg"));
        // Lambda does not store generic info
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(6).getType().equals(List.class));
    }

    @Test
    public void test8Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c, Float d,
                       Boolean flag, Binding<String> bindingValue, List<Integer> listArg,
                       Map<String, Object> mapArg) -> {
                    mapArg.put("key", "Hello world!");
                })
                .param(7)
                    .type(new TypeReference<Map<String, Object>>(){}.getType())
                    .build()
                .build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 8);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(7).getName().equals("mapArg"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(7)
                .getType().equals(new TypeReference<Map<String, Object>>(){}.getType()));
    }

    @Test
    public void test9Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c, Float d,
                       Boolean flag, Binding<String> bindingValue, List<Integer> listArg,
                       Map<String, Object> mapArg, List<String> someList) -> {
                    mapArg.put("key", "Hello world!");
                })
                .param("someList")
                    .type(new TypeReference<List<String>>(){}.getType())
                    .build()
                .build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 9);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(8).getName().equals("someList"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(8)
                .getType().equals(new TypeReference<List<String>>(){}.getType()));
    }

    @Test
    public void test10Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c, Float d,
                       Boolean flag, Binding<String> bindingValue, List<Integer> listArg,
                       Map<String, Object> mapArg, List<String> someList, String tenthArg) -> {
                    mapArg.put("key", "Hello world!");
                })
                .param("someList")
                    .type(new TypeReference<List<String>>(){}.getType())
                    .build()
                .build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 10);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(9).getName().equals("tenthArg"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(9).getType().equals(String.class));
    }

    @Test(expected = UnrulyException.class)
    public void testInvalidParameterIndex() {
        Action.builder()
                .with((String x, BigDecimal value, Integer c) -> {
                    return;
                })
                .param(3)
                    .type(new TypeReference<List<String>>(){}.getType())
                    .build()
                .build();

    }

    @Test(expected = UnrulyException.class)
    public void testInvalidParameterNameIndex() {
        Action.builder()
                .with((String x, BigDecimal value, Integer c) -> {
                    return;
                })
                .param("y")
                    .type(new TypeReference<List<String>>(){}.getType())
                    .build()
                .build();

    }

    @Test
    public void testInnerClass() {
        Action action = Action.builder()
                .with(new TriAction<String, Integer, Map<String, Integer>>() {
                    @Override
                    public void run(String a, Integer b, Map<String, Integer> map) {

                    }
                })
                .build();
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 3);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(2).getName().equals("map"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(2)
                .getType().equals(new TypeReference<Map<String, Integer>>(){}.getType()));
        action.run(a -> "aa", b -> 12, map ->  new HashMap<>());
    }

    public String c = "c";

    @Test
    public void testOtherArgs() {

        Integer a = 12;
        String b = "b";

        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c) -> {
                    a.intValue();
                    b.length();
                    c.intValue();
                    return;
                })
                .build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 3);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(2).getName().equals("c"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(2).getType().equals(Integer.class));
    }

    public static void testActionMethod1(Integer a, List<Integer> values, BigDecimal c) {}

    @Test
    public void testMethodReference1() {
        Action action = Action.builder()
                .with(ActionBuilderTest::testActionMethod1).build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 3);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(0).getName().equals("a"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(0).getType().equals(Integer.class));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(1).getName().equals("values"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(1).getType().equals(new TypeReference<List<Integer>>(){}.getType()));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(2).getName().equals("c"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(2).getType().equals(BigDecimal.class));
    }

    public void testActionMethod2(List<Integer> values, BigDecimal c) {}

    @Test
    public void testMethodReference2() {
        Action action = Action.builder()
                .with(this::testActionMethod2).build();

        Assert.assertTrue(action.getDefinition().getParameterDefinitions().size() == 2);
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(0).getName().equals("values"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(0).getType().equals(new TypeReference<List<Integer>>(){}.getType()));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(1).getName().equals("c"));
        Assert.assertTrue(action.getDefinition().getParameterDefinitions().get(1).getType().equals(BigDecimal.class));
    }

    @Test
    public void testLoadFromClass() {
        List<org.rulii.model.condition.Condition> conditions = org.rulii.model.condition.Condition.builder().build(TestClass.class);
        List<Action> actions = Action.builder().build(new TestClass());
        org.rulii.model.function.Function[] functions = org.rulii.model.function.Function.builder().build(TestClass.class);
        Assert.assertTrue(conditions.get(0).run(x -> 25));
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", 25);
        actions.get(0).run(bindings);
        Assert.assertTrue(bindings.getValue("x", Integer.class) == 0);
        Assert.assertTrue(functions[0].run(x -> 25).equals(50));
    }

    public static class TestClass {

        public TestClass() {
            super();
        }

        @Condition
        public static boolean given(Integer x) {
            return x >= 25;
        }

        @org.rulii.annotation.Action
        public void action(Binding<Integer> x) {
            x.setValue(0);
        }

        @Function
        public Integer function(Integer x) {
            return x * 2;
        }
    }
}
