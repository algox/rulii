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
import org.rulii.util.reflect.MethodResolver;

import java.lang.reflect.Method;

/**
 * A test class for MethodResolver class to test the functionality of method resolution.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class MethodResolverTest {

    public MethodResolverTest() {
        super();
    }

    @Test
    public void testGetImplementationMethodValidCase() throws NoSuchMethodException {
        Class<?> clazz = MethodResolverTest.class;
        Method method = clazz.getMethod("testGetImplementationMethodValidCase");
        MethodResolver methodResolver = MethodResolver.builder().build();
        Method result = methodResolver.getImplementationMethod(clazz, method);
        Assertions.assertEquals(method, result, "Expected to get correct implementation method");
    }

    @Test
    public void testGetImplementationMethodWithNullInput() {
        MethodResolver methodResolver = MethodResolver.builder().build();
        Assertions.assertThrows(NullPointerException.class, () -> methodResolver.getImplementationMethod(null, null),
                "Expected to throw NullPointerException for null inputs");
    }
}
