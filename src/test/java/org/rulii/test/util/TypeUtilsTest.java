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
package org.rulii.test.util;

import org.rulii.lib.apache.reflect.TypeUtils;
import org.rulii.util.TypeReference;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Test cases related to TypeUtils.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class TypeUtilsTest {

    public TypeUtilsTest() {
        super();
    }

    @Test
    public void testBasics() {
        Assert.assertTrue(TypeUtils.isAssignable(Integer.class, int.class));
        Assert.assertTrue(TypeUtils.isAssignable(List.class, Collection.class));
    }

    @Test
    public void testComplex() {
        Type lhs = new TypeReference<List<?>>() {}.getType();
        Type rhs = new TypeReference<List<String>>() {}.getType();
        Assert.assertTrue(TypeUtils.isAssignable(rhs, lhs));

        lhs = new TypeReference<Map<List<?>, ?>>() {}.getType();
        rhs = new TypeReference<Map<List<?>, String>>() {}.getType();
        Assert.assertTrue(TypeUtils.isAssignable(rhs, lhs));
    }

    @Test
    public void testTypeReference() throws NoSuchFieldException {
        Type type1 = TypeReferenceTestClass.class.getDeclaredField("field").getGenericType();
        Type type2 = new TypeReference<Map<List<?>, ?>>(){}.getType();
        Assert.assertTrue(type1.equals(type2));
    }

    private static class TypeReferenceTestClass {
        private Map<List<?>, ?> field;
    }
}
