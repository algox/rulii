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
import org.rulii.util.reflect.MethodExecutor;
import org.rulii.util.reflect.MethodHandleMethodExecutor;

/**
 * MethodHandleMethodExecutorTest is a test class for validating the functionality of MethodHandleMethodExecutor.
 *
 * This class includes test cases for:
 * - Executing an instance method on a target object.
 * - Handling exceptions related to an invalid number of arguments.
 * - Executing a static method without a target object.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class MethodHandleMethodExecutorTest {

    public MethodHandleMethodExecutorTest() {
        super();
    }

    @Test
    public void testMethodExecution() throws Throwable {
        class TestObject {
            public String runTest(String param) {
                return "Hello " + param;
            }
        }

        TestObject obj = new TestObject();
        MethodExecutor executor = new MethodHandleMethodExecutor(TestObject.class.getMethod("runTest", String.class));
        String ret = executor.execute(obj, "World");
        Assertions.assertEquals("Hello World", ret);
    }

    @Test
    public void testInvalidNumberOfArgsException() {
        class TestObject {
            public String runTest(String param) {
                return "Hello " + param;
            }
        }

        Assertions.assertThrows(UnrulyException.class, () -> {
            TestObject obj = new TestObject();
            MethodExecutor executor = new MethodHandleMethodExecutor(TestObject.class.getMethod("runTest", String.class));
            executor.execute(obj, "World", "Extra arg");
        });
    }

    @Test
    public void testStaticMethodExecution() throws Throwable {
        class TestObject {
            public static String staticRunTest(String param) {
                return "Hello " + param;
            }
        }

        MethodExecutor executor = new MethodHandleMethodExecutor(TestObject.class.getMethod("staticRunTest", String.class));
        String ret = executor.execute(null, "World");
        Assertions.assertEquals("Hello World", ret);
    }

    public static class ZeroParamTestObject {

        public ZeroParamTestObject() {
            super();
        }

        public String runTest() {
            return "no args needed";
        }
    }

    @Test
    public void testNullArgsOnZeroParamMethod_executesSuccessfully() throws Throwable {
        // Regression test: null userArgs used to NPE in the arg-copy loop even though the
        // parameter-count check accepted it - ReflectiveMethodExecutor handles the same input,
        // and both strategies must behave identically behind DefaultMethodExecutor.
        MethodExecutor executor = new MethodHandleMethodExecutor(ZeroParamTestObject.class.getMethod("runTest"));
        String ret = executor.execute(new ZeroParamTestObject(), (Object[]) null);
        Assertions.assertEquals("no args needed", ret);
    }

    @Test
    public void testNullArgsListOnZeroParamMethod_executesSuccessfully() throws Throwable {
        // The List overload converts a null list to a null array - the original path that
        // exposed the NPE.
        MethodExecutor executor = new MethodHandleMethodExecutor(ZeroParamTestObject.class.getMethod("runTest"));
        String ret = executor.execute(new ZeroParamTestObject(), (java.util.List<Object>) null);
        Assertions.assertEquals("no args needed", ret);
    }

    @Test
    public void testNullArgsOnMethodWithParams_throwsUnrulyException() throws Throwable {
        class TestObject {
            public String runTest(String param) {
                return "Hello " + param;
            }
        }

        // Null args must still fail the parameter-count check when the method expects arguments.
        MethodExecutor executor = new MethodHandleMethodExecutor(TestObject.class.getMethod("runTest", String.class));
        Assertions.assertThrows(UnrulyException.class, () -> executor.execute(new TestObject(), (Object[]) null));
    }
}
