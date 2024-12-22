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

import org.rulii.annotation.Param;
import org.rulii.bind.Binding;
import org.rulii.bind.BindingException;
import org.rulii.bind.Bindings;
import org.rulii.bind.ScopedBindings;
import org.rulii.bind.match.*;
import org.rulii.convert.ConverterRegistry;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.model.Definable;
import org.rulii.model.MethodDefinition;
import org.rulii.model.SourceDefinition;
import org.rulii.util.TypeReference;
import org.rulii.util.reflect.LambdaUtils;
import org.rulii.util.reflect.ObjectFactory;
import org.rulii.util.reflect.ReflectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * Parameter Resolver tests.
 *
 * @author Max Arulananthan
 */
public class ParameterResolverTest {

    public ParameterResolverTest() {
        super();
    }

    @Test
    public void matchByNameTest1() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod1"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", String.class, "Hello World!");
        bindings.bind("b", new TypeReference<Set<Integer>>() {}, new HashSet<>());
        bindings.bind("c", new TypeReference<List<Integer>>() {}, new ArrayList<>());
        bindings.bind("d", new TypeReference<Map<?, Long>>() {}, new HashMap<>());

        Assert.assertTrue(definitions.size() == 1);
        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 4);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("a") && !matches.get(0).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(1).getBinding().getName().equals("b") && !matches.get(1).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(2).getBinding().getName().equals("c") && matches.get(2).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(3).getBinding().getName().equals("d") && !matches.get(3).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(0).equals(bindings.getValue("a")));
        Assert.assertTrue(values.get(1).equals(bindings.getValue("b")));
        Assert.assertTrue(values.get(2).equals(bindings.getBinding("c")));
        Assert.assertTrue(values.get(3).equals(bindings.getValue("d")));
    }

    @Test
    public void matchByNameTest2() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod2"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", int.class, 10);
        bindings.bind("values", new TypeReference<Map<String, Integer>>() {}, new HashMap<>());
        bindings.bind("c", new TypeReference<List<Integer>>() {}, new ArrayList<>());
        bindings.bind("d", new TypeReference<Map<?, Long>>() {}, new HashMap<>());

        Assert.assertTrue(definitions.size() == 1);
        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 2);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("a") && !matches.get(0).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(1).getBinding().getName().equals("values") && !matches.get(1).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0),
                bindings,BindingMatchingStrategy.builder().matchByName(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(0).equals(bindings.getValue("a")));
        Assert.assertTrue(values.get(1).equals(bindings.getValue("values")));
    }

    @Test
    public void matchByNameTest3() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod3"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 255);
        bindings.bind("b", new TypeReference<List<Integer>>() {}, new ArrayList<>());
        bindings.bind("c", new TypeReference<Map<?, Long>>() {}, new HashMap<>());

        Assert.assertTrue(definitions.size() == 1);
        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 1);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("x") && matches.get(0).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(0).equals(bindings.getBinding("x")));
    }

    @Test
    public void matchByNameTest4() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod4"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", String.class, "Hello world!");
        bindings.bind("b", int.class, 1);
        bindings.bind("x", new TypeReference<List<Integer>>() {}, new ArrayList<>());

        Assert.assertTrue(definitions.size() == 1);
        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 3);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("a") && !matches.get(0).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(1).getBinding().getName().equals("b") && !matches.get(1).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(2).getBinding().getName().equals("x") && matches.get(2).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(0).equals(bindings.getValue("a")));
        Assert.assertTrue(values.get(1).equals(bindings.getValue("b")));
        Assert.assertTrue(values.get(2).equals(bindings.getBinding("x")));
    }

    @Test
    public void matchByTypeTest1() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod1"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", String.class, "Ruling class");
        bindings.bind("b", new TypeReference<Set<Integer>>() {}, new HashSet<>());
        bindings.bind("c", new TypeReference<List<Integer>>() {}, new ArrayList<>());
        bindings.bind("d", new TypeReference<Map<String, Long>>() {}, new HashMap<>());

        Assert.assertTrue(definitions.size() == 1);
        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByType(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 4);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("a") && !matches.get(0).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(1).getBinding().getName().equals("b") && !matches.get(1).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(2).getBinding().getName().equals("c") && matches.get(2).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(3).getBinding().getName().equals("d") && !matches.get(3).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByType(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(0).equals(bindings.getValue("a")));
        Assert.assertTrue(values.get(1).equals(bindings.getValue("b")));
        Assert.assertTrue(values.get(2).equals(bindings.getBinding("c")));
        Assert.assertTrue(values.get(3).equals(bindings.getValue("d")));
    }

    @Test
    public void matchByTypeTest2() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod2"), SourceDefinition.build());

        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("key1", Integer.class, 23);

        bindings.addScope().getBindings()
                .bind("key2", new TypeReference<Map<String, Integer>>() {}, new HashMap<>());

        Assert.assertTrue(definitions.size() == 1);
        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByType(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 2);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("key1") && !matches.get(0).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(1).getBinding().getName().equals("key2") && !matches.get(1).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByType(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(0).equals(bindings.getValue("key1")));
        Assert.assertTrue(values.get(1).equals(bindings.getValue("key2")));
    }

    @Test(expected = BindingException.class)
    public void matchByTypeTest3() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod3"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class, "Ruling class");
        bindings.bind("key2", Integer.class, 24);

        Assert.assertTrue(definitions.size() == 1);
        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByType(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 1);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("key1") && matches.get(0).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByType(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(0).equals(bindings.getValue("key1")));
    }

    @Test
    public void matchByTypeTest4() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod4"), SourceDefinition.build());

        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind(Binding.builder().with("key1").value("test").build());
        bindings.addScope().getBindings()
                .bind("key3", new TypeReference<List<Integer>>() {}, new ArrayList<>());

        Assert.assertTrue(definitions.size() == 1);
        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByType(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 3);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("key1") && !matches.get(0).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(1).getBinding() == null);
        Assert.assertTrue(matches.get(2).getBinding().getName().equals("key3") && matches.get(2).getDefinition().isBindingType());

        resolver.resolve(matches, definitions.get(0), bindings, BindingMatchingStrategy.builder().matchByType(),
                ConverterRegistry.builder().build(), ObjectFactory.builder().build());
    }

    @Test
    public void matchByNameAndTypeTest1() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod1"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", String.class, "Ruling class");
        bindings.bind("b", new TypeReference<Set<Integer>>() {}, new HashSet<>());
        bindings.bind("c", new TypeReference<List<Integer>>() {}, new Vector());
        bindings.bind("d", new TypeReference<Map<String, Long>>() {}, new HashMap<>());

        Assert.assertTrue(definitions.size() == 1);
        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByNameAndType(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 4);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("a") && !matches.get(0).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(1).getBinding().getName().equals("b") && !matches.get(1).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(2).getBinding().getName().equals("c") && matches.get(2).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(3).getBinding().getName().equals("d") && !matches.get(3).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByType(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(0).equals(bindings.getValue("a")));
        Assert.assertTrue(values.get(1).equals(bindings.getValue("b")));
        Assert.assertTrue(values.get(2).equals(bindings.getBinding("c")));
        Assert.assertTrue(values.get(3).equals(bindings.getValue("d")));
    }

    @Test
    public void matchByNameAndTypeTest2() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod4"), SourceDefinition.build());

        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", String.class, "Ruling class");
        bindings.bind("b", Integer.class, 8);

        Bindings newScopeBindings = bindings.addScope().getBindings();
        newScopeBindings.bind("a", Integer.class, 50);
        newScopeBindings.bind("x", new TypeReference<List<Integer>>() {}, new ArrayList<>());

        Assert.assertTrue(definitions.size() == 1);
        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByNameAndType(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 3);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("a")
                && matches.get(0).getBinding().getType().equals(String.class) && !matches.get(0).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(1).getBinding().getName().equals("b") && !matches.get(1).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(2).getBinding().getName().equals("x") && matches.get(2).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByNameAndType(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(2).equals(bindings.getBinding("x")));
        bindings.removeScope();
        Assert.assertTrue(values.get(0).equals(bindings.getValue("a")));
        Assert.assertTrue(values.get(1).equals(bindings.getValue("b")));
    }

    @Test
    public void matchByNameThenByTypeTest() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod4"), SourceDefinition.build());

        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", String.class, "Ruling class");
        bindings.bind("b", Integer.class, 1);
        bindings.bind("c", new TypeReference<List<Integer>>() {}, new ArrayList<>());

        Bindings newScopeBindings = bindings.addScope().getBindings();
        newScopeBindings.bind("a", Integer.class, 33);
        newScopeBindings.bind("q", new TypeReference<Set<Integer>>() {}, new HashSet<>());

        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByNameThenByType(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 3);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("a")
                && matches.get(0).getBinding().getType().equals(Integer.class) && !matches.get(0).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(1).getBinding().getName().equals("b") && !matches.get(1).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(2).getBinding().getName().equals("c") && matches.get(2).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByNameThenByType(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(0).equals(bindings.getValue("a")));
        bindings.removeScope();
        Assert.assertTrue(values.get(1).equals(bindings.getValue("b")));
        Assert.assertTrue(values.get(2).equals(bindings.getBinding("c")));
    }

    @Test
    public void matchByNameAndTypeThenByTypeTest() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod4"), SourceDefinition.build());

        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", String.class, "Ruling class");
        bindings.bind("b", Integer.class, 34);
        bindings.bind("c", new TypeReference<List<Integer>>() {}, new ArrayList<>());

        Bindings newScopeBindings = bindings.addScope().getBindings();
        newScopeBindings.bind("a", Integer.class, 3);
        newScopeBindings.bind("x", new TypeReference<Set<Integer>>() {}, new HashSet<>());

        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByNameAndTypeThenByJustType(), ObjectFactory.builder().build());

        Assert.assertTrue(matches.size() == 3);
        Assert.assertTrue(matches.get(0).getBinding().getName().equals("a")
                && matches.get(0).getBinding().getType().equals(String.class) && !matches.get(0).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(1).getBinding().getName().equals("b") && !matches.get(1).getDefinition().isBindingType());
        Assert.assertTrue(matches.get(2).getBinding().getName().equals("c") && matches.get(2).getDefinition().isBindingType());

        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByNameAndTypeThenByJustType(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(1).equals(bindings.getValue("b")));
        Assert.assertTrue(values.get(2).equals(bindings.getBinding("c")));
        bindings.removeScope();
        Assert.assertTrue(values.get(0).equals(bindings.getValue("a")));
    }

    @Test
    public void valueResolverTest() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod1"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("a", String.class, "Ruling class");
        bindings.bind("b", new TypeReference<Set<Integer>>() {}, new HashSet<>());
        bindings.bind("c", new TypeReference<List<Integer>>() {}, new Vector());
        bindings.bind("d", new TypeReference<Map<String, Long>>() {}, new HashMap<>());

        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ObjectFactory.builder().build());
        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());

        Assert.assertTrue(values.get(0).equals(bindings.getValue("a")));
        Assert.assertTrue(values.get(1).equals(bindings.getValue("b")));
        Assert.assertTrue(values.get(2).equals(bindings.getBinding("c")));
        Assert.assertTrue(values.get(3).equals(bindings.getValue("d")));
    }

    @Test
    public void valueResolverDefaultValueTest() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod5"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("c", new BigDecimal("100.00"));

        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ObjectFactory.builder().build());
        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());

        Assert.assertTrue(values.get(0).equals(12345l));
        Assert.assertTrue(values.get(1).equals(0));
        Assert.assertTrue(values.get(2).equals(bindings.getValue("c")));
    }

    @Test
    public void valueResolverNoMatchTest() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod5"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();

        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ObjectFactory.builder().build());
        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
    }

    @Test
    public void autoConvertTest() {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod6"), SourceDefinition.build());

        Bindings bindings = Bindings.builder().standard();
        bindings.bind(a -> "Hello");
        bindings.bind("b", "12345");
        bindings.bind(x -> new ArrayList<>());

        List<ParameterMatch> matches = resolver.match(definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ObjectFactory.builder().build());
        List<Object> values = resolver.resolve(matches, definitions.get(0), bindings,
                BindingMatchingStrategy.builder().matchByName(), ConverterRegistry.builder().build(), ObjectFactory.builder().build());
        Assert.assertTrue(values.get(1).equals(12345));
        Assert.assertTrue(values.get(3).equals(321l));
    }

    @Test
    public void matchTest1() {
        Condition condition = Condition.builder().with((Integer num, Binding<Integer> bind, Optional<String> opt,
                                                       String match, BigDecimal salary, Optional<List<?>> values) -> true).build();
        Method method = LambdaUtils.getImplementationMethod(condition.getTarget());

        Class x = method.getDeclaringClass();
        ParameterResolver resolver = ParameterResolver.builder().build();
        MethodDefinition definition = MethodDefinition.load(method, false, SourceDefinition.build());

        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind(num -> 10, opt -> "abc", match -> "123");
        Binding<Integer> b = bindings.bind(bind -> new Integer(200));

        List<ParameterMatch> matches = resolver.match(definition, bindings,
                BindingMatchingStrategy.builder().matchByNameThenByType(),
                ObjectFactory.builder().build());
        List<Object> values = resolver.resolve(matches, definition, bindings,
                BindingMatchingStrategy.builder().matchByNameThenByType(),
                ConverterRegistry.builder().build(), ObjectFactory.builder().build());

        Assert.assertEquals(values.get(0), 10);
        Assert.assertEquals(values.get(1), b);
        Assert.assertEquals(values.get(2), Optional.of("abc"));
        Assert.assertEquals(values.get(3), "123");
        Assert.assertEquals(values.get(4), null);
        Assert.assertEquals(values.get(5), Optional.empty());
    }

    @Test
    public void matchTest2() {
        Action action = Action.builder().with((TestClass test, Binding<Integer> bind, Optional<String> opt,
                                               Optional<Binding<String>> someValue, Optional<TestClass> x) -> {})
                .param(1).type(new TypeReference<Binding<Integer>>(){}.getType()).build()
                .param(2).type(new TypeReference<Optional<String>>(){}.getType()).build()
                .param(3).type(new TypeReference<Optional<Binding<String>>>(){}.getType()).build()
                .param(4).type(new TypeReference<Optional<TestClass>>(){}.getType()).build()
                .build();

        ParameterResolver resolver = ParameterResolver.builder().build();
        MethodDefinition definition = action.getDefinition();
        ScopedBindings bindings = Bindings.builder().scoped();
        TestClass testClass = new TestClass();
        Binding<TestClass> testClassBinding = bindings.bind(a -> testClass);

        List<ParameterMatch> matches = resolver.match(definition, bindings,
                BindingMatchingStrategy.builder().matchByNameThenByType(),
                ObjectFactory.builder().build());
        List<Object> values = resolver.resolve(matches, definition, bindings,
                BindingMatchingStrategy.builder().matchByNameThenByType(),
                ConverterRegistry.builder().build(), ObjectFactory.builder().build());

        Assert.assertEquals(values.get(0), testClass);
        Assert.assertEquals(values.get(1), null);
        Assert.assertEquals(values.get(2), Optional.empty());
        Assert.assertEquals(values.get(3), Optional.empty());
        Assert.assertEquals(values.get(4), Optional.of(testClass));
        Assert.assertEquals(((Optional) values.get(4)).get(), testClass);
    }

    @Test
    public void matchTest3() {
        Action action = Action.builder().with((Integer num, Binding<BigDecimal> bind, Optional<List> opt, String match) -> {})
                .param(0)
                    .defaultValueText("100")
                .build()
                .param(1)
                    .type(new TypeReference<Binding<BigDecimal>>() {})
                .build()
                .param(2)
                    .matchUsing(MatchByNameMatchingStrategy.class)
                .build()
                .build();

        ParameterResolver resolver = ParameterResolver.builder().build();
        MethodDefinition definition = action.getDefinition();
        ScopedBindings bindings = Bindings.builder().scoped();

        Binding<BigDecimal> var2 = bindings.bind(bind -> new BigDecimal("1000.01"));
        bindings.addScope();
        bindings.bind(a -> "match");

        List<ParameterMatch> matches = resolver.match(definition, bindings,
                BindingMatchingStrategy.builder().matchByNameThenByType(),
                ObjectFactory.builder().build());
        List<Object> values = resolver.resolve(matches, definition, bindings,
                BindingMatchingStrategy.builder().matchByNameThenByType(),
                ConverterRegistry.builder().build(), ObjectFactory.builder().build());

        Assert.assertEquals(values.get(0), 100);
        Assert.assertEquals(values.get(1), var2);
        Assert.assertEquals(values.get(2), Optional.empty());
        Assert.assertEquals(values.get(3), "match");
    }

    @Test
    public void matchTest4() {
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod7"), SourceDefinition.build());
        ScopedBindings bindings = Bindings.builder().scoped();

        bindings.bind(arg1 -> 1000l);
        bindings.addScope();
        bindings.bind(a -> 10l);

        // Find the renamed parameter arg1
        List<Object> values = resolveMatches(definitions.get(0), bindings, BindingMatchingStrategy.builder().matchByNameThenByType());
        Assert.assertEquals(values.get(0), 1000l);

        // Find the default value
        bindings = Bindings.builder().scoped();
        bindings.bind(a -> 10l);
        values = resolveMatches(definitions.get(0), bindings, BindingMatchingStrategy.builder().matchByNameThenByType());
        Assert.assertEquals(values.get(0), 12345l);

        // Auto Convert
        bindings = Bindings.builder().scoped();
        bindings.bind(arg1 -> "5555");
        values = resolveMatches(definitions.get(0), bindings, BindingMatchingStrategy.builder().matchByNameThenByType());
        Assert.assertEquals(values.get(0), 5555l);

        // Match by Type
        bindings = Bindings.builder().scoped();
        bindings.bind(arg1 -> "5555");
        values = resolveMatches(definitions.get(0), bindings, BindingMatchingStrategy.builder().matchByNameThenByType());
        Assert.assertEquals(values.get(1), "5555");
    }

    @Test(expected = BindingException.class)
    public void matchTest5() {
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod7"), SourceDefinition.build());
        ScopedBindings bindings = Bindings.builder().scoped();

        bindings.bind(x -> "1", y -> "2");
        resolveMatches(definitions.get(0), bindings, BindingMatchingStrategy.builder().matchByNameThenByType());
    }

    @Test
    public void matchTest6() {
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod8"), SourceDefinition.build());
        ScopedBindings bindings = Bindings.builder().scoped();

        List<Object> values = resolveMatches(definitions.get(0), bindings, BindingMatchingStrategy.builder().build());
        Assert.assertEquals(ReflectionUtils.getDefaultValue(boolean.class), values.get(0));
        Assert.assertEquals(ReflectionUtils.getDefaultValue(byte.class), values.get(1));
        Assert.assertEquals(ReflectionUtils.getDefaultValue(char.class), values.get(2));
        Assert.assertEquals(ReflectionUtils.getDefaultValue(double.class), values.get(3));
        Assert.assertEquals(ReflectionUtils.getDefaultValue(float.class), values.get(4));
        Assert.assertEquals(ReflectionUtils.getDefaultValue(int.class), values.get(5));
        Assert.assertEquals(ReflectionUtils.getDefaultValue(long.class), values.get(6));
        Assert.assertEquals(ReflectionUtils.getDefaultValue(short.class), values.get(7));
    }

    @Test
    public void matchTest7() {
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod9"), SourceDefinition.build());

        List<Object> values = resolveMatches(definitions.get(0), Bindings.builder().standard(), BindingMatchingStrategy.builder().build());
        Assert.assertEquals(Optional.empty(), values.get(0));
        Assert.assertEquals(Optional.empty(), values.get(1));
        Assert.assertEquals(Optional.empty(), values.get(2));
        Assert.assertEquals(Optional.empty(), values.get(3));
        Assert.assertEquals(Optional.empty(), values.get(4));
        Assert.assertEquals(Optional.empty(), values.get(5));
        Assert.assertEquals(Optional.empty(), values.get(6));
        Assert.assertEquals(Optional.empty(), values.get(7));
    }

    @Test
    public void matchTest8() {
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod10"), SourceDefinition.build());

        TestClass x = new TestClass();
        Bindings bindings = Bindings.builder().standard(arg1 -> x);
        List<Object> values = resolveMatches(definitions.get(0), bindings, BindingMatchingStrategy.builder().matchByNameAndTypeThenByJustType());
        Assert.assertEquals(x, values.get(0));
        Assert.assertEquals(x, values.get(1));
        Assert.assertEquals(x, values.get(2));
    }

    @Test
    public void matchTest9() {
        Function function = Function.builder().with((Binding<Integer> num, Binding<BigDecimal> bind, Optional<List> opt, Binding<String> match) -> {return true;}).build();
        MethodDefinition definition = ((Definable<MethodDefinition>) function).getDefinition();
        Bindings bindings = Bindings.builder().standard();
        Binding bind1 = bindings.bind("num", 100);
        Binding bind2 = bindings.bind("bind", new BigDecimal("999"));
        Binding bind3 = bindings.bind("opt", new LinkedList<>());
        Binding bind4 = bindings.bind("match", "hello");

        List<Object> values = resolveMatches(definition, bindings, BindingMatchingStrategy.builder().matchByNameAndTypeThenByJustType());
        Assert.assertEquals(bind1, values.get(0));
        Assert.assertEquals(bind2, values.get(1));
        Assert.assertEquals(Optional.of(bind3.getValue()), values.get(2));
        Assert.assertEquals(bind4, values.get(3));
    }

    @Test
    public void matchTest10() {
        List<MethodDefinition> definitions = MethodDefinition.load(TestClass.class, method -> method.getName().equals("testMethod11"), SourceDefinition.build());
        MethodDefinition definition = definitions.get(0);

        Bindings bindings = Bindings.builder().standard();
        Binding bind1 = bindings.bind("a", "abcd");
        Binding bind2 = bindings.bind("b", 999);
        Binding bind3 = bindings.bind("x", Arrays.asList(1,2,3,4,5));
        Binding bind4 = bindings.bind("d", Long.MAX_VALUE);

        List<Object> values = resolveMatches(definition, bindings, BindingMatchingStrategy.builder().matchByNameAndType());
        Assert.assertEquals(bind1, values.get(0));
        Assert.assertEquals(bind2, values.get(1));
        Assert.assertEquals(bind3, values.get(2));
        Assert.assertEquals(bind4, values.get(3));
    }

    @Test
    public void matchTest11() {
        Condition function = Condition.builder().with((Binding<Integer> num, Binding<BigDecimal> bind, Optional<List> opt, Binding<String> match, Short sh) -> {return true;})
                .param("num")
                    .name("arg1")
                    .matchUsing(MatchByNameMatchingStrategy.class)
                .build()
                .param(1)
                    .name("arg2")
                    .type(BigDecimal.class)
                    .matchUsing(MatchByNameAndTypeMatchingStrategy.class)
                .build()
                .param("opt")
                    .name("arg3")
                    .type(List.class)
                    .matchUsing(MatchByTypeMatchingStrategy.class)
                .build()
                .param(3)
                    .name("arg4")
                    .type(String.class)
                    .matchUsing(SmartMatchByTypeMatchingStrategy.class)
                .build()
                .param(4)
                    .name("arg5")
                    .type(Short.class)
                    .defaultValueText("123")
                    .matchUsing(SmartMatchByTypeMatchingStrategy.class)
                .build()
                .build();
        MethodDefinition definition = function.getDefinition();
        Bindings bindings = Bindings.builder().standard();
        Binding bind1 = bindings.bind("arg1", 100);
        Binding bind2 = bindings.bind("arg2", new BigDecimal("999"));
        Binding bind3 = bindings.bind("arg3", new LinkedList<>());
        Binding bind4 = bindings.bind("x", "hello");

        List<Object> values = resolveMatches(definition, bindings, BindingMatchingStrategy.builder().matchByNameAndTypeThenByJustType());
        Assert.assertEquals(bind1, values.get(0));
        Assert.assertEquals(bind2, values.get(1));
        Assert.assertEquals(Optional.of(bind3.getValue()), values.get(2));
        Assert.assertEquals(bind4, values.get(3));
        Assert.assertEquals(Short.valueOf("123"), values.get(4));
    }

    private static List<Object> resolveMatches(MethodDefinition definition, Bindings bindings, BindingMatchingStrategy strategy) {
        ParameterResolver resolver = ParameterResolver.builder().build();
        List<ParameterMatch> matches = resolver.match(definition, bindings, strategy, ObjectFactory.builder().build());
        return resolver.resolve(matches, definition, bindings, strategy, ConverterRegistry.builder().build(), ObjectFactory.builder().build());
    }

    private static class TestClass {

        public boolean testMethod1(String a, Set<Integer> b, Binding<List<Integer>> c, Map<?, Long> d) {
            return true;
        }

        public void testMethod2(int a, Map<String, Integer> values) {}

        public boolean testMethod3(Binding<?> x) {
            return true;
        }

        public boolean testMethod4(String a, Integer b, Binding<List<Integer>> x) {
            return true;
        }

        public boolean testMethod5(@Param(defaultValue = "12345") Long a, int b, BigDecimal c) {
            return true;
        }

        public boolean testMethod6(String a, Integer b, Binding<List<Integer>> x, @Param(defaultValue = "321") Long d) {
            return true;
        }

        public void testMethod7(@Param(name = "arg1", matchUsing = MatchByNameMatchingStrategy.class, defaultValue = "12345") long a,
                                String b) {}

        public void testMethod8(boolean a, byte b, char c, double d, float f, int i, long l, short s) {}

        public void testMethod9(Optional<Boolean> a, Optional<Byte> b, Optional<Character> c, Optional<Double> d, Optional<Float> f,
                                Optional<Integer> i, Optional<Long> l, Optional<Short> s) {}

        public void testMethod10(@Param(name = "arg1", matchUsing = MatchByNameAndTypeMatchingStrategy.class) TestClass x,
                                 @Param(matchUsing = MatchByTypeMatchingStrategy.class) TestClass y,
                                 TestClass z) {}

        public boolean testMethod11(Binding<String> a, Binding<Integer> b, Binding<List<Integer>> x, Binding<Long> d) {
            return true;
        }
    }
}
