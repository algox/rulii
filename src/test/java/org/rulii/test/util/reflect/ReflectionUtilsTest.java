/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
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

import java.beans.BeanInfo;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test cases related to ReflectionUtils.
 *
 * @author Max Arulananthan
 * @since 1.0
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
        // Each @PostConstruct-annotated method here is individually invalid (non-void return,
        // has a parameter, declares a checked exception) - none of them qualify as a real
        // post-construct method, regardless of which PostConstruct annotation type is used.
        Method postConstructor = ReflectionUtils.getPostConstructMethods(ErrorClass.class);
        Assertions.assertNull(postConstructor);
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
    public void testGetMethodLookup_concurrentAccessManyDistinctClasses_doesNotThrow() throws Exception {
        // getMethodLookup's cache is static and reached via MethodHandleMethodExecutor's
        // constructor, which is on the build path for essentially every Condition/Action/
        // Function/Rule - concurrent population of that cache with many distinct keys must not
        // corrupt it.
        Class<?>[] classes = {
                Integer.class, Long.class, Double.class, Float.class, Boolean.class,
                Character.class, Byte.class, Short.class, String.class, Object.class,
                List.class, Map.class, Optional.class, Arrays.class, Serializable.class,
                ReflectionUtilsTest.class, SomeClass.class, OtherClass.class, ErrorClass.class,
                PostConstruct1.class, PostConstruct2.class, TestClass.class, BaseClass1.class,
                BaseClass2.class, Interface1.class, Interface2.class, Interface3.class
        };

        int threadCount = 8;
        int iterations = 500;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicBoolean failed = new AtomicBoolean(false);
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            futures.add(executor.submit(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        Class<?> c = classes[i % classes.length];
                        MethodHandles.Lookup lookup = ReflectionUtils.getMethodLookup(c);
                        if (lookup == null) failed.set(true);
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            }));
        }

        for (Future<?> future : futures) future.get();
        executor.shutdown();

        Assertions.assertFalse(failed.get());
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
        Assertions.assertTrue(ReflectionUtils.isAnnotated(method2, Then.class));
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
        Assertions.assertFalse(methods.contains(method3));
        Assertions.assertFalse(methods.contains(method4));
        Assertions.assertFalse(methods.contains(method5));

        List<Method> thenMethods = Arrays.asList(ReflectionUtils.getMethodsWithAnnotation(TestClass.class, Then.class));
        Assertions.assertTrue(thenMethods.contains(method2));
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

    @Test
    public void testGetParameterNamesForConstructor() throws NoSuchMethodException {
        Constructor<?> ctor = ConstructorNamesClass.class.getDeclaredConstructor(String.class, Integer.class);
        String[] names = ReflectionUtils.getParameterNames(ctor);
        Assertions.assertNotNull(names);
        Assertions.assertEquals(2, names.length);
        Assertions.assertEquals("firstName", names[0]);
        Assertions.assertEquals("age", names[1]);
    }

    @Test
    public void testGetDefaultValue() {
        Assertions.assertEquals(0, ReflectionUtils.getDefaultValue(int.class));
        Assertions.assertEquals(false, ReflectionUtils.getDefaultValue(boolean.class));
        Assertions.assertEquals((long) 0, ReflectionUtils.getDefaultValue(long.class));
        Assertions.assertEquals((char) 0, ReflectionUtils.getDefaultValue(char.class));
        Assertions.assertNull(ReflectionUtils.getDefaultValue(void.class));
        // Non-primitive types have no default value entry.
        Assertions.assertNull(ReflectionUtils.getDefaultValue(String.class));
    }

    @Test
    public void testGetWrapperClass() {
        Assertions.assertEquals(Integer.class, ReflectionUtils.getWrapperClass(int.class));
        Assertions.assertEquals(Boolean.class, ReflectionUtils.getWrapperClass(boolean.class));
        Assertions.assertEquals(Void.class, ReflectionUtils.getWrapperClass(void.class));
        Assertions.assertEquals(Double.class, ReflectionUtils.getWrapperClass(double.class));
        // Non-primitive types come back unchanged.
        Assertions.assertEquals(String.class, ReflectionUtils.getWrapperClass(String.class));
    }

    @Test
    public void testInvokePostConstruct_targetMethodThrows_wrappedInIllegalArgumentException() {
        Method postConstructor = ReflectionUtils.getPostConstructMethods(ThrowingInit.class);
        Assertions.assertNotNull(postConstructor);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> ReflectionUtils.invokePostConstruct(postConstructor, new ThrowingInit()));
    }

    @Test
    public void testLoadBeanInfo() {
        BeanInfo beanInfo = ReflectionUtils.loadBeanInfo(ConstructorNamesClass.class);
        Assertions.assertNotNull(beanInfo);
        Assertions.assertTrue(beanInfo.getPropertyDescriptors().length > 0);
    }

    @Test
    public void testIsBindingAndIsOptional() throws NoSuchMethodException {
        Method method = Interface3.class.getDeclaredMethod("test", Binding.class, Optional.class, Optional.class);
        // Parameterized declarations
        Assertions.assertTrue(ReflectionUtils.isBinding(method.getGenericParameterTypes()[0]));
        Assertions.assertTrue(ReflectionUtils.isOptional(method.getGenericParameterTypes()[1]));
        // Raw class equals the wrapper itself
        Assertions.assertTrue(ReflectionUtils.isBinding(Binding.class));
        Assertions.assertTrue(ReflectionUtils.isOptional(Optional.class));
        // Unrelated types
        Assertions.assertFalse(ReflectionUtils.isBinding(String.class));
        Assertions.assertFalse(ReflectionUtils.isOptional(method.getGenericParameterTypes()[0]));
        Assertions.assertFalse(ReflectionUtils.isBinding(method.getGenericParameterTypes()[1]));
    }

    @Test
    public void testGetUnderlyingBindingAndOptionalTypes() throws NoSuchMethodException {
        Method method = Interface3.class.getDeclaredMethod("test", Binding.class, Optional.class, Optional.class);
        Assertions.assertEquals(String.class, ReflectionUtils.getUnderlyingBindingType(method.getGenericParameterTypes()[0]));
        Assertions.assertEquals(Integer.class, ReflectionUtils.getUnderlyingOptionalType(method.getGenericParameterTypes()[1]));
    }

    @Test
    public void testGetUnderlyingType_rawWrapperClass_returnsObject() {
        Assertions.assertEquals(Object.class, ReflectionUtils.getUnderlyingType(Binding.class, Binding.class));
        Assertions.assertEquals(Object.class, ReflectionUtils.getUnderlyingType(Optional.class, Optional.class));
    }

    @Test
    public void testGetUnderlyingType_notAParameterizedType_throwsUnrulyException() {
        Assertions.assertThrows(UnrulyException.class,
                () -> ReflectionUtils.getUnderlyingType(String.class, Binding.class));
    }

    @Test
    public void testGetUnderlyingType_wrongRawType_throwsUnrulyException() throws NoSuchMethodException {
        Method method = Interface3.class.getDeclaredMethod("test", Binding.class, Optional.class, Optional.class);
        // Parameter 1 is Optional<Integer> - asking for its underlying Binding type must fail.
        Assertions.assertThrows(UnrulyException.class,
                () -> ReflectionUtils.getUnderlyingType(method.getGenericParameterTypes()[1], Binding.class));
    }

    @Test
    public void testFindRunMethodWithRuleContext() {
        Assertions.assertNotNull(ReflectionUtils.findRunMethodWithRuleContext(WithRunMethod.class));
        Assertions.assertNull(ReflectionUtils.findRunMethodWithRuleContext(String.class));
    }

    @Test
    public void testIsJavaCoreClass() {
        Assertions.assertTrue(ReflectionUtils.isJavaCoreClass(int.class));
        Assertions.assertTrue(ReflectionUtils.isJavaCoreClass(String[].class));
        Assertions.assertTrue(ReflectionUtils.isJavaCoreClass(String.class));
        Assertions.assertTrue(ReflectionUtils.isJavaCoreClass(List.class));
        Assertions.assertFalse(ReflectionUtils.isJavaCoreClass(ReflectionUtilsTest.class));
    }

    @Test
    public void testGetAnnotationText_withExplicitValues() throws NoSuchMethodException {
        Method method = AnnotatedMethods.class.getDeclaredMethod("customized");
        String text = ReflectionUtils.getAnnotationText(method.getAnnotation(TextMarker.class));
        Assertions.assertTrue(text.startsWith("TextMarker("));
        Assertions.assertTrue(text.contains("label=custom"));
        Assertions.assertTrue(text.contains("tags=[a, b]"));
    }

    @Test
    public void testGetAnnotationText_allDefaults_rendersEmptyParens() throws NoSuchMethodException {
        Method method = AnnotatedMethods.class.getDeclaredMethod("defaults");
        String text = ReflectionUtils.getAnnotationText(method.getAnnotation(TextMarker.class));
        Assertions.assertEquals("TextMarker()", text);
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TextMarker {

        String label() default "";

        String[] tags() default {};
    }

    private static class AnnotatedMethods {

        public AnnotatedMethods() {
            super();
        }

        @TextMarker(label = "custom", tags = {"a", "b"})
        void customized() {}

        @TextMarker
        void defaults() {}
    }

    public static class ConstructorNamesClass {

        private String firstName;
        private Integer age;

        public ConstructorNamesClass(String firstName, Integer age) {
            super();
            this.firstName = firstName;
            this.age = age;
        }

        public String getFirstName() {
            return firstName;
        }

        public Integer getAge() {
            return age;
        }
    }

    private static class ThrowingInit {

        public ThrowingInit() {
            super();
        }

        @PostConstruct
        private void init() {
            throw new IllegalStateException("init failed");
        }
    }

    private static class WithRunMethod {

        public WithRunMethod() {
            super();
        }

        public void run(org.rulii.context.RuleContext ctx) {
            // no-op
        }
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
