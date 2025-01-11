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
package org.rulii.test.action;

import org.junit.Assert;
import org.junit.Test;
import org.rulii.annotation.Condition;
import org.rulii.annotation.Function;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.action.TriAction;
import org.rulii.util.TypeReference;

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
                })
                .name("action0")
                .build();

        Assert.assertEquals("action0", action.getDefinition().getName());
        Assert.assertEquals(0, action.getDefinition().getParameterDefinitions().size());
    }

    @Test
    public void test1Arg() {
        Action action = Action.builder()
                .with((String x) -> {
                })
                .description("Action with one arg")
                .build();

        Assert.assertEquals("Action with one arg", action.getDefinition().getDescription());
        Assert.assertEquals(1, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("x", action.getDefinition().getParameterDefinitions().get(0).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(0).getType(), String.class);
    }

    @Test
    public void test2Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value) -> {
                })
                .build();

        Assert.assertEquals(2, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("value", action.getDefinition().getParameterDefinitions().get(1).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(1).getType(), BigDecimal.class);
    }

    @Test
    public void test3Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c) -> {
                })
                .build();

        Assert.assertEquals(3, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("c", action.getDefinition().getParameterDefinitions().get(2).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(2).getType(), Integer.class);
    }

    @Test
    public void test4Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c, Float d) -> {
                })
                .param("d")
                    //.optional(true)
                    .build()
                .build();

        Assert.assertEquals(4, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("d", action.getDefinition().getParameterDefinitions().get(3).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(3).getType(), Float.class);
    }

    @Test
    public void test5Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c, Float d, Boolean flag) -> {
                })
                .param("d")
                    .build()
                .param("flag")
                    .defaultValueText("yes")
                    .build()
                .build();

        Assert.assertEquals(5, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("flag", action.getDefinition().getParameterDefinitions().get(4).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(4).getType(), Boolean.class);
        Assert.assertEquals("yes", action.getDefinition().getParameterDefinitions().get(4).getDefaultValueText());
    }

    @Test
    public void test6Args() {
        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c, Float d,
                       Boolean flag, Binding<String> bindingValue) -> {
                    bindingValue.setValue("Hello world!");
                })
                .build();

        Assert.assertEquals(6, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("bindingValue", action.getDefinition().getParameterDefinitions().get(5).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(5).getType(), Binding.class);
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

        Assert.assertEquals(7, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("listArg", action.getDefinition().getParameterDefinitions().get(6).getName());
        // Lambda does not store generic info
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(6).getType(), List.class);
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

        Assert.assertEquals(8, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("mapArg", action.getDefinition().getParameterDefinitions().get(7).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(7)
                .getType(), new TypeReference<Map<String, Object>>() {
        }.getType());
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

        Assert.assertEquals(9, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("someList", action.getDefinition().getParameterDefinitions().get(8).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(8)
                .getType(), new TypeReference<List<String>>() {
        }.getType());
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

        Assert.assertEquals(10, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("tenthArg", action.getDefinition().getParameterDefinitions().get(9).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(9).getType(), String.class);
    }

    @Test(expected = UnrulyException.class)
    public void testInvalidParameterIndex() {
        Action.builder()
                .with((String x, BigDecimal value, Integer c) -> {
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
        Assert.assertEquals(3, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("map", action.getDefinition().getParameterDefinitions().get(2).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(2)
                .getType(), new TypeReference<Map<String, Integer>>() {
        }.getType());
        action.run(a -> "aa", b -> 12, map ->  new HashMap<>());
    }

    public String c = "c";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testOtherArgs() {
        String a = "12 ";
        String b = "b";

        Action action = Action.builder()
                .with((String x, BigDecimal value, Integer c) -> {
                    a.trim();
                    b.length();
                })
                .build();

        Assert.assertEquals(3, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("c", action.getDefinition().getParameterDefinitions().get(2).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(2).getType(), Integer.class);
    }

    public static void testActionMethod1(Integer a, List<Integer> values, BigDecimal c) {}

    @Test
    public void testMethodReference1() {
        Action action = Action.builder()
                .with(ActionBuilderTest::testActionMethod1).build();

        Assert.assertEquals(3, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("a", action.getDefinition().getParameterDefinitions().get(0).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(0).getType(), Integer.class);
        Assert.assertEquals("values", action.getDefinition().getParameterDefinitions().get(1).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(1).getType(), new TypeReference<List<Integer>>() {
        }.getType());
        Assert.assertEquals("c", action.getDefinition().getParameterDefinitions().get(2).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(2).getType(), BigDecimal.class);
    }

    public void testActionMethod2(List<Integer> values, BigDecimal c) {}

    @Test
    public void testMethodReference2() {
        Action action = Action.builder()
                .with(this::testActionMethod2).build();

        Assert.assertEquals(2, action.getDefinition().getParameterDefinitions().size());
        Assert.assertEquals("values", action.getDefinition().getParameterDefinitions().get(0).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(0).getType(), new TypeReference<List<Integer>>() {
        }.getType());
        Assert.assertEquals("c", action.getDefinition().getParameterDefinitions().get(1).getName());
        Assert.assertEquals(action.getDefinition().getParameterDefinitions().get(1).getType(), BigDecimal.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testLoadFromClass() {
        List<org.rulii.model.condition.Condition> conditions = org.rulii.model.condition.Condition.builder().build(TestClass.class);
        List<Action> actions = Action.builder().build(new TestClass());
        org.rulii.model.function.Function[] functions = org.rulii.model.function.Function.builder().build(TestClass.class);
        Assert.assertTrue(conditions.get(0).run(x -> 25));
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", 25);
        actions.get(0).run(bindings);
        Assert.assertEquals(0, (int) bindings.getValue("x", Integer.class));
        Assert.assertEquals(50, functions[0].run(x -> 25));
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
