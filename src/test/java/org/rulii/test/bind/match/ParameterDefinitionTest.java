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
package org.rulii.test.bind.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.annotation.Description;
import org.rulii.annotation.Param;
import org.rulii.bind.Binding;
import org.rulii.bind.match.MatchByNameAndTypeMatchingStrategy;
import org.rulii.bind.match.MatchByNameMatchingStrategy;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.model.Definable;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterDefinition;
import org.rulii.model.SourceDefinition;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.model.function.TriFunction;
import org.rulii.util.TypeReference;
import org.rulii.util.reflect.LambdaUtils;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ParameterDefinition tests.
 *
 * @author Max Arulananthan
 */
public class ParameterDefinitionTest {

    public ParameterDefinitionTest() {
        super();
    }

    @Test
    public void testMethodDefinition1() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod0", String.class, List.class, Binding.class, Map.class);
        MethodDefinition definition = MethodDefinition.load(m, true, SourceDefinition.build());

        Assertions.assertEquals(definition.getName(), "testMethod0");
        Assertions.assertEquals(definition.getDescription(), "Test Method 0");
        Assertions.assertTrue(definition.containsGenericInfo());
        Assertions.assertEquals(definition.getParameterCount(), 4);

        Assertions.assertNotNull(definition.getParameterDefinition("a"));
        Assertions.assertNotNull(definition.getParameterDefinition("b"));
        Assertions.assertNotNull(definition.getParameterDefinition("c"));
        Assertions.assertNotNull(definition.getParameterDefinition("d"));

        Assertions.assertEquals(definition.getParameterDefinition("a"), definition.getParameterDefinition(0));
        Assertions.assertEquals(definition.getParameterDefinition("b"), definition.getParameterDefinition(1));
        Assertions.assertEquals(definition.getParameterDefinition("c"), definition.getParameterDefinition(2));
        Assertions.assertEquals(definition.getParameterDefinition("d"), definition.getParameterDefinition(3));
    }

    @Test
    public void testMethodDefinition2() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod6", BigDecimal.class, Binding.class, Optional.class);
        MethodDefinition definition = MethodDefinition.load(m, true, SourceDefinition.build());

        Assertions.assertEquals(definition.getName(), "testMethod6");
        Assertions.assertTrue(definition.containsGenericInfo());
        Assertions.assertEquals(definition.getParameterCount(), 3);

        Assertions.assertNotNull(definition.getParameterDefinition("salary1"));
        Assertions.assertNotNull(definition.getParameterDefinition("salary2"));
        Assertions.assertNotNull(definition.getParameterDefinition("firstName"));

        Assertions.assertEquals(definition.getParameterDefinition("salary1"), definition.getParameterDefinition(0));
        Assertions.assertEquals(definition.getParameterDefinition("salary2"), definition.getParameterDefinition(1));
        Assertions.assertEquals(definition.getParameterDefinition("firstName"), definition.getParameterDefinition(2));
    }

    @Test
    public void testMethodDefinition13() throws NoSuchMethodException {
        Condition condition = Condition.builder().with((Integer a, Binding<List<String>> b, Integer x) -> a > 10)
                .build();
        MethodDefinition definition = condition.getDefinition();

        Assertions.assertFalse(definition.containsGenericInfo());
        Assertions.assertEquals(definition.getParameterCount(), 3);

        Assertions.assertNotNull(definition.getParameterDefinition("a"));
        Assertions.assertNotNull(definition.getParameterDefinition("b"));
        Assertions.assertNotNull(definition.getParameterDefinition("x"));

        Assertions.assertEquals(definition.getParameterDefinition("a"), definition.getParameterDefinition(0));
        Assertions.assertEquals(definition.getParameterDefinition("b"), definition.getParameterDefinition(1));
        Assertions.assertEquals(definition.getParameterDefinition("x"), definition.getParameterDefinition(2));
    }

    @Test
    public void testParameterTypes() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod0", String.class, List.class, Binding.class, Map.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());

        Assertions.assertEquals(parameters.get(0).getType(), String.class);
        Assertions.assertTrue(parameters.get(0).containsGenericInfo());
        Assertions.assertEquals(parameters.get(1).getType(), new TypeReference<List<Integer>>() {
        }.getType());
        Assertions.assertTrue(parameters.get(1).containsGenericInfo());
        Assertions.assertEquals(parameters.get(2).getType(), new TypeReference<Binding<List<Integer>>>() {
        }.getType());
        Assertions.assertTrue(parameters.get(2).isBindingType());
        Assertions.assertTrue(parameters.get(2).containsGenericInfo());
        Assertions.assertEquals(parameters.get(3).getType(), new TypeReference<Map<?, Long>>() {
        }.getType());
        Assertions.assertTrue(parameters.get(3).containsGenericInfo());
    }

    @Test
    public void testBindableParameter1() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod1", int.class, Map.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assertions.assertTrue(!parameters.get(0).isBindingType() && parameters.get(0).getType().equals(int.class));
        Assertions.assertFalse(parameters.get(1).isBindingType());
    }

    @Test
    public void testBindableParameter2() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod2", Binding.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assertions.assertTrue(parameters.get(0).isBindingType() && parameters.get(0).getUnderlyingType().equals(Integer.class));
    }

    @Test
    public void testBindableParameter3() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod3", String.class, Integer.class, Binding.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assertions.assertTrue(!parameters.get(0).isBindingType() && parameters.get(0).getType().equals(String.class));
        Assertions.assertTrue(!parameters.get(1).isBindingType() && parameters.get(1).getType().equals(Integer.class));
        Assertions.assertTrue(parameters.get(2).isBindingType() && parameters.get(2).getUnderlyingType().equals(
                new TypeReference<List<Integer>>() {}.getType()));
    }

    @Test
    public void testBindableParameter4() {
        TriFunction<Boolean, Integer, Binding<List<String>>, Optional<Integer>> condition = ((a, b, x) -> a > 10);
        SerializedLambda lambda = LambdaUtils.getSerializedLambda(condition);
        Class<?> c = LambdaUtils.getImplementationClass(lambda);
        Method m = LambdaUtils.getImplementationMethod(lambda, c);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, false, SourceDefinition.build());
        Assertions.assertTrue(!parameters.get(0).isBindingType() && parameters.get(0).getType().equals(Integer.class));
        Assertions.assertTrue(parameters.get(1).isBindingType() && parameters.get(1).getUnderlyingType().equals(Object.class));
        Assertions.assertFalse(parameters.get(0).containsGenericInfo());
        Assertions.assertFalse(parameters.get(1).containsGenericInfo());
    }

    @Test
    public void testBindableParameter5() {
        Condition condition = Condition.builder().with((Integer a, Binding<List<String>> b, Integer x) -> a > 10)
                .param(1)
                    .type(new TypeReference<Binding<List<Integer>>>() {}.getType())
                    .build()
                .build();
        List<ParameterDefinition> parameters = condition.getDefinition().getParameterDefinitions();
        Assertions.assertTrue(!parameters.get(0).isBindingType() && parameters.get(0).getType().equals(Integer.class));
        Assertions.assertTrue(parameters.get(1).isBindingType() && parameters.get(1).getUnderlyingType().equals(new TypeReference<List<Integer>>() {}.getType()));
        Assertions.assertFalse(parameters.get(0).containsGenericInfo());
        Assertions.assertTrue(parameters.get(1).containsGenericInfo());
    }

    @Test
    public void testMethodParamDefinition1() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod4", Integer.class, Binding.class, Optional.class, String.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assertions.assertEquals("arg1", parameters.get(0).getName());
        Assertions.assertEquals(parameters.get(0).getType(), Integer.class);
        Assertions.assertEquals("123", parameters.get(0).getDefaultValueText());
    }

    @Test
    public void testMethodParamDefinition2() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod4", Integer.class, Binding.class, Optional.class, String.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assertions.assertEquals("arg2", parameters.get(1).getName());
        Assertions.assertEquals(parameters.get(1).getType(), new TypeReference<Binding<Integer>>() {
        }.getType());
        Assertions.assertNull(parameters.get(1).getDefaultValueText());
        Assertions.assertTrue(parameters.get(1).isBindingType());
        Assertions.assertEquals(parameters.get(1).getUnderlyingType(), Integer.class);
    }

    @Test
    public void testMethodParamDefinition3() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod4", Integer.class, Binding.class, Optional.class, String.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assertions.assertEquals("arg3", parameters.get(2).getName());
        Assertions.assertEquals(parameters.get(2).getType(), new TypeReference<Optional<String>>() {
        }.getType());
        Assertions.assertNull(parameters.get(2).getDefaultValueText());
        Assertions.assertFalse(parameters.get(2).isBindingType());
        Assertions.assertTrue(parameters.get(2).isOptionalType());
        Assertions.assertEquals(parameters.get(2).getUnderlyingType(), String.class);
        Assertions.assertNull(parameters.get(2).getMatchUsing());
    }

    @Test
    public void testMethodParamDefinition4() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod4", Integer.class, Binding.class, Optional.class, String.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assertions.assertEquals("arg4", parameters.get(3).getName());
        Assertions.assertEquals(parameters.get(3).getType(), String.class);
        Assertions.assertNull(parameters.get(3).getDefaultValueText());
        Assertions.assertFalse(parameters.get(3).isBindingType());
        Assertions.assertFalse(parameters.get(3).isOptionalType());
        Assertions.assertEquals(parameters.get(3).getMatchUsing(), MatchByNameAndTypeMatchingStrategy.class);
        Assertions.assertEquals(parameters.get(3).getUnderlyingType(), String.class);
    }

    @Test
    public void testMethodParamDefinition5() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod5", BigDecimal.class, BigDecimal.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assertions.assertEquals("salary1", parameters.get(0).getName());
        Assertions.assertNull(parameters.get(0).getDescription());
        Assertions.assertEquals("salary2", parameters.get(1).getName());
        Assertions.assertEquals("salary 2", parameters.get(1).getDescription());
    }

    @Test
    public void testMethodParamDefinition6() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod6", BigDecimal.class, Binding.class, Optional.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assertions.assertEquals("salary1", parameters.get(0).getName());
        Assertions.assertEquals("0.00", parameters.get(0).getDefaultValueText());
        Assertions.assertFalse(parameters.get(0).isMatchSpecified());
        Assertions.assertFalse(parameters.get(0).isBindingType());
        Assertions.assertFalse(parameters.get(0).isOptionalType());

        Assertions.assertNull(parameters.get(1).getDefaultValueText());
        Assertions.assertTrue(parameters.get(1).isMatchSpecified());
        Assertions.assertTrue(parameters.get(1).isBindingType());
        Assertions.assertFalse(parameters.get(1).isOptionalType());

        Assertions.assertEquals("firstName", parameters.get(2).getName());
        Assertions.assertNull(parameters.get(2).getDefaultValueText());
        Assertions.assertFalse(parameters.get(2).isMatchSpecified());
        Assertions.assertFalse(parameters.get(2).isBindingType());
        Assertions.assertTrue(parameters.get(2).isOptionalType());
    }

    @Test
    public void testMethodParamDefinition7() {
        Condition condition = Condition.builder().with((@Param(defaultValue = "123") Integer num,
                Binding<Integer> bind, Optional<String> opt,
                @Param(matchUsing = MatchByNameAndTypeMatchingStrategy.class) String match) -> true).build();

        Assertions.assertTrue(LambdaUtils.isLambda(condition.getTarget()));
        SerializedLambda lambda = LambdaUtils.getSafeSerializedLambda(condition.getTarget());
        Assertions.assertNotNull(lambda);
        Class<?> implClass = LambdaUtils.getImplementationClass(lambda);
        Assertions.assertNotNull(implClass);
        Method m = LambdaUtils.getImplementationMethod(lambda, implClass);
        Assertions.assertNotNull(m);

        List<ParameterDefinition> parameters = ParameterDefinition.load(m, false, SourceDefinition.build());
        Assertions.assertEquals("num", parameters.get(0).getName());
        Assertions.assertEquals("bind", parameters.get(1).getName());
        Assertions.assertEquals("opt", parameters.get(2).getName());
        Assertions.assertEquals("match", parameters.get(3).getName());
        Assertions.assertTrue(parameters.get(1).isBindingType());
        Assertions.assertFalse(parameters.get(0).isOptionalType());
        Assertions.assertTrue(parameters.get(2).isOptionalType());

        Assertions.assertFalse(parameters.get(0).containsGenericInfo());
        Assertions.assertFalse(parameters.get(1).containsGenericInfo());
        Assertions.assertFalse(parameters.get(2).containsGenericInfo());
        Assertions.assertFalse(parameters.get(3).containsGenericInfo());

        Assertions.assertNull(parameters.get(3).getMatchUsing());
    }

    @Test
    public void testMethodParamDefinition8() {
        Condition condition = Condition.builder().with((Integer num, Binding<Integer> bind, Optional<String> opt, String match) -> true)
                .param(0)
                    .matchUsing(MatchByNameMatchingStrategy.class)
                .build()
                .param(1)
                    .type(new TypeReference<Binding<Integer>>(){}.getType())
                .build()
                .param("opt")
                    .type(new TypeReference<Optional<String>>(){})
                    .name("newOpt")
                .build()
                .param(3)
                    .description("arg 4")
                    .defaultValueText("hello")
                .build()
                .build();

        MethodDefinition definition = condition.getDefinition();

        Assertions.assertEquals(definition.getParameterDefinition(0).getMatchUsing(), MatchByNameMatchingStrategy.class);
        Assertions.assertEquals(definition.getParameterDefinition(1).getType(), new TypeReference<Binding<Integer>>(){}.getType());
        Assertions.assertTrue(definition.getParameterDefinition("bind").containsGenericInfo());

        Assertions.assertEquals(definition.getParameterDefinition(2).getType(), new TypeReference<Optional<String>>(){}.getType());
        Assertions.assertTrue(definition.getParameterDefinition("newOpt").containsGenericInfo());

        Assertions.assertEquals(definition.getParameterDefinition(3).getDescription(), "arg 4");
        Assertions.assertEquals(definition.getParameterDefinition("match").getDefaultValueText(), "hello");
    }

    @Test
    public void testMethodParamDefinition9() {
        Action action = Action.builder().with((Integer num, Binding<Integer> bind, Optional<String> opt, String match) -> {})
                .param(0)
                    .name("arg1")
                    .description("desc")
                    .type(BigInteger.class)
                    .defaultValueText("100")
                .build()
                .build();

        MethodDefinition definition = action.getDefinition();
        Assertions.assertEquals(definition.getParameterDefinition(0).getName(), "arg1");
        Assertions.assertEquals(definition.getParameterDefinition(0).getType(), BigInteger.class);
        Assertions.assertEquals(definition.getParameterDefinition(0).getDefaultValueText(), "100");
        Assertions.assertEquals(definition.getParameterDefinition("arg1").getDescription(), "desc");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMethodParamDefinition10() {
        Function<?> function = Function.builder().with((Integer num, Binding<Integer> bind, Optional<String> opt, String match) -> 1)
                .param(0)
                    .name("arg1")
                    .description("desc")
                    .type(BigInteger.class)
                    .defaultValueText("100")
                .build()
                .build();

        MethodDefinition definition = ((Definable<MethodDefinition>) function).getDefinition();
        Assertions.assertEquals(definition.getParameterDefinition(0).getName(), "arg1");
        Assertions.assertEquals(definition.getParameterDefinition("arg1").getDescription(), "desc");
        Assertions.assertEquals(definition.getParameterDefinition(0).getType(), BigInteger.class);
        Assertions.assertEquals(definition.getParameterDefinition(0).getDefaultValueText(), "100");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMethodParamDefinition11() {
        Condition condition = Condition.builder().with((@Description("hello1") Integer num, @Description("hello2") Binding<Integer> bind,
                                                        @Description("hello3") Optional<Integer> opt, @Description("hello4") String match) -> true).build();
        MethodDefinition definition = ((Definable<MethodDefinition>) condition).getDefinition();
        Assertions.assertTrue(LambdaUtils.isLambda(condition.getTarget()));
        SerializedLambda lambda = LambdaUtils.getSafeSerializedLambda(condition.getTarget());
        Assertions.assertNotNull(lambda);
        Class<?> implClass = LambdaUtils.getImplementationClass(lambda);
        Assertions.assertNotNull(implClass);
        Method m = LambdaUtils.getImplementationMethod(lambda, implClass);
        Assertions.assertNotNull(m);
    }

    private static class TestClass {

        @Description("Test Method 0")
        public boolean testMethod0(String a, List<Integer> b, Binding<List<Integer>> c, Map<?, Long> d) {
            return true;
        }

        public void testMethod1(int a, Map<String, Integer> values) {}

        public boolean testMethod2(Binding<Integer> x) {
            return true;
        }

        public boolean testMethod3(String a, Integer b, Binding<List<Integer>> x) {
            return true;
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public void testMethod4(@Param(defaultValue = "123") Integer arg1,
                                Binding<Integer> arg2, Optional<String> arg3,
                                @Param(matchUsing = MatchByNameAndTypeMatchingStrategy.class) String arg4) {}

        public void testMethod5(@Param("salary1") BigDecimal arg1,
                                @Param(name = "salary2") @Description("salary 2") BigDecimal arg2) {}

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public void testMethod6(@Param(value = "salary1", defaultValue = "0.00") BigDecimal arg1,
                                @Param(value = "salary2", matchUsing = MatchByTypeMatchingStrategy.class) Binding<BigDecimal> arg2,
                                Optional<String> firstName) {}
    }
}
