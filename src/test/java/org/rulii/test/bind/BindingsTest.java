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
 * Tests for Bindings.
 *
 * @author Max Arulananthan
 */
public class BindingsTest {

    public BindingsTest() {
        super();
    }

    @Test
    public void testCreateBindings() {
        Bindings bindings = Bindings.builder().standard();
        Assertions.assertInstanceOf(DefaultBindings.class, bindings);
        Bindings scopedBindings = Bindings.builder().scoped();
        Assertions.assertInstanceOf(DefaultScopedBindings.class, scopedBindings);
    }

    @Test
    public void testBindings1() {
        Bindings bindings = Bindings.builder().standard();
        Binding<String> b = bindings.bind("key", String.class, "value");
        Binding<String> var = bindings.getBinding("key");
        Assertions.assertEquals("key", var.getName());
        Assertions.assertEquals(String.class, var.getType());
        Assertions.assertEquals("value", var.getValue());
        Assertions.assertEquals(b, var);
    }

    @Test
    public void testBindings2() {
        Bindings bindings = Bindings.builder().standard();
        Binding<Double> b = bindings.bind("key", Double.class);
        Binding<Double> var = bindings.getBinding("key");
        var.setValue(33.33);
        double result = var.getValue();
        Assertions.assertEquals(33.33, result, 0.00);
        Assertions.assertEquals(b, var);
    }

    @Test
    public void testBindings3() {
        List<Integer> values = new ArrayList<>();
        values.add(1);
        values.add(2);
        values.add(3);
        Bindings bindings = Bindings.builder().standard();
        Binding<List<Integer>> b = bindings.bind("key", new TypeReference<List<Integer>>(){});
        Binding<List<Integer>> var = bindings.getBinding("key", new TypeReference<List<Integer>>(){}.getType());
        var.setValue(values);
        Assertions.assertEquals(values, var.getValue());
        Assertions.assertEquals(b, var);
    }

    @Test
    public void testBindings4() {
        Assertions.assertThrows(BindingAlreadyExistsException.class, () -> {
            Bindings bindings = Bindings.builder().standard();
            bindings.bind("key", String.class, "value");
            bindings.bind("key", new TypeReference<List<Integer>>() {
            });
        });
    }

    @Test
    public void testBindings5() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class, "value");
        bindings.bind("key2", new TypeReference<List<Integer>>(){});
        bindings.bind("key3", TypeReference.with(BigDecimal.class));
        Assertions.assertEquals(3, bindings.size());
    }

    @Test
    public void testBindings6() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class, "value");
        bindings.bind("key2", new TypeReference<List<Integer>>(){});
        Assertions.assertTrue(bindings.contains("key1"));
        Assertions.assertTrue(bindings.contains("key2"));
    }

    @Test
    public void testBindings7() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class, "value");
        bindings.bind("key2", new TypeReference<List<?>>(){});
        bindings.bind("key3", BigDecimal.class);
        bindings.bind("key4", new TypeReference<Map<? extends List<?>, List<Integer>>>(){});

        Assertions.assertTrue(bindings.contains("key1", String.class));
        Assertions.assertTrue(bindings.contains("key2", new TypeReference<List<?>>(){}));
        Assertions.assertTrue(bindings.contains("key3", BigDecimal.class));
        Assertions.assertTrue(bindings.contains("key4", new TypeReference<Map<? extends List<?>, List<Integer>>>(){}));
    }

    @Test
    public void testBindings8() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class, "value");
        bindings.bind("key2", new TypeReference<List<?>>(){});
        bindings.bind("key3", BigDecimal.class, new BigDecimal("10.00"));
        bindings.bind("key4", new TypeReference<Map<? extends List<?>, List<Integer>>>(){});

        Binding<BigDecimal> binding = bindings.getBinding("key3");

        Assertions.assertNotNull(binding);
        Assertions.assertEquals(binding.getValue(), new BigDecimal("10.00"));
    }

    @Test
    public void testBindings9() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class, "value");
        bindings.bind("key2", new TypeReference<List<?>>() {});
        bindings.bind("key3", TypeReference.with(BigDecimal.class), new BigDecimal("10.00"));
        bindings.bind("key4", new TypeReference<Map<? extends List<?>, List<Integer>>>() {});
        bindings.bind("key5", TypeReference.with(BigDecimal.class), new BigDecimal("20.00"));

        List<Binding<BigDecimal>> bindings1 = bindings.getBindings(BigDecimal.class);
        Assertions.assertEquals(2, bindings1.size());
        List<Binding<Map<? extends List<?>, List<Integer>>>> bindings2 = bindings.getBindings(
                new TypeReference<Map<? extends List<?>, List<Integer>>>() {}.getType());
        Assertions.assertEquals(1, bindings2.size());
    }

    @Test
    public void testBindings10() {
        Bindings bindings1 = Bindings.builder().standard();
        bindings1.bind(key1 -> "hello", a -> 123, c -> 12.11);
        Assertions.assertTrue(bindings1.contains("key1", String.class));
        Assertions.assertTrue(bindings1.contains("a", int.class));
        Assertions.assertTrue(bindings1.contains("c", double.class));
    }

    @Test
    public void testBindingsWithNoType() {
        List<Integer> values = new ArrayList<>();

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", 25);
        bindings.bind("b", "Hello world");
        bindings.bind("c", values);
        bindings.bind("d", Integer.class, null);

        Assertions.assertEquals(4, bindings.size());
        Assertions.assertTrue(bindings.contains("a", int.class));
        Assertions.assertTrue(bindings.contains("b", String.class));
        Assertions.assertTrue(bindings.contains("c", ArrayList.class));
    }

    @Test
    public void bindTest1() {
        Bindings bindings = Bindings.builder().standard();
        Binding<String> binding = Binding.builder().with("key1").value("Hello World!").build();
        bindings.bind(binding);
        Binding<String> match = bindings.getBinding("key1");
        Assertions.assertEquals(match, binding);
    }

    @Test
    public void bindTest2() {
        Bindings bindings = Bindings.builder().standard();
        Binding<String> binding = Binding.builder().with(key1 -> "hello world!").build();
        bindings.bind(binding);
        Binding<String> match = bindings.getBinding("key1");
        Assertions.assertEquals(match, binding);
    }

    @Test
    public void bindTest3() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", 100);
        Binding<Integer> match = bindings.getBinding("key1");
        Assertions.assertEquals("key1", match.getName());
        Assertions.assertEquals(100, match.getValue());
        Assertions.assertEquals(match.getType(), Integer.class);
    }

    @Test
    public void bindTest4() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", int.class);
        Binding<Integer> match = bindings.getBinding("key1");
        Assertions.assertEquals("key1", match.getName());
        Assertions.assertEquals(0, match.getValue());
        Assertions.assertEquals(match.getType(), int.class);
    }

    @Test
    public void bindTest5() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", new TypeReference<List<Integer>>() {
        });
        Binding<List<Integer>> match = bindings.getBinding("x");
        Assertions.assertEquals("x", match.getName());
        Assertions.assertEquals(match.getType(), new TypeReference<List<Integer>>() {
        }.getType());
        Assertions.assertNull(match.getValue());
    }

    @Test
    public void bindTest6() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 250);
        Binding<Integer> match = bindings.getBinding("x");
        Assertions.assertEquals("x", match.getName());
        Assertions.assertEquals(match.getType(), int.class);
        Assertions.assertEquals(250, match.getValue());
    }

    @Test
    public void bindTest7() {
        List<Integer> values = new ArrayList<>();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", new TypeReference<List<Integer>>() {
        }, values);
        Binding<List<Integer>> match = bindings.getBinding("x");
        Assertions.assertEquals("x", match.getName());
        Assertions.assertEquals(match.getType(), new TypeReference<List<Integer>>() {
        }.getType());
        Assertions.assertEquals(match.getValue(), values);
    }

    @Test
    public void bindTest8() {
        Bindings bindings = Bindings.builder().standard();
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
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().with(key1 -> "hello world!").build());
        bindings.bind(Binding.builder().with(key2 -> 25).build());
        bindings.bind(Binding.builder().with(key3 -> new BigDecimal("100.00")).build());
        Assertions.assertEquals(3, bindings.size());
    }

    @Test
    public void bindTest10() {
        Assertions.assertThrows(BindingAlreadyExistsException.class, () -> {
            Bindings bindings = Bindings.builder().standard();
            bindings.bind("x", int.class, 250);
            bindings.bind("x", String.class, "Hello world");
        });
    }

    @Test
    public void bindTest11() {
        Bindings bindings = Bindings.builder().standard();
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
        Bindings bindings = Bindings.builder().standard();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<Integer> binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding<Map<List<Integer>, String>> binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {
        }).build();
        bindings.bind(binding3);

        Assertions.assertTrue(bindings.contains("x"));
        Assertions.assertTrue(bindings.contains("X"));
        Assertions.assertTrue(bindings.contains("X", Integer.class));
        Assertions.assertTrue(bindings.contains("x", String.class));
        Assertions.assertTrue(bindings.contains("y", new TypeReference<Map<List<Integer>, String>>() {
        }));
        Assertions.assertFalse(bindings.contains("a"));
    }

    @Test
    public void bindTest13() {
        Bindings bindings = Bindings.builder().standard();
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
        Bindings bindings = Bindings.builder().standard();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<Integer> binding2 = Binding.builder().with("X").value("101").build();
        bindings.bind(binding2);
        Binding<Map<List<Integer>, String>> binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {
        }).build();
        bindings.bind(binding3);
        Binding<Map<String, String>> binding4 = Binding.builder().with("z").type(new TypeReference<Map<String, String>>() {
        }).build();
        bindings.bind(binding4);
        Binding<ArrayList<Integer>> binding5 = Binding.builder().with("a").type(new TypeReference<ArrayList<Integer>>() {
        }).value(new ArrayList<>()).build();
        bindings.bind(binding5);

        List<Binding<String>> matches1 = bindings.getBindings(String.class);
        Assertions.assertTrue(matches1.contains(binding1));
        Assertions.assertTrue(matches1.contains(binding2));
        Assertions.assertFalse(matches1.contains(binding3));

        List<Binding<Map<?,?>>> matches2 = bindings.getBindings(Map.class);
        Assertions.assertTrue(matches2.contains(binding3));
        List<Binding<Map<?, ?>>> matches3 = bindings.getBindings(new TypeReference<Map<?, ?>>() {
        }.getType());
        Assertions.assertTrue(matches3.size() == 2 && matches3.contains(binding3) && matches3.contains(binding4));
        List<Binding<Map<List<Integer>, String>>> matches4 = bindings.getBindings(new TypeReference<Map<List<Integer>, String>>() {
        }.getType());
        Assertions.assertTrue(matches4.size() == 1 && matches4.contains(binding3));
        List<Binding<List<Integer>>> matches5 = bindings.getBindings(new TypeReference<List<Integer>>() {
        }.getType());
        Assertions.assertTrue(matches5.size() == 1 && matches5.contains(binding5));
    }

    @Test
    public void bindTest15() {
        List<Integer> values = new ArrayList<>();
        values.add(1);
        values.add(2);
        values.add(3);

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, "Hello World!");
        bindings.bind("y", List.class, values);

        Assertions.assertEquals("Hello World!", bindings.getValue("x"));
        Assertions.assertEquals(bindings.getValue("y"), values);
    }

    @Test
    public void bindTest16() {
        Assertions.assertThrows(NoSuchBindingException.class, () -> {
            Bindings bindings = Bindings.builder().standard();
            bindings.getValue("x");
        });
    }

    @Test
    public void bindTest17() {
        Bindings bindings = Bindings.builder().standard();
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
            Bindings bindings = Bindings.builder().standard();
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

        Bindings bindings = Bindings.builder().standard();
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
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 250);
        bindings.bind("y", Integer.class, 100);

        List<Binding<Integer>> matches = bindings.getBindings(Integer.class);
        Assertions.assertEquals(2, matches.size());
    }

    @Test
    public void bindTest21() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", String.class);
        bindings.bind("b", new TypeReference<Set<Integer>>() {});
        bindings.bind("c", new TypeReference<List<Integer>>() {});
        bindings.bind("d", new TypeReference<Map<?, Long>>() {});

        List<Binding<List<?>>> matches = bindings.getBindings(new TypeReference<List<?>>() {}.getType());
        Assertions.assertEquals(1, matches.size());
        List<Binding<Collection<?>>> matches1 = bindings.getBindings(new TypeReference<Collection<?>>() {}.getType());
        Assertions.assertEquals(2, matches1.size());
    }

    @Test
    public void bindTest22() {
        Bindings bindings = Bindings.builder().standard();
        Binding<String> b1 = bindings.bind("a", String.class, "hello world");
        Binding<Set<Integer>> b2 = bindings.bind("b", new TypeReference<Set<Integer>>() {});

        Assertions.assertEquals(b1.getName(), "a");
        Assertions.assertEquals(b1.getType(), String.class);
        Assertions.assertEquals(b1.getValue(), "hello world");
        Assertions.assertEquals(b2.getName(), "b");
        Assertions.assertEquals(b2.getType(), new TypeReference<Set<Integer>>() {}.getType());
    }
}