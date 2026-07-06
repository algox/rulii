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
package org.rulii.test.text;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.text.ParameterInfo;

/**
 * Tests for ParameterInfo.
 *
 * @author Max Arulananthan
 */
public class ParameterInfoTest {

    public ParameterInfoTest() {
        super();
    }

    @Test
    public void testEquals_sameIndexOutsideIntegerCacheRange_areEqual() {
        // Integer autoboxing only caches -128..127; two separately-constructed ParameterInfo
        // objects with an index outside that range must still compare equal by value.
        ParameterInfo a = new ParameterInfo(200, "x", "one");
        ParameterInfo b = new ParameterInfo(200, "y", "two");

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEquals_differentIndex_areNotEqual() {
        ParameterInfo a = new ParameterInfo(200, "x", "one");
        ParameterInfo b = new ParameterInfo(201, "x", "one");

        Assertions.assertNotEquals(a, b);
    }

    @Test
    public void testCompareTo_ordersByIndex() {
        ParameterInfo a = new ParameterInfo(1, "a", null);
        ParameterInfo b = new ParameterInfo(2, "b", null);

        Assertions.assertTrue(a.compareTo(b) < 0);
        Assertions.assertTrue(b.compareTo(a) > 0);
        Assertions.assertEquals(0, a.compareTo(new ParameterInfo(1, "c", null)));
    }
}
