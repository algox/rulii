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
package org.rulii.test.bind.match;

import org.junit.Assert;
import org.junit.Test;
import org.rulii.annotation.Description;
import org.rulii.annotation.Param;
import org.rulii.bind.Binding;
import org.rulii.bind.match.MatchByNameAndTypeMatchingStrategy;
import org.rulii.bind.match.MatchByNameMatchingStrategy;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.model.function.TriFunction;
import org.rulii.model.Definable;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ParameterDefinition;
import org.rulii.model.SourceDefinition;
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

        Assert.assertEquals(definition.getName(), "testMethod0");
        Assert.assertEquals(definition.getDescription(), "Test Method 0");
        Assert.assertTrue(definition.containsGenericInfo());
        Assert.assertEquals(definition.getParameterCount(), 4);

        Assert.assertNotNull(definition.getParameterDefinition("a"));
        Assert.assertNotNull(definition.getParameterDefinition("b"));
        Assert.assertNotNull(definition.getParameterDefinition("c"));
        Assert.assertNotNull(definition.getParameterDefinition("d"));

        Assert.assertEquals(definition.getParameterDefinition("a"), definition.getParameterDefinition(0));
        Assert.assertEquals(definition.getParameterDefinition("b"), definition.getParameterDefinition(1));
        Assert.assertEquals(definition.getParameterDefinition("c"), definition.getParameterDefinition(2));
        Assert.assertEquals(definition.getParameterDefinition("d"), definition.getParameterDefinition(3));
    }

    @Test
    public void testMethodDefinition2() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod6", BigDecimal.class, Binding.class, Optional.class);
        MethodDefinition definition = MethodDefinition.load(m, true, SourceDefinition.build());

        Assert.assertEquals(definition.getName(), "testMethod6");
        Assert.assertTrue(definition.containsGenericInfo());
        Assert.assertEquals(definition.getParameterCount(), 3);

        Assert.assertNotNull(definition.getParameterDefinition("salary1"));
        Assert.assertNotNull(definition.getParameterDefinition("salary2"));
        Assert.assertNotNull(definition.getParameterDefinition("firstName"));

        Assert.assertEquals(definition.getParameterDefinition("salary1"), definition.getParameterDefinition(0));
        Assert.assertEquals(definition.getParameterDefinition("salary2"), definition.getParameterDefinition(1));
        Assert.assertEquals(definition.getParameterDefinition("firstName"), definition.getParameterDefinition(2));
    }

    @Test
    public void testMethodDefinition13() throws NoSuchMethodException {
        Condition condition = Condition.builder().with((Integer a, Binding<List<String>> b, Integer x) -> a > 10)
                .build();
        MethodDefinition definition = condition.getDefinition();

        Assert.assertFalse(definition.containsGenericInfo());
        Assert.assertEquals(definition.getParameterCount(), 3);

        Assert.assertNotNull(definition.getParameterDefinition("a"));
        Assert.assertNotNull(definition.getParameterDefinition("b"));
        Assert.assertNotNull(definition.getParameterDefinition("x"));

        Assert.assertEquals(definition.getParameterDefinition("a"), definition.getParameterDefinition(0));
        Assert.assertEquals(definition.getParameterDefinition("b"), definition.getParameterDefinition(1));
        Assert.assertEquals(definition.getParameterDefinition("x"), definition.getParameterDefinition(2));
    }

    @Test
    public void testParameterTypes() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod0", String.class, List.class, Binding.class, Map.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());

        Assert.assertTrue(parameters.get(0).getType().equals(String.class));
        Assert.assertTrue(parameters.get(0).containsGenericInfo());
        Assert.assertTrue(parameters.get(1).getType().equals(new TypeReference<List<Integer>>(){}.getType()));
        Assert.assertTrue(parameters.get(1).containsGenericInfo());
        Assert.assertTrue(parameters.get(2).getType().equals(new TypeReference<Binding<List<Integer>>>(){}.getType()));
        Assert.assertTrue(parameters.get(2).isBindingType());
        Assert.assertTrue(parameters.get(2).containsGenericInfo());
        Assert.assertTrue(parameters.get(3).getType().equals(new TypeReference<Map<?, Long>>(){}.getType()));
        Assert.assertTrue(parameters.get(3).containsGenericInfo());
    }

    @Test
    public void testBindableParameter1() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod1", int.class, Map.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assert.assertTrue(!parameters.get(0).isBindingType() && parameters.get(0).getType().equals(int.class));
        Assert.assertTrue(!parameters.get(1).isBindingType());
    }

    @Test
    public void testBindableParameter2() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod2", Binding.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assert.assertTrue(parameters.get(0).isBindingType() && parameters.get(0).getUnderlyingType().equals(Integer.class));
    }

    @Test
    public void testBindableParameter3() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod3", String.class, Integer.class, Binding.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assert.assertTrue(!parameters.get(0).isBindingType() && parameters.get(0).getType().equals(String.class));
        Assert.assertTrue(!parameters.get(1).isBindingType() && parameters.get(1).getType().equals(Integer.class));
        Assert.assertTrue(parameters.get(2).isBindingType() && parameters.get(2).getUnderlyingType().equals(
                new TypeReference<List<Integer>>() {}.getType()));
    }

    @Test
    public void testBindableParameter4() {
        TriFunction<Boolean, Integer, Binding<List<String>>, Optional<Integer>> condition = ((a, b, x) -> a > 10);
        SerializedLambda lambda = LambdaUtils.getSerializedLambda(condition);
        Class c = LambdaUtils.getImplementationClass(lambda);
        Method m = LambdaUtils.getImplementationMethod(lambda, c);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, false, SourceDefinition.build());
        Assert.assertTrue(!parameters.get(0).isBindingType() && parameters.get(0).getType().equals(Integer.class));
        Assert.assertTrue(parameters.get(1).isBindingType() && parameters.get(1).getUnderlyingType().equals(Object.class));
        Assert.assertFalse(parameters.get(0).containsGenericInfo());
        Assert.assertFalse(parameters.get(1).containsGenericInfo());
    }

    @Test
    public void testBindableParameter5() {
        Condition condition = Condition.builder().with((Integer a, Binding<List<String>> b, Integer x) -> a > 10)
                .param(1)
                    .type(new TypeReference<Binding<List<Integer>>>() {}.getType())
                    .build()
                .build();
        List<ParameterDefinition> parameters = condition.getDefinition().getParameterDefinitions();
        Assert.assertTrue(!parameters.get(0).isBindingType() && parameters.get(0).getType().equals(Integer.class));
        Assert.assertTrue(parameters.get(1).isBindingType() && parameters.get(1).getUnderlyingType().equals(new TypeReference<List<Integer>>() {}.getType()));
        Assert.assertFalse(parameters.get(0).containsGenericInfo());
        Assert.assertTrue(parameters.get(1).containsGenericInfo());
    }

    @Test
    public void testMethodParamDefinition1() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod4", Integer.class, Binding.class, Optional.class, String.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assert.assertTrue(parameters.get(0).getName().equals("arg1"));
        Assert.assertTrue(parameters.get(0).getType().equals(Integer.class));
        Assert.assertTrue(parameters.get(0).getDefaultValueText().equals("123"));
    }

    @Test
    public void testMethodParamDefinition2() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod4", Integer.class, Binding.class, Optional.class, String.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assert.assertTrue(parameters.get(1).getName().equals("arg2"));
        Assert.assertTrue(parameters.get(1).getType().equals(new TypeReference<Binding<Integer>>(){}.getType()));
        Assert.assertTrue(parameters.get(1).getDefaultValueText() == null);
        Assert.assertTrue(parameters.get(1).isBindingType());
        Assert.assertTrue(parameters.get(1).getUnderlyingType().equals(Integer.class));
    }

    @Test
    public void testMethodParamDefinition3() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod4", Integer.class, Binding.class, Optional.class, String.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assert.assertTrue(parameters.get(2).getName().equals("arg3"));
        Assert.assertTrue(parameters.get(2).getType().equals(new TypeReference<Optional<String>>(){}.getType()));
        Assert.assertTrue(parameters.get(2).getDefaultValueText() == null);
        Assert.assertTrue(!parameters.get(2).isBindingType());
        Assert.assertTrue(parameters.get(2).isOptionalType());
        Assert.assertTrue(parameters.get(2).getUnderlyingType().equals(String.class));
        Assert.assertTrue(parameters.get(2).getMatchUsing() == null);
    }

    @Test
    public void testMethodParamDefinition4() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod4", Integer.class, Binding.class, Optional.class, String.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assert.assertTrue(parameters.get(3).getName().equals("arg4"));
        Assert.assertTrue(parameters.get(3).getType().equals(String.class));
        Assert.assertTrue(parameters.get(3).getDefaultValueText() == null);
        Assert.assertTrue(!parameters.get(3).isBindingType());
        Assert.assertTrue(!parameters.get(3).isOptionalType());
        Assert.assertTrue(parameters.get(3).getMatchUsing().equals(MatchByNameAndTypeMatchingStrategy.class));
        Assert.assertTrue(parameters.get(3).getUnderlyingType().equals(String.class));
    }

    @Test
    public void testMethodParamDefinition5() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod5", BigDecimal.class, BigDecimal.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assert.assertTrue(parameters.get(0).getName().equals("salary1"));
        Assert.assertTrue(parameters.get(0).getDescription() == null);
        Assert.assertTrue(parameters.get(1).getName().equals("salary2"));
        Assert.assertTrue(parameters.get(1).getDescription().equals("salary 2"));
    }

    @Test
    public void testMethodParamDefinition6() throws NoSuchMethodException {
        Method m = TestClass.class.getDeclaredMethod("testMethod6", BigDecimal.class, Binding.class, Optional.class);
        List<ParameterDefinition> parameters = ParameterDefinition.load(m, true, SourceDefinition.build());
        Assert.assertTrue(parameters.get(0).getName().equals("salary1"));
        Assert.assertTrue(parameters.get(0).getDefaultValueText().equals("0.00"));
        Assert.assertFalse(parameters.get(0).isMatchSpecified());
        Assert.assertFalse(parameters.get(0).isBindingType());
        Assert.assertFalse(parameters.get(0).isOptionalType());

        Assert.assertTrue(parameters.get(1).getDefaultValueText() == null);
        Assert.assertTrue(parameters.get(1).isMatchSpecified());
        Assert.assertTrue(parameters.get(1).isBindingType());
        Assert.assertFalse(parameters.get(1).isOptionalType());

        Assert.assertTrue(parameters.get(2).getName().equals("firstName"));
        Assert.assertTrue(parameters.get(2).getDefaultValueText() == null);
        Assert.assertFalse(parameters.get(2).isMatchSpecified());
        Assert.assertFalse(parameters.get(2).isBindingType());
        Assert.assertTrue(parameters.get(2).isOptionalType());
    }

    @Test
    public void testMethodParamDefinition7() {
        Condition condition = Condition.builder().with((@Param(defaultValue = "123") Integer num,
                Binding<Integer> bind, Optional<String> opt,
                @Param(matchUsing = MatchByNameAndTypeMatchingStrategy.class) String match) -> true).build();

        Assert.assertTrue(LambdaUtils.isLambda(condition.getTarget()));
        SerializedLambda lambda = LambdaUtils.getSafeSerializedLambda(condition.getTarget());
        Assert.assertNotNull(lambda);
        Class<?> implClass = LambdaUtils.getImplementationClass(lambda);
        Assert.assertNotNull(implClass);
        Method m = LambdaUtils.getImplementationMethod(lambda, implClass);
        Assert.assertNotNull(m);

        List<ParameterDefinition> parameters = ParameterDefinition.load(m, false, SourceDefinition.build());
        Assert.assertTrue(parameters.get(0).getName().equals("num"));
        Assert.assertTrue(parameters.get(1).getName().equals("bind"));
        Assert.assertTrue(parameters.get(2).getName().equals("opt"));
        Assert.assertTrue(parameters.get(3).getName().equals("match"));
        Assert.assertTrue(parameters.get(1).isBindingType());
        Assert.assertFalse(parameters.get(0).isOptionalType());
        Assert.assertTrue(parameters.get(2).isOptionalType());

        Assert.assertFalse(parameters.get(0).containsGenericInfo());
        Assert.assertFalse(parameters.get(1).containsGenericInfo());
        Assert.assertFalse(parameters.get(2).containsGenericInfo());
        Assert.assertFalse(parameters.get(3).containsGenericInfo());

        Assert.assertNull(parameters.get(3).getMatchUsing());
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

        Assert.assertEquals(definition.getParameterDefinition(0).getMatchUsing(), MatchByNameMatchingStrategy.class);
        Assert.assertEquals(definition.getParameterDefinition(1).getType(), new TypeReference<Binding<Integer>>(){}.getType());
        Assert.assertTrue(definition.getParameterDefinition("bind").containsGenericInfo());

        Assert.assertEquals(definition.getParameterDefinition(2).getType(), new TypeReference<Optional<String>>(){}.getType());
        Assert.assertTrue(definition.getParameterDefinition("newOpt").containsGenericInfo());

        Assert.assertEquals(definition.getParameterDefinition(3).getDescription(), "arg 4");
        Assert.assertEquals(definition.getParameterDefinition("match").getDefaultValueText(), "hello");
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
        Assert.assertEquals(definition.getParameterDefinition(0).getName(), "arg1");
        Assert.assertEquals(definition.getParameterDefinition(0).getType(), BigInteger.class);
        Assert.assertEquals(definition.getParameterDefinition(0).getDefaultValueText(), "100");
        Assert.assertEquals(definition.getParameterDefinition("arg1").getDescription(), "desc");
    }

    @Test
    public void testMethodParamDefinition10() {
        Function function = Function.builder().with((Integer num, Binding<Integer> bind, Optional<String> opt, String match) -> 1)
                .param(0)
                    .name("arg1")
                    .description("desc")
                    .type(BigInteger.class)
                    .defaultValueText("100")
                .build()
                .build();

        MethodDefinition definition = ((Definable<MethodDefinition>) function).getDefinition();
        Assert.assertEquals(definition.getParameterDefinition(0).getName(), "arg1");
        Assert.assertEquals(definition.getParameterDefinition("arg1").getDescription(), "desc");
        Assert.assertEquals(definition.getParameterDefinition(0).getType(), BigInteger.class);
        Assert.assertEquals(definition.getParameterDefinition(0).getDefaultValueText(), "100");
    }

    @Test
    public void testMethodParamDefinition11() {
        Condition condition = Condition.builder().with((@Description("hello1") Integer num, @Description("hello2") Binding<Integer> bind,
                                                        @Description("hello3") Optional<Integer> opt, @Description("hello4") String match) -> true).build();
        MethodDefinition definition = ((Definable<MethodDefinition>) condition).getDefinition();
        System.err.println(definition);

        Assert.assertTrue(LambdaUtils.isLambda(condition.getTarget()));
        SerializedLambda lambda = LambdaUtils.getSafeSerializedLambda(condition.getTarget());
        Assert.assertNotNull(lambda);
        Class<?> implClass = LambdaUtils.getImplementationClass(lambda);
        Assert.assertNotNull(implClass);
        Method m = LambdaUtils.getImplementationMethod(lambda, implClass);
        Assert.assertNotNull(m);
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

        public void testMethod4(@Param(defaultValue = "123") Integer arg1,
                                Binding<Integer> arg2, Optional<String> arg3,
                                @Param(matchUsing = MatchByNameAndTypeMatchingStrategy.class) String arg4) {}

        public void testMethod5(@Param("salary1") BigDecimal arg1,
                                @Param(name = "salary2") @Description("salary 2") BigDecimal arg2) {}

        public void testMethod6(@Param(value = "salary1", defaultValue = "0.00") BigDecimal arg1,
                                @Param(value = "salary2", matchUsing = MatchByTypeMatchingStrategy.class) Binding<BigDecimal> arg2,
                                Optional<String> firstName) {}
    }
}
