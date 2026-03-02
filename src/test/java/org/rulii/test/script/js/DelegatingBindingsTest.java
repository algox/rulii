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
package org.rulii.test.script.js;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.script.DelegatingBindings;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Tests for {@link DelegatingBindings} – the JSR-223 Bindings bridge over rulii {@link Bindings}.
 *
 * @author Max Arulananthan
 * @since 1.2
 */
public class DelegatingBindingsTest {

    private Bindings ruliiBindings;
    private DelegatingBindings delegating;

    @BeforeEach
    void setup() {
        ruliiBindings = Bindings.builder().standard();
        delegating = new DelegatingBindings(ruliiBindings);
    }

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    @Test
    void constructorWithNullTargetThrowsException() {
        Assertions.assertThrows(Exception.class, () -> new DelegatingBindings(null));
    }

    // -----------------------------------------------------------------------
    // put / get
    // -----------------------------------------------------------------------

    @Test
    void putNewBindingCreatesItAndReturnsPreviousNull() {
        Object prev = delegating.put("x", 42);
        Assertions.assertNull(prev, "Should return null when no prior binding exists.");
        Assertions.assertEquals(42, ((Number) delegating.get("x")).intValue());
    }

    @Test
    void putExistingBindingUpdatesValueAndReturnsPrevious() {
        ruliiBindings.bind("score", 10);
        Object prev = delegating.put("score", 99);
        Assertions.assertEquals(10, ((Number) prev).intValue(), "Should return the old value.");
        Assertions.assertEquals(99, ((Number) delegating.get("score")).intValue(), "Binding should be updated.");
        Assertions.assertEquals(99, ((Number) ruliiBindings.getValue("score")).intValue(), "Underlying rulii binding should reflect update.");
    }

    @Test
    void putStringValue() {
        delegating.put("name", "Alice");
        Assertions.assertEquals("Alice", delegating.get("name"));
    }

    @Test
    void putNullValue() {
        delegating.put("ptr", null);
        Assertions.assertNull(delegating.get("ptr"));
    }

    @Test
    void getExistingKey() {
        ruliiBindings.bind("lang", "Java");
        Assertions.assertEquals("Java", delegating.get("lang"));
    }

    @Test
    void getNonExistingKeyThrowsException() {
        // DefaultBindings.getValue() throws NoSuchBindingException for unknown keys
        Assertions.assertThrows(Exception.class, () -> delegating.get("nosuchkey"));
    }

    @Test
    void getWithNullKeyThrowsException() {
        Assertions.assertThrows(Exception.class, () -> delegating.get(null));
    }

    // -----------------------------------------------------------------------
    // putAll
    // -----------------------------------------------------------------------

    @Test
    void putAllAddsAllEntries() {
        delegating.putAll(Map.of("p", 1, "q", 2, "r", 3));
        Assertions.assertEquals(1, ((Number) delegating.get("p")).intValue());
        Assertions.assertEquals(2, ((Number) delegating.get("q")).intValue());
        Assertions.assertEquals(3, ((Number) delegating.get("r")).intValue());
    }

    @Test
    void putAllWithNullMapThrowsException() {
        Assertions.assertThrows(Exception.class, () -> delegating.putAll(null));
    }

    // -----------------------------------------------------------------------
    // containsKey
    // -----------------------------------------------------------------------

    @Test
    void containsKeyReturnsTrueForExistingBinding() {
        ruliiBindings.bind("flag", true);
        Assertions.assertTrue(delegating.containsKey("flag"));
    }

    @Test
    void containsKeyReturnsFalseForMissingBinding() {
        Assertions.assertFalse(delegating.containsKey("missing"));
    }

    @Test
    void containsKeyWithNullThrowsException() {
        Assertions.assertThrows(Exception.class, () -> delegating.containsKey(null));
    }

    // -----------------------------------------------------------------------
    // containsValue
    // -----------------------------------------------------------------------

    @Test
    void containsValueReturnsTrueWhenValuePresent() {
        ruliiBindings.bind("greeting", "hello");
        Assertions.assertTrue(delegating.containsValue("hello"));
    }

    @Test
    void containsValueReturnsFalseWhenValueAbsent() {
        Assertions.assertFalse(delegating.containsValue("nothere"));
    }

    @Test
    void containsValueWithNullValue() {
        delegating.put("ptr", null);
        Assertions.assertTrue(delegating.containsValue(null));
    }

    // -----------------------------------------------------------------------
    // size / isEmpty
    // -----------------------------------------------------------------------

    @Test
    void sizeIsZeroForEmptyBindings() {
        Assertions.assertEquals(0, delegating.size());
    }

    @Test
    void sizeIncreasesWithEachNewBinding() {
        Assertions.assertEquals(0, delegating.size());
        ruliiBindings.bind("a", 1);
        Assertions.assertEquals(1, delegating.size());
        ruliiBindings.bind("b", 2);
        Assertions.assertEquals(2, delegating.size());
    }

    @Test
    void isEmptyReturnsTrueWhenNoBindings() {
        Assertions.assertTrue(delegating.isEmpty());
    }

    @Test
    void isEmptyReturnsFalseAfterBinding() {
        ruliiBindings.bind("x", 1);
        Assertions.assertFalse(delegating.isEmpty());
    }

    // -----------------------------------------------------------------------
    // keySet / values / entrySet
    // -----------------------------------------------------------------------

    @Test
    void keySetContainsAllBoundNames() {
        ruliiBindings.bind("a", 1);
        ruliiBindings.bind("b", 2);
        Set<String> keys = delegating.keySet();
        Assertions.assertTrue(keys.contains("a"));
        Assertions.assertTrue(keys.contains("b"));
        Assertions.assertEquals(2, keys.size());
    }

    @Test
    void valuesContainsAllBoundValues() {
        ruliiBindings.bind("x", 100);
        ruliiBindings.bind("y", 200);
        Collection<Object> values = delegating.values();
        Assertions.assertTrue(values.contains(100));
        Assertions.assertTrue(values.contains(200));
    }

    @Test
    void entrySetContainsNameValuePairs() {
        ruliiBindings.bind("k", "v");
        Set<Map.Entry<String, Object>> entries = delegating.entrySet();
        boolean found = entries.stream()
                .anyMatch(e -> "k".equals(e.getKey()) && "v".equals(e.getValue()));
        Assertions.assertTrue(found);
    }

    @Test
    void entrySetSizeMatchesBindingCount() {
        ruliiBindings.bind("one", 1);
        ruliiBindings.bind("two", 2);
        ruliiBindings.bind("three", 3);
        Assertions.assertEquals(3, delegating.entrySet().size());
    }

    // -----------------------------------------------------------------------
    // Unsupported operations
    // -----------------------------------------------------------------------

    @Test
    void removeThrowsUnsupportedOperationException() {
        ruliiBindings.bind("x", 1);
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> delegating.remove("x"));
    }

    @Test
    void clearThrowsUnsupportedOperationException() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> delegating.clear());
    }

    // -----------------------------------------------------------------------
    // toString / equals / hashCode (smoke tests)
    // -----------------------------------------------------------------------

    @Test
    void toStringIsNonNull() {
        Assertions.assertNotNull(delegating.toString());
    }

    @Test
    void equalsReflexive() {
        Assertions.assertEquals(delegating, delegating);
    }
}
