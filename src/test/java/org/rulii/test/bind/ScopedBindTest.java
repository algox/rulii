/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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

import org.rulii.bind.*;
import org.rulii.util.TypeReference;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(scopedBindings instanceof DefaultScopedBindings);
    }

    @Test
    public void bindTest1() {
        Bindings bindings = Bindings.builder().scoped();
        Binding binding = Binding.builder().with("key1").value("Hello World!").build();
        bindings.bind(binding);
        Binding match = bindings.getBinding("key1");
        Assert.assertTrue(match.equals(binding));
    }

    @Test
    public void bindTest2() {
        Bindings bindings = Bindings.builder().scoped();
        Binding binding = Binding.builder().with(key1 -> "hello world!").build();
        bindings.bind(binding);
        Binding match = bindings.getBinding("key1");
        Assert.assertTrue(match.equals(binding));
    }

    @Test
    public void bindTest3() {
        Bindings bindings = Bindings.builder().scoped();
        Binding b = bindings.bind("key1", 100);
        Binding match = bindings.getBinding("key1");
        Assert.assertTrue(match.getName().equals("key1"));
        Assert.assertTrue(match.getValue().equals(100));
        Assert.assertTrue(match.getType().equals(Integer.class));
        Assert.assertEquals(b, match);
    }

    @Test
    public void bindTest4() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("key1", int.class);
        Binding match = bindings.getBinding("key1");
        Assert.assertTrue(match.getName().equals("key1"));
        Assert.assertTrue(match.getValue().equals(0));
        Assert.assertTrue(match.getType().equals(int.class));
    }

    @Test
    public void bindTest5() {
        Bindings bindings = Bindings.builder().scoped();
        Binding b = bindings.bind("x", new TypeReference<List<Integer>>() {});
        Binding match = bindings.getBinding("x");
        Assert.assertTrue(match.getName().equals("x"));
        Assert.assertTrue(match.getType().equals(new TypeReference<List<Integer>>() {}.getType()));
        Assert.assertTrue(match.getValue() == null);
        Assert.assertEquals(b, match);
    }

    @Test
    public void bindTest6() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", int.class, 250);
        Binding match = bindings.getBinding("x");
        Assert.assertTrue(match.getName().equals("x"));
        Assert.assertTrue(match.getType().equals(int.class));
        Assert.assertTrue(match.getValue().equals(250));
    }

    @Test
    public void bindTest7() {
        List<Integer> values = new ArrayList<>();
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", new TypeReference<List<Integer>>() {}, values);
        Binding match = bindings.getBinding("x");
        Assert.assertTrue(match.getName().equals("x"));
        Assert.assertTrue(match.getType().equals(new TypeReference<List<Integer>>() {}.getType()));
        Assert.assertTrue(match.getValue().equals(values));
    }

    @Test
    public void bindTest8() {
        Bindings bindings = Bindings.builder().scoped();
        Assert.assertTrue(bindings.size() == 0);
        bindings.bind(Binding.builder().with(key1 -> "hello world!").build());
        Assert.assertTrue(bindings.size() == 1);
        bindings.bind(Binding.builder().with(key2 -> 25).build());
        Assert.assertTrue(bindings.size() == 2);
        bindings.bind(Binding.builder().with(key3 -> new BigDecimal("100.00")).build());
        Assert.assertTrue(bindings.size() == 3);
    }

    @Test
    public void bindTest9() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind(Binding.builder().with(key1 -> "hello world!").build());
        bindings.bind(Binding.builder().with(key2 -> 25).build());
        bindings.bind(Binding.builder().with(key3 -> new BigDecimal("100.00")).build());
        Assert.assertTrue(bindings.size() == 3);
    }

    @Test(expected = BindingAlreadyExistsException.class)
    public void bindTest10() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", int.class, 250);
        bindings.bind("x", String.class, "Hello world");
    }

    @Test
    public void bindTest11() {
        Bindings bindings = Bindings.builder().scoped();
        Binding binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);

        Binding<String> match = bindings.getBinding("X");
        Assert.assertTrue(match.equals(binding2));
        match = bindings.getBinding("key");
        Assert.assertTrue(match == null);
    }

    @Test
    public void bindTest12() {
        Bindings bindings = Bindings.builder().scoped();
        Binding binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {}).build();
        bindings.bind(binding3);

        Assert.assertTrue(bindings.contains("x"));
        Assert.assertTrue(bindings.contains("X"));
        Assert.assertTrue(bindings.contains("X", Integer.class));
        Assert.assertTrue(bindings.contains("x", String.class));
        Assert.assertTrue(bindings.contains("y", new TypeReference<Map<List<Integer>, String>>() {}));
        Assert.assertTrue(!bindings.contains("a"));
    }

    @Test
    public void bindTest13() {
        Bindings bindings = Bindings.builder().scoped();
        Binding binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {
        }).build();
        bindings.bind(binding3);

        Binding match1 = bindings.getBinding("x");
        Binding match2 = bindings.getBinding("X", Integer.class);
        Binding match3 = bindings.getBinding("y", new TypeReference<Map<List<Integer>, String>>() {
        }.getType());
        Binding match4 = bindings.getBinding("a");
        Binding match5 = bindings.getBinding("a", Integer.class);
        Binding match6 = bindings.getBinding("a", new TypeReference<Map<List<Integer>, String>>() {
        }.getType());
        Assert.assertTrue(match1.equals(binding1));
        Assert.assertTrue(match2.equals(binding2));
        Assert.assertTrue(match3.equals(binding3));
        Assert.assertTrue(match4 == null);
        Assert.assertTrue(match5 == null);
        Assert.assertTrue(match6 == null);
    }

    @Test
    public void bindTest14() {
        Bindings bindings = Bindings.builder().scoped();
        Binding binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding binding2 = Binding.builder().with("X").value("101").build();
        bindings.bind(binding2);
        Binding binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {}).build();
        bindings.bind(binding3);
        Binding binding4 = Binding.builder().with("z").type(new TypeReference<Map<String, String>>() {}).build();
        bindings.bind(binding4);
        Binding binding5 = Binding.builder().with("a").type(new TypeReference<ArrayList<Integer>>() {}).value(new ArrayList<>()).build();
        bindings.bind(binding5);

        List<Binding<String>> matches1 = bindings.getBindings(String.class);
        Assert.assertTrue(matches1.contains(binding1));
        Assert.assertTrue(matches1.contains(binding2));
        Assert.assertTrue(!matches1.contains(binding3));

        List<Binding<Map>> matches2 = bindings.getBindings(Map.class);
        Assert.assertTrue(matches2.contains(binding3));
        List<Binding<Map<?, ?>>> matches3 = bindings.getBindings(new TypeReference<Map<?, ?>>() {}.getType());
        Assert.assertTrue(matches3.size() == 2 && matches3.contains(binding3) && matches3.contains(binding4));
        List<Binding<Map<List<Integer>, String>>> matches4 = bindings.getBindings(new TypeReference<Map<List<Integer>, String>>() {}.getType());
        Assert.assertTrue(matches4.size() == 1 && matches4.contains(binding3));
        List<Binding<List<Integer>>> matches5 = bindings.getBindings(new TypeReference<List<Integer>>() {}.getType());
        Assert.assertTrue(matches5.size() == 1 && matches5.contains(binding5));
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

        Assert.assertTrue(bindings.getValue("x").equals("Hello World!"));
        Assert.assertTrue(bindings.getValue("y").equals(values));
    }

    @Test(expected = NoSuchBindingException.class)
    public void bindTest16() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.getValue("x");
    }

    @Test
    public void bindTest17() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", String.class, "Hello World!");
        Assert.assertTrue(bindings.getValue("x").equals("Hello World!"));
        bindings.setValue("x", "new value");
        Assert.assertTrue(bindings.getValue("x").equals("new value"));
        bindings.bind("y", List.class);
        bindings.setValue("y", new ArrayList<>());
        Assert.assertTrue(bindings.getValue("y").equals(new ArrayList<>()));
    }

    @Test(expected = InvalidBindingException.class)
    public void bindTest18() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", String.class, "Hello World!");
        bindings.setValue("x", 123);
    }

    @Test
    public void bindTest19() {
        List<Integer> values = new ArrayList<>();
        values.add(1);
        values.add(2);
        values.add(3);

        Bindings bindings = Bindings.builder().scoped();
        Binding binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding binding3 = Binding.builder().with("y").value(values).build();
        bindings.bind(binding3);
        Binding binding4 = Binding.builder().with("z").build();
        bindings.bind(binding4);

        Map<String, ?> bindingsMap = bindings.asMap();
        Assert.assertTrue(bindings.size() == bindingsMap.size());

        for (Binding<?> binding : bindings) {
            Assert.assertTrue(Objects.equals(binding.getValue(), bindingsMap.get(binding.getName())));
        }
    }

    @Test
    public void bindTest20() {
        Bindings bindings = Bindings.builder().scoped();
        bindings.bind("x", int.class, 250);
        bindings.bind("y", Integer.class, 100);

        List<Binding<Integer>> matches = bindings.getBindings(Integer.class);
        Assert.assertTrue(matches.size() == 2);
    }

    @Test
    public void bindTest21() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("key1", String.class, "value");
        Assert.assertTrue(bindings.getValue("key1").equals("value"));
        bindings.addScope();
        bindings.bind(Binding.builder().with("key1").type(String.class).value("value2").build());
        Assert.assertTrue(bindings.getValue("key1").equals("value2"));
        Assert.assertTrue(bindings.size() == 2);
        bindings.removeScope();
        Assert.assertTrue(bindings.getValue("key1").equals("value"));
        Assert.assertTrue(bindings.size() == 1);
    }

    @Test(expected = CannotRemoveRootScopeException.class)
    public void bindTest22() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.removeScope();
    }

    @Test
    public void bindTest23() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("x", Integer.class);
        Assert.assertTrue(bindings.size() == 1);
        bindings.addScope();
        Assert.assertTrue(bindings.getCurrentBindings().size() == 0);
        bindings.removeScope();
        Assert.assertTrue(bindings.size() == 1);
    }

    @Test
    public void bindTest24() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("x", int.class);
        Assert.assertTrue(bindings.getValue("x").equals(0));
        bindings.addScope().getBindings().bind("x", 24);
        Assert.assertTrue(bindings.getValue("x").equals(24));
        bindings.removeScope();
        Assert.assertTrue(bindings.getValue("x").equals(0));
    }

    @Test
    public void bindTest25() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("x", int.class);
        bindings.addScope().getBindings().bind("x", "Hello World!");
        Binding<String> match1 = bindings.getBinding("x", String.class);
        Assert.assertTrue(match1.getName().equals("x") && String.class.equals(match1.getType()));
        bindings.removeScope();
        Binding<Integer> match2 = bindings.getBinding("x", int.class);
        Assert.assertTrue(match2.getName().equals("x") && int.class.equals(match2.getType()));
    }

    @Test
    public void bindTest26() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", new ArrayList<>());
        bindings.addScope().getBindings()
                .bind("b", new HashSet<>());
        bindings.addScope().getBindings()
                .bind("c", new Vector<>());
        List<Binding<List>> matches1 = bindings.getBindings(List.class);
        Assert.assertTrue(matches1.size() == 1);
        List<Binding<List<?>>> matches2 = bindings.getBindings(new TypeReference<List<?>>() {}.getType());
        Assert.assertTrue(matches2.size() == 1);
        List<Binding<List<?>>> matches3 = bindings.getAllBindings(new TypeReference<List<?>>() {}.getType());
        Assert.assertTrue(matches3.size() == 2);
        List<Binding<Collection<?>>> matches4 = bindings.getAllBindings(new TypeReference<Collection<?>>() {}.getType());
        Assert.assertTrue(matches4.size() == 3);
        bindings.removeScope();
        matches4 = bindings.getAllBindings(new TypeReference<Collection<?>>() {}.getType());
        Assert.assertTrue(matches4.size() == 2);
        bindings.removeScope();
        matches4 = bindings.getAllBindings(new TypeReference<Collection<?>>() {}.getType());
        Assert.assertTrue(matches4.size() == 1);
    }

    @Test
    public void bindTest27() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", new ArrayList<>());
        bindings.addScope().getBindings()
                .bind("b", new HashSet<>());
        bindings.addScope().getBindings()
                .bind("c", new BigDecimal("100"));
        List<Binding<List>> matches1 = bindings.getBindings(List.class);
        Assert.assertTrue(matches1.size() == 1 && matches1.stream().findFirst().get().getName().equals("a"));
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
            Assert.assertTrue(bindings.getValue(entry.getKey()).equals(entry.getValue()));
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
        Assert.assertTrue(bindings.size() == 3);
        Assert.assertTrue(bindings.uniqueSize() == 2);
    }

    @Test
    public void bindTest30() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", new ArrayList<>());
        bindings.addScope().getBindings().bind("b", new HashSet<>());
        bindings.addScope().getBindings().bind("a", new BigDecimal("100"));

        ScopedBindings immutableBindings = bindings.asImmutable();
        Assert.assertTrue(immutableBindings.size() == 3);
        Assert.assertTrue(immutableBindings.uniqueSize() == 2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void bindTest31() {
        ScopedBindings bindings = Bindings.builder().scoped().asImmutable();
        bindings.bind("test", Integer.class);
    }

    @Test
    public void bindTest32() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.addScope("scope-1");
        bindings.addScope("scope-2");
        bindings.addScope("scope-3");
        Assert.assertTrue(bindings.getScopeSize() == 4);
    }

    @Test
    public void bindTest33() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.addScope("scope-1");
        bindings.addScope("scope-2");
        bindings.addScope("scope-3");
        Assert.assertTrue(bindings.containsScope(ScopedBindings.ROOT_SCOPE));
        Assert.assertTrue(bindings.containsScope("scope-1"));
        Assert.assertTrue(bindings.containsScope("scope-2"));
        Assert.assertTrue(bindings.containsScope("scope-3"));
    }

    @Test
    public void bindTest34() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.addScope("scope-1");
        bindings.addScope("scope-2");
        bindings.addScope("scope-3");
        Assert.assertTrue(bindings.containsScope(ScopedBindings.ROOT_SCOPE));

        bindings.removeScope("scope-3");
        Assert.assertFalse(bindings.containsScope("scope-3"));
        Assert.assertTrue(bindings.containsScope("scope-1"));
        Assert.assertTrue(bindings.containsScope("scope-2"));

        bindings.removeScope("scope-2");
        Assert.assertFalse(bindings.containsScope("scope-3"));
        Assert.assertFalse(bindings.containsScope("scope-2"));
        Assert.assertTrue(bindings.containsScope("scope-1"));

        bindings.removeScope("scope-1");
        Assert.assertFalse(bindings.containsScope("scope-3"));
        Assert.assertFalse(bindings.containsScope("scope-2"));
        Assert.assertFalse(bindings.containsScope("scope-1"));
    }

    @Test
    public void bindTest35() {
        ScopedBindings bindings = Bindings.builder().scoped();
        Bindings bindings1 = bindings.addScope("scope-1").getBindings();
        Bindings bindings2 = bindings.addScope("scope-2").getBindings();
        Bindings bindings3 = bindings.addScope("scope-3").getBindings();

        Assert.assertTrue(bindings3 == bindings.getCurrentBindings());
        Assert.assertTrue(bindings2 == bindings.getParentScope().getBindings());
        bindings.removeScope();
        Assert.assertTrue(bindings2 == bindings.getCurrentBindings());
        Assert.assertTrue(bindings1 == bindings.getParentScope().getBindings());
        bindings.removeScope();
        Assert.assertTrue(bindings1 == bindings.getCurrentBindings());
        Assert.assertTrue(bindings.getParentScope() == null);
    }

    @Test
    public void bindTest36() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.addScope("scope-1");
        bindings.addScope("scope-2");
        bindings.addScope("scope-3");

        bindings.removeScope("scope-1");
        Assert.assertFalse(bindings.containsScope("scope-3"));
        Assert.assertFalse(bindings.containsScope("scope-2"));
        Assert.assertFalse(bindings.containsScope("scope-1"));
    }
}
