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
import org.rulii.util.TypeReference;

/**
 * Tests for TypeReference.
 *
 * @author Max Arulananthan
 */
public class TypeReferenceTest {

    public TypeReferenceTest() {
        super();
    }

    @Test
    public void testEquals_separatelyCreatedAnonymousSubclasses_sameCapturedType_areEqual() {
        // Every "new TypeReference<X>(){}" expression creates its own distinct anonymous class,
        // even for the same type argument - equals() must not depend on getClass().
        TypeReference<String> a = new TypeReference<String>() {};
        TypeReference<String> b = new TypeReference<String>() {};

        Assertions.assertNotEquals(a.getClass(), b.getClass());
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEquals_withFactory_equalsUserCreatedAnonymousSubclass() {
        TypeReference<String> viaWith = TypeReference.with(String.class);
        TypeReference<String> viaAnonymous = new TypeReference<String>() {};

        Assertions.assertEquals(viaWith, viaAnonymous);
    }

    @Test
    public void testEquals_differentCapturedType_areNotEqual() {
        TypeReference<String> a = new TypeReference<String>() {};
        TypeReference<Integer> b = new TypeReference<Integer>() {};

        Assertions.assertNotEquals(a, b);
    }
}
