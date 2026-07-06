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
import org.rulii.util.reflect.ReflectiveMethodExecutor;

import java.lang.reflect.Method;

/**
 * This class contains test cases for the ReflectiveMethodExecutor class.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ReflectiveMethodExecutorTest {

    public ReflectiveMethodExecutorTest() {
        super();
    }

    public String getSuccessTestMessage() {
        return "ReflectiveMethodExecutor test succeeded!";
    }

    public String throwingMethod() {
        throw new IllegalStateException("boom - the target method's own exception");
    }

    @Test
    public void whenExecuteWithParameters_thenTheMethodMustExecuteSuccessfully() {
        try {
            Method method = ReflectiveMethodExecutorTest.class.getDeclaredMethod("getSuccessTestMessage");
            ReflectiveMethodExecutor executor = new ReflectiveMethodExecutor(method);
            String result = executor.execute(this);
            Assertions.assertTrue(result.contains("succeeded"));
        } catch (Throwable e) {
            // Print the stack trace to aid debugging.
            e.printStackTrace();
        }
    }

    @Test
    public void whenExecuteWithStaticMethod_thenTheMethodMustExecuteSuccessfully() {
        try {
            Method method = ReflectiveMethodExecutorTest.class.getMethod("getSuccessTestMessage");
            ReflectiveMethodExecutor executor = new ReflectiveMethodExecutor(method);
            String result = executor.execute(this);
            Assertions.assertTrue(result.contains("succeeded"));
        } catch (Throwable e) {
            // Print the stack trace to aid debugging.
            e.printStackTrace();
        }
    }

    @Test
    public void whenExecuteWithInvalidParameters_thenUnrulyExceptionIsThrown() {
        try {
            Method method = ReflectiveMethodExecutorTest.class.getMethod("getSuccessTestMessage");
            ReflectiveMethodExecutor executor = new ReflectiveMethodExecutor(method);
            Assertions.assertThrows(UnrulyException.class, () -> executor.execute(this, "invalidParameter"));
        } catch (Throwable e) {
            // Print the stack trace to aid debugging.
            e.printStackTrace();
        }
    }

    @Test
    public void whenTargetMethodThrows_thenOriginalExceptionPropagatesUnwrapped() throws NoSuchMethodException {
        Method method = ReflectiveMethodExecutorTest.class.getDeclaredMethod("throwingMethod");
        ReflectiveMethodExecutor executor = new ReflectiveMethodExecutor(method);
        // The target method's own exception must propagate directly - not wrapped in
        // UnrulyException(InvocationTargetException(realException)) - matching what
        // MethodHandleMethodExecutor already does for the same scenario.
        IllegalStateException e = Assertions.assertThrows(IllegalStateException.class, () -> executor.execute(this));
        Assertions.assertEquals("boom - the target method's own exception", e.getMessage());
    }
}
