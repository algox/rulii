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
import org.rulii.script.ScriptOptions;

/**
 * Unit tests for ScriptOptions.
 */
public class ScriptOptionsTest {

    @Test
    public void testDefaultBindingsName() {
        Assertions.assertEquals("ctx", ScriptOptions.DEFAULT.bindingsName());
    }

    @Test
    public void testCustomBindingsName() {
        ScriptOptions options = new ScriptOptions("bindings");
        Assertions.assertEquals("bindings", options.bindingsName());
    }

    @Test
    public void testNullBindingsNameThrows() {
        Assertions.assertThrows(Exception.class, () -> new ScriptOptions(null));
    }

    @Test
    public void testEmptyBindingsNameThrows() {
        Assertions.assertThrows(Exception.class, () -> new ScriptOptions(""));
    }

    @Test
    public void testDefaultConstantIsNotNull() {
        Assertions.assertNotNull(ScriptOptions.DEFAULT);
    }

    @Test
    public void testEquality() {
        ScriptOptions a = new ScriptOptions("ctx");
        ScriptOptions b = new ScriptOptions("ctx");
        Assertions.assertEquals(a, b);
    }

    @Test
    public void testInequality() {
        ScriptOptions a = new ScriptOptions("ctx");
        ScriptOptions b = new ScriptOptions("bindings");
        Assertions.assertNotEquals(a, b);
    }
}
