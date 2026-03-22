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
package org.rulii.test.script.js;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.script.ScriptParameter;

/**
 * Unit tests for ScriptParameter.
 */
public class ScriptParameterTest {

    @Test
    public void testCreateWithNameAndType() {
        ScriptParameter param = new ScriptParameter("age", Integer.class);
        Assertions.assertEquals("age", param.getName());
        Assertions.assertEquals(Integer.class, param.getType());
    }

    @Test
    public void testCreateWithStringType() {
        ScriptParameter param = new ScriptParameter("name", String.class);
        Assertions.assertEquals("name", param.getName());
        Assertions.assertEquals(String.class, param.getType());
    }

    @Test
    public void testCreateWithPrimitiveType() {
        ScriptParameter param = new ScriptParameter("flag", boolean.class);
        Assertions.assertEquals("flag", param.getName());
        Assertions.assertEquals(boolean.class, param.getType());
    }

    @Test
    public void testNullNameThrows() {
        Assertions.assertThrows(Exception.class, () -> new ScriptParameter(null, Integer.class));
    }

    @Test
    public void testEmptyNameThrows() {
        Assertions.assertThrows(Exception.class, () -> new ScriptParameter("", Integer.class));
    }

    @Test
    public void testNullTypeThrows() {
        Assertions.assertThrows(Exception.class, () -> new ScriptParameter("x", null));
    }

    @Test
    public void testToStringContainsNameAndType() {
        ScriptParameter param = new ScriptParameter("score", Double.class);
        String str = param.toString();
        Assertions.assertTrue(str.contains("score"));
        Assertions.assertTrue(str.contains("Double"));
    }
}