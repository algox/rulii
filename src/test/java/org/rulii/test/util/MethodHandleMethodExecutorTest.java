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

    public static class Calculator {

        private final long offset;

        public Calculator(long offset) {
            super();
            this.offset = offset;
        }

        public long scale(int value, long factor, boolean negate) {
            long result = value * factor + offset;
            return negate ? -result : result;
        }

        public void touch(StringBuilder text) {
            text.append("!");
        }

        public String fail(String message) throws java.io.IOException {
            throw new java.io.IOException(message);
        }
    }

    @Test
    public void testPrimitiveArgsAndResult() throws Throwable {
        MethodExecutor executor = new MethodHandleMethodExecutor(Calculator.class.getMethod("scale", int.class, long.class, boolean.class));

        Object result = executor.execute(new Calculator(0), 6, 7L, true);
        Assertions.assertEquals(Long.valueOf(-42L), result);
        // An Integer is widened to the long parameter, as a direct call would.
        Object widened = executor.execute(new Calculator(0), 6, 7, false);
        Assertions.assertEquals(Long.valueOf(42L), widened);
    }

    @Test
    public void testVoidMethodReturnsNull() throws Throwable {
        StringBuilder text = new StringBuilder("done");
        MethodExecutor executor = new MethodHandleMethodExecutor(Calculator.class.getMethod("touch", StringBuilder.class));

        Object result = executor.execute(new Calculator(0), text);
        Assertions.assertNull(result);
        Assertions.assertEquals("done!", text.toString());
    }

    @Test
    public void testSameExecutorOnDifferentTargets() throws Throwable {
        // One executor serves every run of a rule: nothing of a call may stick to it.
        MethodExecutor executor = new MethodHandleMethodExecutor(Calculator.class.getMethod("scale", int.class, long.class, boolean.class));

        Object first = executor.execute(new Calculator(1), 2, 3L, false);
        Object second = executor.execute(new Calculator(100), 2, 3L, false);
        Object third = executor.execute(new Calculator(1), 2, 3L, false);
        Assertions.assertEquals(Long.valueOf(7L), first);
        Assertions.assertEquals(Long.valueOf(106L), second);
        Assertions.assertEquals(Long.valueOf(7L), third);
    }

    @Test
    public void testExecutorsOfTheSameMethodAreIndependent() throws Throwable {
        Calculator one = new Calculator(1);
        Calculator hundred = new Calculator(100);
        MethodExecutor first = new MethodHandleMethodExecutor(Calculator.class.getMethod("scale", int.class, long.class, boolean.class));
        MethodExecutor second = new MethodHandleMethodExecutor(Calculator.class.getMethod("scale", int.class, long.class, boolean.class));

        Object a = first.execute(one, 2, 3L, false);
        Object b = second.execute(hundred, 2, 3L, true);
        Object c = first.execute(hundred, 2, 3L, false);
        Assertions.assertEquals(Long.valueOf(7L), a);
        Assertions.assertEquals(Long.valueOf(-106L), b);
        Assertions.assertEquals(Long.valueOf(106L), c);
    }

    public static class Joiner {

        public Joiner() {
            super();
        }

        public String five(String a, int b, Long c, boolean d, char e) {
            return a + b + c + d + e;
        }

        public static String six(String a, int b, Long c, boolean d, char e, double f) {
            return a + b + c + d + e + f;
        }
    }

    @Test
    public void testMoreThanFourArguments() throws Throwable {
        // Longer argument lists take another path inside the executor; the outcome must be the same.
        MethodExecutor five = new MethodHandleMethodExecutor(Joiner.class.getMethod("five",
                String.class, int.class, Long.class, boolean.class, char.class));
        MethodExecutor six = new MethodHandleMethodExecutor(Joiner.class.getMethod("six",
                String.class, int.class, Long.class, boolean.class, char.class, double.class));

        Object instanceResult = five.execute(new Joiner(), "a", 1, 2L, true, 'x');
        Object staticResult = six.execute(null, "a", 1, 2L, true, 'x', 1.5d);
        Assertions.assertEquals("a12truex", instanceResult);
        Assertions.assertEquals("a12truex1.5", staticResult);

        Assertions.assertThrows(UnrulyException.class, () -> five.execute(new Joiner(), "a", 1, 2L, true));
        Assertions.assertThrows(ClassCastException.class, () -> five.execute(new Joiner(), "a", "one", 2L, true, 'x'));
        Assertions.assertThrows(NullPointerException.class, () -> six.execute(null, "a", null, 2L, true, 'x', 1.5d));
    }

    @Test
    public void testWrongArgumentType_throwsClassCastException() throws Throwable {
        // AbstractRunnable relies on this to report a ParameterMismatchException.
        MethodExecutor executor = new MethodHandleMethodExecutor(Calculator.class.getMethod("touch", StringBuilder.class));
        Assertions.assertThrows(ClassCastException.class, () -> executor.execute(new Calculator(0), "not a StringBuilder"));
    }

    @Test
    public void testWrongTargetType_throwsClassCastException() throws Throwable {
        MethodExecutor executor = new MethodHandleMethodExecutor(Calculator.class.getMethod("touch", StringBuilder.class));
        Assertions.assertThrows(ClassCastException.class, () -> executor.execute("not a Calculator", new StringBuilder()));
    }

    @Test
    public void testNullForPrimitiveParameter_throwsNullPointerException() throws Throwable {
        MethodExecutor executor = new MethodHandleMethodExecutor(Calculator.class.getMethod("scale", int.class, long.class, boolean.class));
        Assertions.assertThrows(NullPointerException.class, () -> executor.execute(new Calculator(0), null, 7L, true));
    }

    @Test
    public void testNullTargetOnInstanceMethod_throwsNullPointerException() throws Throwable {
        MethodExecutor executor = new MethodHandleMethodExecutor(Calculator.class.getMethod("touch", StringBuilder.class));
        Assertions.assertThrows(NullPointerException.class, () -> executor.execute(null, new StringBuilder()));
    }

    @Test
    public void testExceptionOfTheMethodIsNotWrapped() throws Throwable {
        MethodExecutor executor = new MethodHandleMethodExecutor(Calculator.class.getMethod("fail", String.class));
        java.io.IOException e = Assertions.assertThrows(java.io.IOException.class, () -> executor.execute(new Calculator(0), "boom"));
        Assertions.assertEquals("boom", e.getMessage());
    }
}
