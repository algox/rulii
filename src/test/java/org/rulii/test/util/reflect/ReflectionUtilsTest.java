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
package org.rulii.test.util.reflect;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.annotation.Action;
import org.rulii.annotation.Then;
import org.rulii.bind.Binding;
import org.rulii.model.UnrulyException;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.TriFunction;
import org.rulii.util.reflect.LambdaUtils;
import org.rulii.util.reflect.ReflectionUtils;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Test cases related to ReflectionUtils.
 *
 * @author Max Arulananthan
 */
public class ReflectionUtilsTest {

    public ReflectionUtilsTest() {
        super();
    }

    @Test
    public void parameterNamesTest1() throws NoSuchMethodException {
        Method m = SomeClass.class.getDeclaredMethod("testMethod", String.class, Integer.class, List.class);
        Assertions.assertNotNull(m);
        String[] parameterNames = ReflectionUtils.getParameterNames(m);
        Assertions.assertEquals(3, parameterNames.length);
        Assertions.assertEquals("a", parameterNames[0]);
        Assertions.assertEquals("b", parameterNames[1]);
        Assertions.assertEquals("c", parameterNames[2]);
    }

    @Test
    public void parameterNamesTest2() throws NoSuchMethodException {
        TriFunction<Boolean, Integer, String, List<Float>> lambda = (Integer a, String b, List<Float> c) -> a > 100;
        SerializedLambda serializedLambda = LambdaUtils.getSafeSerializedLambda(lambda);
        Assertions.assertNotNull(serializedLambda);
        Class<?> c = LambdaUtils.getImplementationClass(serializedLambda);
        Assertions.assertNotNull(c);
        Method m = LambdaUtils.getImplementationMethod(serializedLambda, c);
        String[] parameterNames = ReflectionUtils.getParameterNames(m);
        Assertions.assertEquals(3, parameterNames.length);
        Assertions.assertEquals("a", parameterNames[0]);
        Assertions.assertEquals("b", parameterNames[1]);
        Assertions.assertEquals("c", parameterNames[2]);
    }

    @Test
    public void postConstructorTest1() {
        Method postConstructor = ReflectionUtils.getPostConstructMethods(SomeClass.class);
        Assertions.assertNotNull(postConstructor);
        ReflectionUtils.invokePostConstruct(postConstructor, new SomeClass());
    }

    @Test
    public void postConstructorTest2() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            // 2 PostConstructors
            ReflectionUtils.getPostConstructMethods(OtherClass.class);
        });
    }

    @Test
    public void postConstructorTest3() {
        Assertions.assertThrows(UnrulyException.class, () -> {
            ReflectionUtils.getPostConstructMethods(ErrorClass.class);
        });
    }

    @Test
    public void postConstructorTest4() {
        Method postConstructor = ReflectionUtils.getPostConstructMethods(PostConstruct1.class);
        Assertions.assertNotNull(postConstructor);
    }

    @Test
    public void postConstructorTest5() {
        Method postConstructor = ReflectionUtils.getPostConstructMethods(PostConstruct2.class);
        Assertions.assertNotNull(postConstructor);
    }

    @Test
    public void isAnnotatedTest1() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ReflectionUtils.isAnnotated(null, null);
        });
    }

    @Test
    public void isAnnotatedTest2() throws NoSuchMethodException {
        Method method = TestClass.class.getDeclaredMethod("execute");
        Assertions.assertFalse(ReflectionUtils.isAnnotated(method, Action.class));
    }

    @Test
    public void isAnnotatedTest3() throws NoSuchMethodException {
        Method method1 = TestClass.class.getDeclaredMethod("execute", Map.class);
        Assertions.assertTrue(ReflectionUtils.isAnnotated(method1, Action.class));
        Method method2 = TestClass.class.getDeclaredMethod("execute", List.class);
        Assertions.assertTrue(ReflectionUtils.isAnnotated(method2, Action.class));
    }

    @Test
    public void getMethodsWithAnnotationTest1() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ReflectionUtils.getMethodsWithAnnotation(null, null);
        });
    }

    @Test
    public void getMethodsWithAnnotationTest2() throws NoSuchMethodException {
        Method method1 = TestClass.class.getDeclaredMethod("execute", Map.class);
        Method method2 = TestClass.class.getDeclaredMethod("execute", List.class);
        Method method3 = BaseClass2.class.getDeclaredMethod("run", String.class);
        Method method4 = Interface2.class.getDeclaredMethod("test", Integer.class, String.class);
        Method method5 = BaseClass2.class.getDeclaredMethod("init");
        List<Method> methods = Arrays.asList(ReflectionUtils.getMethodsWithAnnotation(TestClass.class, Action.class));
        Assertions.assertTrue(methods.contains(method1));
        Assertions.assertTrue(methods.contains(method2));
        Assertions.assertTrue(methods.contains(method3));
        Assertions.assertTrue(methods.contains(method4));
        Assertions.assertFalse(methods.contains(method5));
    }

    @Test
    public void testUnderlyingTypes1() throws NoSuchMethodException {
        Method method = Interface3.class.getDeclaredMethod("test", Binding.class, Optional.class, Optional.class);
        Assertions.assertEquals(ReflectionUtils.getUnderlyingType(method.getGenericParameterTypes()[0], Binding.class), String.class);
        Assertions.assertEquals(ReflectionUtils.getUnderlyingType(method.getGenericParameterTypes()[1], Optional.class), Integer.class);
    }

    @Test
    public void testUnderlyingTypes2() throws NoSuchMethodException {
        Condition condition = Condition.builder().with((Binding<Integer> bind, Optional<String> opt) -> true)
                .build();
        Method method = LambdaUtils.getImplementationMethod(condition.getTarget());
        LambdaUtils.getSafeSerializedLambda(condition.getTarget());
        Assertions.assertEquals(ReflectionUtils.getUnderlyingType(method.getGenericParameterTypes()[0], Binding.class), Object.class);
        Assertions.assertEquals(ReflectionUtils.getUnderlyingType(method.getGenericParameterTypes()[1], Optional.class), Object.class);
    }

    private static class SomeClass {

        public SomeClass() {
            super();
        }

        @PostConstruct
        private void init() {
            //
        }

        public void testMethod(String a, Integer b, List<Float> c) {
            // test
        }
    }

    private static class OtherClass {

        public OtherClass() {
            super();
        }

        @PostConstruct
        private void init1() {
            //
        }

        @PostConstruct
        private void init2() {
            //
        }
    }

    private static class ErrorClass {

        public ErrorClass() {
            super();
        }

        @PostConstruct
        private int init1() {
            return 0;
        }

        @PostConstruct
        private void init2(int x) {
            //
        }

        @PostConstruct
        private void init3() throws Exception {
            //
        }
    }

    private static class PostConstruct1 {

        public PostConstruct1() {
            super();
        }

        @PostConstruct
        private void init() {}
    }

    private static class PostConstruct2 {

        public PostConstruct2() {
            super();
        }

        @javax.annotation.PostConstruct
        private void init() {}
    }

    private abstract static class TestClass<A, B> extends BaseClass1 {

        @Action
        public abstract <C,D> void execute(Map<C, D> map);

        @Then
        public abstract B execute(List<A> a);

        private void execute() {

        }
    }

    private abstract static class BaseClass1 extends BaseClass2 {

        public void run() {

        }
    }

    private abstract static class BaseClass2  implements Interface1 {

        @PostConstruct
        private void init() {
            //
        }

        public void run() {

        }

        @Then
        public void run(String x) {}
    }

    private interface Interface1 extends Serializable, Cloneable, Interface2 {

        void test(Integer a);
    }

    private interface Interface2  {

        @Then
        void test(Integer a, String b);
    }

    private interface Interface3 {

        void test(Binding<String> value1, Optional<Integer> value2, Optional<Binding<String>> value3);
    }
}
