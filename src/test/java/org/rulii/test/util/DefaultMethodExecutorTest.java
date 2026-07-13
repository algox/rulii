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
package org.rulii.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.model.UnrulyException;
import org.rulii.util.reflect.DefaultMethodExecutor;
import org.rulii.util.reflect.MethodExecutor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Test cases for DefaultMethodExecutor and the MethodExecutor factory/default methods.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultMethodExecutorTest {

    public DefaultMethodExecutorTest() {
        super();
    }

    public String greet(String name) {
        return "Hello " + name;
    }

    public static String staticGreet(String name) {
        return "Static Hello " + name;
    }

    private String privateGreet(String name) {
        return "Private Hello " + name;
    }

    public String throwingMethod() {
        throw new IllegalStateException("boom from target method");
    }

    @Test
    public void testBuildFactory_returnsDefaultMethodExecutor() throws NoSuchMethodException {
        Method method = DefaultMethodExecutorTest.class.getMethod("greet", String.class);
        MethodExecutor executor = MethodExecutor.build(method);
        Assertions.assertInstanceOf(DefaultMethodExecutor.class, executor);
        Assertions.assertEquals(method, executor.method());
    }

    @Test
    public void testExecuteInstanceMethod() throws Throwable {
        Method method = DefaultMethodExecutorTest.class.getMethod("greet", String.class);
        MethodExecutor executor = MethodExecutor.build(method);
        String result = executor.execute(this, "World");
        Assertions.assertEquals("Hello World", result);
    }

    @Test
    public void testExecuteStaticMethod() throws Throwable {
        Method method = DefaultMethodExecutorTest.class.getMethod("staticGreet", String.class);
        MethodExecutor executor = MethodExecutor.build(method);
        String result = executor.execute(null, "World");
        Assertions.assertEquals("Static Hello World", result);
    }

    @Test
    public void testExecutePrivateMethod() throws Throwable {
        Method method = DefaultMethodExecutorTest.class.getDeclaredMethod("privateGreet", String.class);
        MethodExecutor executor = MethodExecutor.build(method);
        String result = executor.execute(this, "World");
        Assertions.assertEquals("Private Hello World", result);
    }

    @Test
    public void testExecuteWithListArguments() throws Throwable {
        Method method = DefaultMethodExecutorTest.class.getMethod("greet", String.class);
        MethodExecutor executor = MethodExecutor.build(method);
        String result = executor.execute(this, List.of("World"));
        Assertions.assertEquals("Hello World", result);
    }

    @Test
    public void testExecuteWithWrongNumberOfArgs_throwsUnrulyException() throws NoSuchMethodException {
        Method method = DefaultMethodExecutorTest.class.getMethod("greet", String.class);
        MethodExecutor executor = MethodExecutor.build(method);
        Assertions.assertThrows(UnrulyException.class, () -> executor.execute(this, "World", "extra"));
    }

    @Test
    public void testTargetMethodException_propagatesUnwrapped() throws NoSuchMethodException {
        Method method = DefaultMethodExecutorTest.class.getMethod("throwingMethod");
        MethodExecutor executor = MethodExecutor.build(method);
        IllegalStateException e = Assertions.assertThrows(IllegalStateException.class, () -> executor.execute(this));
        Assertions.assertEquals("boom from target method", e.getMessage());
    }

    @Test
    public void testNullMethod_throwsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new DefaultMethodExecutor(null));
    }

    @Test
    public void testToString_containsMethodAndDelegate() throws NoSuchMethodException {
        Method method = DefaultMethodExecutorTest.class.getMethod("greet", String.class);
        DefaultMethodExecutor executor = new DefaultMethodExecutor(method);
        String text = executor.toString();
        Assertions.assertTrue(text.contains("DefaultMethodExecutor"));
        Assertions.assertTrue(text.contains("greet"));
    }
}
