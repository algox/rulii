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
package org.rulii.test.bind;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Bindings;
import org.rulii.bind.ScopedBindings;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Tests for the Map<String, Object> interface now implemented by
 * DefaultBindings, DefaultScopedBindings, ImmutableBindings, and ImmutableScopedBindings.
 *
 * Each group of tests is repeated for all four implementations to confirm
 * contract consistency.
 */
public class BindingsAsMapTest {

    // ===================================================================
    // Helpers
    // ===================================================================

    private Map<String, Object> standardMap() {
        Bindings b = Bindings.builder().standard();
        b.bind("a", String.class, "alpha");
        b.bind("b", int.class,    2);
        b.bind("c", Boolean.class, true);
        return b.asMap();
    }

    private Map<String, Object> scopedMap() {
        ScopedBindings b = Bindings.builder().scoped();
        b.bind("a", String.class, "alpha");
        b.bind("b", int.class,    2);
        b.bind("c", Boolean.class, true);
        return b.asMap();
    }

    private Map<String, Object> immutableStandardMap() {
        Bindings b = Bindings.builder().standard();
        b.bind("a", String.class, "alpha");
        b.bind("b", int.class,    2);
        b.bind("c", Boolean.class, true);
        return b.asImmutable().asMap();
    }

    private Map<String, Object> immutableScopedMap() {
        ScopedBindings b = Bindings.builder().scoped();
        b.bind("a", String.class, "alpha");
        b.bind("b", int.class,    2);
        b.bind("c", Boolean.class, true);
        return b.asImmutable().asMap();
    }

    // ===================================================================
    // asMap() returns `this` — same reference
    // ===================================================================

    @Test
    public void testAsMapReturnsSameReferenceForStandard() {
        Bindings b = Bindings.builder().standard();
        Assertions.assertSame(b.asMap(), b.asMap());
    }

    @Test
    public void testAsMapReturnsSameReferenceForScoped() {
        ScopedBindings b = Bindings.builder().scoped();
        Assertions.assertSame(b.asMap(), b.asMap());
    }

    @Test
    public void testAsMapReturnsSameReferenceForImmutableStandard() {
        Bindings b = Bindings.builder().standard().asImmutable();
        Assertions.assertSame(b.asMap(), b.asMap());
    }

    @Test
    public void testAsMapReturnsSameReferenceForImmutableScoped() {
        ScopedBindings b = Bindings.builder().scoped().asImmutable();
        Assertions.assertSame(b.asMap(), b.asMap());
    }

    // ===================================================================
    // size()
    // ===================================================================

    @Test
    public void testSizeStandard()         { Assertions.assertEquals(3, standardMap().size()); }
    @Test
    public void testSizeScoped()           { Assertions.assertEquals(3, scopedMap().size()); }
    @Test
    public void testSizeImmutableStandard(){ Assertions.assertEquals(3, immutableStandardMap().size()); }
    @Test
    public void testSizeImmutableScoped()  { Assertions.assertEquals(3, immutableScopedMap().size()); }

    // ===================================================================
    // isEmpty()
    // ===================================================================

    @Test
    public void testIsEmptyReturnsTrueForEmpty() {
        Assertions.assertTrue(Bindings.builder().standard().asMap().isEmpty());
        Assertions.assertTrue(Bindings.builder().scoped().asMap().isEmpty());
    }

    @Test
    public void testIsEmptyReturnsFalseWhenPopulated() {
        Assertions.assertFalse(standardMap().isEmpty());
        Assertions.assertFalse(scopedMap().isEmpty());
        Assertions.assertFalse(immutableStandardMap().isEmpty());
        Assertions.assertFalse(immutableScopedMap().isEmpty());
    }

    // ===================================================================
    // containsKey(Object)
    // ===================================================================

    @Test
    public void testContainsKeyPresentStandard()         { Assertions.assertTrue(standardMap().containsKey("a")); }
    @Test
    public void testContainsKeyPresentScoped()           { Assertions.assertTrue(scopedMap().containsKey("b")); }
    @Test
    public void testContainsKeyPresentImmutableStandard(){ Assertions.assertTrue(immutableStandardMap().containsKey("c")); }
    @Test
    public void testContainsKeyPresentImmutableScoped()  { Assertions.assertTrue(immutableScopedMap().containsKey("a")); }

    @Test
    public void testContainsKeyAbsentStandard()         { Assertions.assertFalse(standardMap().containsKey("z")); }
    @Test
    public void testContainsKeyAbsentScoped()           { Assertions.assertFalse(scopedMap().containsKey("z")); }
    @Test
    public void testContainsKeyAbsentImmutableStandard(){ Assertions.assertFalse(immutableStandardMap().containsKey("z")); }
    @Test
    public void testContainsKeyAbsentImmutableScoped()  { Assertions.assertFalse(immutableScopedMap().containsKey("z")); }

    @Test
    public void testContainsKeyNullThrows() {
        Assertions.assertThrows(Exception.class, () -> standardMap().containsKey(null));
        Assertions.assertThrows(Exception.class, () -> scopedMap().containsKey(null));
    }

    // ===================================================================
    // containsValue(Object)
    // ===================================================================

    @Test
    public void testContainsValuePresentStandard()         { Assertions.assertTrue(standardMap().containsValue("alpha")); }
    @Test
    public void testContainsValuePresentScoped()           { Assertions.assertTrue(scopedMap().containsValue(2)); }
    @Test
    public void testContainsValuePresentImmutableStandard(){ Assertions.assertTrue(immutableStandardMap().containsValue(true)); }
    @Test
    public void testContainsValuePresentImmutableScoped()  { Assertions.assertTrue(immutableScopedMap().containsValue("alpha")); }

    @Test
    public void testContainsValueAbsent() {
        Assertions.assertFalse(standardMap().containsValue("not-there"));
        Assertions.assertFalse(scopedMap().containsValue(999));
        Assertions.assertFalse(immutableStandardMap().containsValue("not-there"));
        Assertions.assertFalse(immutableScopedMap().containsValue("not-there"));
    }

    @Test
    public void testContainsNullValue() {
        Bindings b = Bindings.builder().standard();
        b.bind("n", String.class, null);
        Assertions.assertTrue(b.asMap().containsValue(null));
    }

    // ===================================================================
    // get(Object)
    // ===================================================================

    @Test
    public void testGetReturnsValueStandard()         { Assertions.assertEquals("alpha", standardMap().get("a")); }
    @Test
    public void testGetReturnsValueScoped()           { Assertions.assertEquals(2,       scopedMap().get("b")); }
    @Test
    public void testGetReturnsValueImmutableStandard(){ Assertions.assertEquals(true,    immutableStandardMap().get("c")); }
    @Test
    public void testGetReturnsValueImmutableScoped()  { Assertions.assertEquals("alpha", immutableScopedMap().get("a")); }

    @Test
    public void testGetAbsentKeyReturnsNull() {
        Assertions.assertNull(standardMap().get("missing"));
        Assertions.assertNull(scopedMap().get("missing"));
        Assertions.assertNull(immutableStandardMap().get("missing"));
        Assertions.assertNull(immutableScopedMap().get("missing"));
    }

    @Test
    public void testGetNullKeyThrows() {
        Assertions.assertThrows(Exception.class, () -> standardMap().get(null));
        Assertions.assertThrows(Exception.class, () -> scopedMap().get(null));
    }

    @Test
    public void testGetReflectsLiveChanges() {
        Bindings b = Bindings.builder().standard();
        b.bind("x", String.class, "before");
        Map<String, Object> map = b.asMap();
        b.setValue("x", "after");
        Assertions.assertEquals("after", map.get("x"));
    }

    @Test
    public void testGetReflectsLiveChangesScoped() {
        ScopedBindings b = Bindings.builder().scoped();
        b.bind("x", String.class, "before");
        Map<String, Object> map = b.asMap();
        b.setValue("x", "after");
        Assertions.assertEquals("after", map.get("x"));
    }

    // ===================================================================
    // put(String, Object) — sets existing or binds new
    // ===================================================================

    @Test
    public void testPutUpdatesExistingBinding() {
        Bindings b = Bindings.builder().standard();
        b.bind("x", String.class, "old");
        Map<String, Object> map = b.asMap();
        map.put("x", "new");
        Assertions.assertEquals("new", b.getValue("x"));
    }

    @Test
    public void testPutCreatesNewBinding() {
        Bindings b = Bindings.builder().standard();
        Map<String, Object> map = b.asMap();
        map.put("fresh", "value");
        Assertions.assertTrue(b.contains("fresh"));
        Assertions.assertEquals("value", b.getValue("fresh"));
    }

    @Test
    public void testPutReturnsPreviousValue() {
        Bindings b = Bindings.builder().standard();
        b.bind("score", int.class, 10);
        Object previous = b.asMap().put("score", 20);
        Assertions.assertEquals(10, ((Number) previous).intValue());
    }

    @Test
    public void testPutReturnsNullForNewBinding() {
        Bindings b = Bindings.builder().standard();
        Object previous = b.asMap().put("brandNew", "hello");
        Assertions.assertNull(previous);
    }

    @Test
    public void testPutOnScopedBindings() {
        ScopedBindings b = Bindings.builder().scoped();
        b.bind("y", int.class, 5);
        b.asMap().put("y", 50);
        Assertions.assertEquals(50, ((Number) b.getValue("y")).intValue());
    }

    @Test
    public void testPutOnImmutableStandardThrows() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutableStandardMap().put("x", "v"));
    }

    @Test
    public void testPutOnImmutableScopedThrows() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutableScopedMap().put("x", "v"));
    }

    // ===================================================================
    // remove(Object) — always throws
    // ===================================================================

    @Test
    public void testRemoveThrowsOnStandard() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> standardMap().remove("a"));
    }

    @Test
    public void testRemoveThrowsOnScoped() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> scopedMap().remove("a"));
    }

    @Test
    public void testRemoveThrowsOnImmutableStandard() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutableStandardMap().remove("a"));
    }

    @Test
    public void testRemoveThrowsOnImmutableScoped() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutableScopedMap().remove("a"));
    }

    // ===================================================================
    // clear() — always throws
    // ===================================================================

    @Test
    public void testClearThrowsOnStandard()         { Assertions.assertThrows(UnsupportedOperationException.class, () -> standardMap().clear()); }
    @Test
    public void testClearThrowsOnScoped()           { Assertions.assertThrows(UnsupportedOperationException.class, () -> scopedMap().clear()); }
    @Test
    public void testClearThrowsOnImmutableStandard(){ Assertions.assertThrows(UnsupportedOperationException.class, () -> immutableStandardMap().clear()); }
    @Test
    public void testClearThrowsOnImmutableScoped()  { Assertions.assertThrows(UnsupportedOperationException.class, () -> immutableScopedMap().clear()); }

    // ===================================================================
    // putAll(Map)
    // ===================================================================

    @Test
    public void testPutAllAddsNewBindings() {
        Bindings b = Bindings.builder().standard();
        Map<String, Object> map = b.asMap();
        map.putAll(Map.of("x", 1, "y", 2));
        Assertions.assertTrue(b.contains("x"));
        Assertions.assertTrue(b.contains("y"));
    }

    @Test
    public void testPutAllUpdatesExistingBindings() {
        Bindings b = Bindings.builder().standard();
        b.bind("x", int.class, 1);
        b.asMap().putAll(Map.of("x", 99));
        Assertions.assertEquals(99, ((Number) b.getValue("x")).intValue());
    }

    @Test
    public void testPutAllOnImmutableThrows() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> immutableStandardMap().putAll(Map.of("z", "v")));
    }

    // ===================================================================
    // keySet()
    // ===================================================================

    @Test
    public void testKeySetContainsAllNamesStandard() {
        Set<String> keys = standardMap().keySet();
        Assertions.assertTrue(keys.contains("a"));
        Assertions.assertTrue(keys.contains("b"));
        Assertions.assertTrue(keys.contains("c"));
        Assertions.assertEquals(3, keys.size());
    }

    @Test
    public void testKeySetContainsAllNamesScoped() {
        Set<String> keys = scopedMap().keySet();
        Assertions.assertEquals(3, keys.size());
        Assertions.assertTrue(keys.contains("a"));
    }

    @Test
    public void testKeySetContainsAllNamesImmutableStandard() {
        Set<String> keys = immutableStandardMap().keySet();
        Assertions.assertEquals(3, keys.size());
        Assertions.assertTrue(keys.contains("b"));
    }

    @Test
    public void testKeySetContainsAllNamesImmutableScoped() {
        Set<String> keys = immutableScopedMap().keySet();
        Assertions.assertEquals(3, keys.size());
        Assertions.assertTrue(keys.contains("c"));
    }

    @Test
    public void testKeySetIsEmptyForEmptyBindings() {
        Assertions.assertTrue(Bindings.builder().standard().asMap().keySet().isEmpty());
    }

    // ===================================================================
    // values()
    // ===================================================================

    @Test
    public void testValuesContainsAllValuesStandard() {
        Collection<Object> vals = standardMap().values();
        Assertions.assertTrue(vals.contains("alpha"));
        Assertions.assertTrue(vals.contains(2));
        Assertions.assertTrue(vals.contains(true));
    }

    @Test
    public void testValuesContainsAllValuesScoped() {
        Collection<Object> vals = scopedMap().values();
        Assertions.assertTrue(vals.contains("alpha"));
        Assertions.assertTrue(vals.contains(2));
    }

    @Test
    public void testValuesContainsAllValuesImmutableStandard() {
        Collection<Object> vals = immutableStandardMap().values();
        Assertions.assertEquals(3, vals.size());
        Assertions.assertTrue(vals.contains(true));
    }

    @Test
    public void testValuesContainsAllValuesImmutableScoped() {
        Collection<Object> vals = immutableScopedMap().values();
        Assertions.assertEquals(3, vals.size());
        Assertions.assertTrue(vals.contains("alpha"));
    }

    // ===================================================================
    // entrySet()
    // ===================================================================

    @Test
    public void testEntrySetSizeMatchesBindingSizeStandard() {
        Set<Map.Entry<String, Object>> entries = standardMap().entrySet();
        Assertions.assertEquals(3, entries.size());
    }

    @Test
    public void testEntrySetSizeMatchesBindingSizeScoped() {
        Set<Map.Entry<String, Object>> entries = scopedMap().entrySet();
        Assertions.assertEquals(3, entries.size());
    }

    @Test
    public void testEntrySetContainsCorrectPairsStandard() {
        Map<String, Object> map = standardMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Assertions.assertEquals(map.get(entry.getKey()), entry.getValue());
        }
    }

    @Test
    public void testEntrySetContainsCorrectPairsScoped() {
        Map<String, Object> map = scopedMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Assertions.assertEquals(map.get(entry.getKey()), entry.getValue());
        }
    }

    @Test
    public void testEntrySetContainsCorrectPairsImmutable() {
        Map<String, Object> map = immutableStandardMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Assertions.assertEquals(map.get(entry.getKey()), entry.getValue());
        }
    }

    @Test
    public void testEntrySetIsEmptyForEmptyBindings() {
        Assertions.assertTrue(Bindings.builder().standard().asMap().entrySet().isEmpty());
    }

    // ===================================================================
    // Scoped bindings — shadowing across scopes via Map view
    // ===================================================================

    @Test
    public void testScopedMapReturnsTopmostShadowedValue() {
        ScopedBindings b = Bindings.builder().scoped();
        b.bind("x", String.class, "root");
        b.addScope("inner");
        b.bind("x", String.class, "inner");
        // Map view should return the innermost (current scope) value
        Assertions.assertEquals("inner", b.asMap().get("x"));
    }

    @Test
    public void testScopedMapSizeCountsAllScopeBindings() {
        ScopedBindings b = Bindings.builder().scoped();
        b.bind("x", String.class, "root");
        b.addScope("inner");
        b.bind("y", String.class, "inner");
        // size() counts all bindings across all scopes
        Assertions.assertEquals(2, b.size());
    }

    @Test
    public void testScopedMapKeySetDeduplicatesShadowedNames() {
        ScopedBindings b = Bindings.builder().scoped();
        b.bind("x", String.class, "root");
        b.addScope("inner");
        b.bind("x", String.class, "inner");
        b.bind("y", String.class, "inner-only");
        // keySet() should deduplicate — "x" appears once
        Assertions.assertEquals(2, b.asMap().keySet().size());
    }
}
