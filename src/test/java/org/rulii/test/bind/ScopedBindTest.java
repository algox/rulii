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
package org.rulii.test.bind;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.*;
import org.rulii.util.TypeReference;

import java.math.BigDecimal;
import java.util.*;

/**
 * Tests for Scoped Bindings.
 *
 * @author Max Arulananthan
 */
public class ScopedBindTest {

    public ScopedBindTest() {
        super();
    }

    @Test
    public void testCreateBindings() {
        Bindings scopedBindings = Bindings.builder().scoped();
        Assertions.assertInstanceOf(DefaultScopedBindings.class, scopedBindings);
    }

    @Test
    public void bindTest1() {
        Bindings bindings = Bindings.builder().scoped();
        Binding<String> binding = Binding.builder().with("key1").value("Hello World!").build();
        bindings.bind(binding);
        Binding<String> match = bindings.getBinding("key1");
        Assertions.assertEquals(match, binding);
    }

    @Test
    public void bindTest2() {
        Bindings bindings = Bindings.builder().scoped();
        Binding<String> binding = Binding.builder().with(key1 -> "hello world!").build();
        bindings.bind(binding);
        Binding<String> match = bindings.getBinding("key1");
        Assertions.assertEquals(match, binding);
    }

    @Test
    public void bindTest3() {
        Bindings bindings = Bindings.builder().scoped();
        Binding<Integer> b = bindings.bind("key1", 100);
        Binding<Integer> match = bindings.getBinding("key1");
        Assertions.assertEquals("key1", match.getName());
        Assertions.assertEquals(100, match.getValue());
        Assertions.assertEquals(match.getType(), Integer.class);
        Assertions.assertEquals(b, match);
    }

    @Test
    public void bindTest4() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("key1", int.class);
        Binding<Integer> match = bindings.getBinding("key1");
        Assertions.assertEquals("key1", match.getName());
        Assertions.assertEquals(0, match.getValue());
        Assertions.assertEquals(match.getType(), int.class);
    }

    @Test
    public void bindTest5() {
        Bindings bindings = Bindings.builder().scoped();
        Binding<List<Integer>> b = bindings.bind("x", new TypeReference<List<Integer>>() {});
        Binding<List<Integer>> match = bindings.getBinding("x");
        Assertions.assertEquals("x", match.getName());
        Assertions.assertEquals(match.getType(), new TypeReference<List<Integer>>() {
        }.getType());
        Assertions.assertNull(match.getValue());
        Assertions.assertEquals(b, match);
    }

    @Test
    public void bindTest6() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", int.class, 250);
        Binding<Integer> match = bindings.getBinding("x");
        Assertions.assertEquals("x", match.getName());
        Assertions.assertEquals(match.getType(), int.class);
        Assertions.assertEquals(250, match.getValue());
    }

    @Test
    public void bindTest7() {
        List<Integer> values = new ArrayList<>();
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", new TypeReference<List<Integer>>() {}, values);
        Binding<List<Integer>> match = bindings.getBinding("x");
        Assertions.assertEquals("x", match.getName());
        Assertions.assertEquals(match.getType(), new TypeReference<List<Integer>>() {
        }.getType());
        Assertions.assertEquals(match.getValue(), values);
    }

    @Test
    public void bindTest8() {
        Bindings bindings = Bindings.builder().scoped();
        Assertions.assertEquals(0, bindings.size());
        bindings.bind(Binding.builder().with(key1 -> "hello world!").build());
        Assertions.assertEquals(1, bindings.size());
        bindings.bind(Binding.builder().with(key2 -> 25).build());
        Assertions.assertEquals(2, bindings.size());
        bindings.bind(Binding.builder().with(key3 -> new BigDecimal("100.00")).build());
        Assertions.assertEquals(3, bindings.size());
    }

    @Test
    public void bindTest9() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind(Binding.builder().with(key1 -> "hello world!").build());
        bindings.bind(Binding.builder().with(key2 -> 25).build());
        bindings.bind(Binding.builder().with(key3 -> new BigDecimal("100.00")).build());
        Assertions.assertEquals(3, bindings.size());
    }

    @Test
    public void bindTest10() {
        Assertions.assertThrows(BindingAlreadyExistsException.class, () -> {
            Bindings bindings = Bindings.builder().scoped();
            bindings.bind("x", int.class, 250);
            bindings.bind("x", String.class, "Hello world");
        });
    }

    @Test
    public void bindTest11() {
        Bindings bindings = Bindings.builder().scoped();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<Integer> binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);

        Binding<Integer> match = bindings.getBinding("X");
        Assertions.assertEquals(match, binding2);
        match = bindings.getBinding("key");
        Assertions.assertNull(match);
    }

    @Test
    public void bindTest12() {
        Bindings bindings = Bindings.builder().scoped();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<Integer> binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding<Map<List<Integer>, String>> binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {}).build();
        bindings.bind(binding3);

        Assertions.assertTrue(bindings.contains("x"));
        Assertions.assertTrue(bindings.contains("X"));
        Assertions.assertTrue(bindings.contains("X", Integer.class));
        Assertions.assertTrue(bindings.contains("x", String.class));
        Assertions.assertTrue(bindings.contains("y", new TypeReference<Map<List<Integer>, String>>() {}));
        Assertions.assertFalse(bindings.contains("a"));
    }

    @Test
    public void bindTest13() {
        Bindings bindings = Bindings.builder().scoped();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<Integer> binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding<Map<List<Integer>, String>> binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {
        }).build();
        bindings.bind(binding3);

        Binding<String> match1 = bindings.getBinding("x");
        Binding<Integer> match2 = bindings.getBinding("X", Integer.class);
        Binding<Map<List<Integer>, String>> match3 = bindings.getBinding("y", new TypeReference<Map<List<Integer>, String>>() {
        }.getType());
        Binding<?> match4 = bindings.getBinding("a");
        Binding<Integer> match5 = bindings.getBinding("a", Integer.class);
        Binding<Map<List<Integer>, String>> match6 = bindings.getBinding("a", new TypeReference<Map<List<Integer>, String>>() {
        }.getType());
        Assertions.assertEquals(match1, binding1);
        Assertions.assertEquals(match2, binding2);
        Assertions.assertEquals(match3, binding3);
        Assertions.assertNull(match4);
        Assertions.assertNull(match5);
        Assertions.assertNull(match6);
    }

    @Test
    public void bindTest14() {
        Bindings bindings = Bindings.builder().scoped();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<String> binding2 = Binding.builder().with("X").value("101").build();
        bindings.bind(binding2);
        Binding<Map<List<Integer>, String>> binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {}).build();
        bindings.bind(binding3);
        Binding<Map<List<Integer>, String>> binding4 = Binding.builder().with("z").type(new TypeReference<Map<String, String>>() {}).build();
        bindings.bind(binding4);
        Binding<ArrayList<Integer>> binding5 = Binding.builder().with("a").type(new TypeReference<ArrayList<Integer>>() {}).value(new ArrayList<>()).build();
        bindings.bind(binding5);

        List<Binding<String>> matches1 = bindings.getBindings(String.class);
        Assertions.assertTrue(matches1.contains(binding1));
        Assertions.assertTrue(matches1.contains(binding2));
        Assertions.assertFalse(matches1.contains(binding3));

        List<Binding<Map<?,?>>> matches2 = bindings.getBindings(Map.class);
        Assertions.assertTrue(matches2.contains(binding3));
        List<Binding<Map<?, ?>>> matches3 = bindings.getBindings(new TypeReference<Map<?, ?>>() {}.getType());
        Assertions.assertTrue(matches3.size() == 2 && matches3.contains(binding3) && matches3.contains(binding4));
        List<Binding<Map<List<Integer>, String>>> matches4 = bindings.getBindings(new TypeReference<Map<List<Integer>, String>>() {}.getType());
        Assertions.assertTrue(matches4.size() == 1 && matches4.contains(binding3));
        List<Binding<List<Integer>>> matches5 = bindings.getBindings(new TypeReference<List<Integer>>() {}.getType());
        Assertions.assertTrue(matches5.size() == 1 && matches5.contains(binding5));
    }

    @Test
    public void bindTest15() {
        List<Integer> values = new ArrayList<>();
        values.add(1);
        values.add(2);
        values.add(3);

        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", String.class, "Hello World!");
        bindings.bind("y", List.class, values);

        Assertions.assertEquals("Hello World!", bindings.getValue("x"));
        Assertions.assertEquals(bindings.getValue("y"), values);
    }

    @Test
    public void bindTest16() {
        Assertions.assertThrows(NoSuchBindingException.class, () -> {
            Bindings bindings = Bindings.builder().scoped();
            bindings.getValue("x");
        });
    }

    @Test
    public void bindTest17() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", String.class, "Hello World!");
        Assertions.assertEquals("Hello World!", bindings.getValue("x"));
        bindings.setValue("x", "new value");
        Assertions.assertEquals("new value", bindings.getValue("x"));
        bindings.bind("y", List.class);
        bindings.setValue("y", new ArrayList<>());
        Assertions.assertEquals(bindings.getValue("y"), new ArrayList<>());
    }

    @Test
    public void bindTest18() {
        Assertions.assertThrows(InvalidBindingException.class, () -> {
            Bindings bindings = Bindings.builder().scoped();
            bindings.bind("x", String.class, "Hello World!");
            bindings.setValue("x", 123);
        });
    }

    @Test
    public void bindTest19() {
        List<Integer> values = new ArrayList<>();
        values.add(1);
        values.add(2);
        values.add(3);

        Bindings bindings = Bindings.builder().scoped();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<Integer> binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding<List<Integer>> binding3 = Binding.builder().with("y").value(values).build();
        bindings.bind(binding3);
        Binding<?> binding4 = Binding.builder().with("z").build();
        bindings.bind(binding4);

        Map<String, ?> bindingsMap = bindings.asMap();
        Assertions.assertEquals(bindings.size(), bindingsMap.size());

        for (Binding<?> binding : bindings) {
            Assertions.assertEquals(binding.getValue(), bindingsMap.get(binding.getName()));
        }
    }

    @Test
    public void bindTest20() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", int.class, 250);
        bindings.bind("y", Integer.class, 100);

        List<Binding<Integer>> matches = bindings.getBindings(Integer.class);
        Assertions.assertEquals(2, matches.size());
    }

    @Test
    public void bindTest21() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("key1", String.class, "value");
        Assertions.assertEquals("value", bindings.getValue("key1"));
        bindings.addScope();
        bindings.bind(Binding.builder().with("key1").type(String.class).value("value2").build());
        Assertions.assertEquals("value2", bindings.getValue("key1"));
        Assertions.assertEquals(2, bindings.size());
        bindings.removeScope();
        Assertions.assertEquals("value", bindings.getValue("key1"));
        Assertions.assertEquals(1, bindings.size());
    }

    @Test
    public void bindTest22() {
        Assertions.assertThrows(CannotRemoveRootScopeException.class, () -> {
            ScopedBindings bindings = Bindings.builder().scoped();
            bindings.removeScope();
        });
    }

    @Test
    public void bindTest23() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("x", Integer.class);
        Assertions.assertEquals(1, bindings.size());
        bindings.addScope();
        Assertions.assertEquals(0, bindings.getCurrentBindings().size());
        bindings.removeScope();
        Assertions.assertEquals(1, bindings.size());
    }

    @Test
    public void bindTest24() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("x", int.class);
        Assertions.assertEquals(0, (int) bindings.getValue("x"));
        bindings.addScope().getBindings().bind("x", 24);
        Assertions.assertEquals(24, (int) bindings.getValue("x"));
        bindings.removeScope();
        Assertions.assertEquals(0, (int) bindings.getValue("x"));
    }

    @Test
    public void bindTest25() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("x", int.class);
        bindings.addScope().getBindings().bind("x", "Hello World!");
        Binding<String> match1 = bindings.getBinding("x", String.class);
        Assertions.assertTrue(match1.getName().equals("x") && String.class.equals(match1.getType()));
        bindings.removeScope();
        Binding<Integer> match2 = bindings.getBinding("x", int.class);
        Assertions.assertTrue(match2.getName().equals("x") && int.class.equals(match2.getType()));
    }

    @Test
    public void bindTest26() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", new ArrayList<>());
        bindings.addScope().getBindings()
                .bind("b", new HashSet<>());
        bindings.addScope().getBindings()
                .bind("c", new Vector<>());
        List<Binding<List<?>>> matches1 = bindings.getBindings(List.class);
        Assertions.assertEquals(1, matches1.size());
        List<Binding<List<?>>> matches2 = bindings.getBindings(new TypeReference<List<?>>() {}.getType());
        Assertions.assertEquals(1, matches2.size());
        List<Binding<List<?>>> matches3 = bindings.getAllBindings(new TypeReference<List<?>>() {}.getType());
        Assertions.assertEquals(2, matches3.size());
        List<Binding<Collection<?>>> matches4 = bindings.getAllBindings(new TypeReference<Collection<?>>() {}.getType());
        Assertions.assertEquals(3, matches4.size());
        bindings.removeScope();
        matches4 = bindings.getAllBindings(new TypeReference<Collection<?>>() {}.getType());
        Assertions.assertEquals(2, matches4.size());
        bindings.removeScope();
        matches4 = bindings.getAllBindings(new TypeReference<Collection<?>>() {}.getType());
        Assertions.assertEquals(1, matches4.size());
    }

    @Test
    public void bindTest27() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", new ArrayList<>());
        bindings.addScope().getBindings()
                .bind("b", new HashSet<>());
        bindings.addScope().getBindings()
                .bind("c", new BigDecimal("100"));
        List<Binding<List<?>>> matches1 = bindings.getBindings(List.class);
        Assertions.assertTrue(matches1.size() == 1 && matches1.stream().findFirst().get().getName().equals("a"));
    }

    @Test
    public void bindTest28() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", new ArrayList<>());
        bindings.addScope().getBindings()
                .bind("b", new HashSet<>());
        bindings.addScope().getBindings()
                .bind("c", new BigDecimal("100"));
        Map<String, ?> map = bindings.asMap();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Assertions.assertEquals(bindings.getValue(entry.getKey()), entry.getValue());
        }
    }

    @Test
    public void bindTest29() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", new ArrayList<>());
        bindings.addScope().getBindings()
                .bind("b", new HashSet<>());
        bindings.addScope().getBindings()
                .bind("a", new BigDecimal("100"));
        Assertions.assertEquals(3, bindings.size());
        Assertions.assertEquals(2, bindings.uniqueSize());
    }

    @Test
    public void bindTest30() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", new ArrayList<>());
        bindings.addScope().getBindings().bind("b", new HashSet<>());
        bindings.addScope().getBindings().bind("a", new BigDecimal("100"));

        ScopedBindings immutableBindings = bindings.asImmutable();
        Assertions.assertEquals(3, immutableBindings.size());
        Assertions.assertEquals(2, immutableBindings.uniqueSize());
    }

    @Test
    public void bindTest31() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            ScopedBindings bindings = Bindings.builder().scoped().asImmutable();
            bindings.bind("test", Integer.class);
        });
    }

    @Test
    public void bindTest32() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.addScope("scope-1");
        bindings.addScope("scope-2");
        bindings.addScope("scope-3");
        Assertions.assertEquals(4, bindings.getScopeSize());
    }

    @Test
    public void bindTest33() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.addScope("scope-1");
        bindings.addScope("scope-2");
        bindings.addScope("scope-3");
        Assertions.assertTrue(bindings.containsScope(ScopedBindings.ROOT_SCOPE));
        Assertions.assertTrue(bindings.containsScope("scope-1"));
        Assertions.assertTrue(bindings.containsScope("scope-2"));
        Assertions.assertTrue(bindings.containsScope("scope-3"));
    }

    @Test
    public void bindTest34() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.addScope("scope-1");
        bindings.addScope("scope-2");
        bindings.addScope("scope-3");
        Assertions.assertTrue(bindings.containsScope(ScopedBindings.ROOT_SCOPE));

        bindings.removeScope("scope-3");
        Assertions.assertFalse(bindings.containsScope("scope-3"));
        Assertions.assertTrue(bindings.containsScope("scope-1"));
        Assertions.assertTrue(bindings.containsScope("scope-2"));

        bindings.removeScope("scope-2");
        Assertions.assertFalse(bindings.containsScope("scope-3"));
        Assertions.assertFalse(bindings.containsScope("scope-2"));
        Assertions.assertTrue(bindings.containsScope("scope-1"));

        bindings.removeScope("scope-1");
        Assertions.assertFalse(bindings.containsScope("scope-3"));
        Assertions.assertFalse(bindings.containsScope("scope-2"));
        Assertions.assertFalse(bindings.containsScope("scope-1"));
    }

    @Test
    public void bindTest35() {
        ScopedBindings bindings = Bindings.builder().scoped();
        Bindings bindings1 = bindings.addScope("scope-1").getBindings();
        Bindings bindings2 = bindings.addScope("scope-2").getBindings();
        Bindings bindings3 = bindings.addScope("scope-3").getBindings();

        Assertions.assertSame(bindings3, bindings.getCurrentBindings());
        Assertions.assertSame(bindings2, bindings.getParentScope().getBindings());
        bindings.removeScope();
        Assertions.assertSame(bindings2, bindings.getCurrentBindings());
        Assertions.assertSame(bindings1, bindings.getParentScope().getBindings());
        bindings.removeScope();
        Assertions.assertSame(bindings1, bindings.getCurrentBindings());
        Assertions.assertNull(bindings.getParentScope());
    }

    @Test
    public void bindTest36() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.addScope("scope-1");
        bindings.addScope("scope-2");
        bindings.addScope("scope-3");

        bindings.removeScope("scope-1");
        Assertions.assertFalse(bindings.containsScope("scope-3"));
        Assertions.assertFalse(bindings.containsScope("scope-2"));
        Assertions.assertFalse(bindings.containsScope("scope-1"));
    }

    /**
     * The testCurrentBindings method tests the `getCurrentBindings` method
     * for the condition when current bindings exist.
     */
    @Test
    void bindTest37() {
        // setup
        ScopedBindings scopedBindings = Bindings.builder().scoped();
        // retrieve bindings
        Bindings bindings = scopedBindings.getCurrentBindings();
        // assertions
        Assertions.assertNotNull(bindings);
    }

    /**
     * This test ensures `getCurrentScope` is returning the current working scope.
     */
    @Test
    void bindTest38() {
        ScopedBindings bindings = Bindings.builder().scoped("scope1");
        NamedScope namedScope = bindings.getCurrentScope();
        Assertions.assertEquals("scope1", namedScope.getName(), "getCurrentScope should return the current scope");
    }

    @Test
    public void testAsMapBinding() {
        // Arrange
        ScopedBindings bindings = Bindings.builder().scoped("scope1");
        bindings.addScope("scope_test");
        bindings.bind("binding_test", "test_value");
        // Act
        Map<String, ?> result = bindings.asMap();
        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.containsKey("binding_test"));
        Assertions.assertEquals("test_value", result.get("binding_test"));
    }
}
