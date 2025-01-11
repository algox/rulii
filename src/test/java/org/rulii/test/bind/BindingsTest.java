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

import org.rulii.bind.*;
import org.rulii.util.TypeReference;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(bindings instanceof DefaultBindings);
        Bindings scopedBindings = Bindings.builder().scoped();
        Assert.assertTrue(scopedBindings instanceof DefaultScopedBindings);
    }

    @Test
    public void testBindings1() {
        Bindings bindings = Bindings.builder().standard();
        Binding b = bindings.bind("key", String.class, "value");
        Binding<String> var = bindings.getBinding("key");
        Assert.assertEquals("key", var.getName());
        Assert.assertEquals(String.class, var.getType());
        Assert.assertEquals("value", var.getValue());
        Assert.assertEquals(b, var);
    }

    @Test
    public void testBindings2() {
        Bindings bindings = Bindings.builder().standard();
        Binding b = bindings.bind("key", Double.class);
        Binding<Double> var = bindings.getBinding("key");
        var.setValue(33.33);
        double result = var.getValue();
        Assert.assertEquals(33.33, result, 0.00);
        Assert.assertEquals(b, var);
    }

    @Test
    public void testBindings3() {
        List<Integer> values = new ArrayList<>();
        values.add(1);
        values.add(2);
        values.add(3);
        Bindings bindings = Bindings.builder().standard();
        Binding b = bindings.bind("key", new TypeReference<List<Integer>>(){});
        Binding<List<Integer>> var = bindings.getBinding("key", new TypeReference<List<Integer>>(){}.getType());
        var.setValue(values);
        Assert.assertEquals(values, var.getValue());
        Assert.assertEquals(b, var);
    }

    @Test(expected = BindingAlreadyExistsException.class)
    public void testBindings4() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key", String.class, "value");
        bindings.bind("key", new TypeReference<List<Integer>>(){});
    }

    @Test
    public void testBindings5() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class, "value");
        bindings.bind("key2", new TypeReference<List<Integer>>(){});
        bindings.bind("key3", TypeReference.with(BigDecimal.class));
        Assert.assertEquals(3, bindings.size());
    }

    @Test
    public void testBindings6() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class, "value");
        bindings.bind("key2", new TypeReference<List<Integer>>(){});
        Assert.assertTrue(bindings.contains("key1"));
        Assert.assertTrue(bindings.contains("key2"));
    }

    @Test
    public void testBindings7() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class, "value");
        bindings.bind("key2", new TypeReference<List<?>>(){});
        bindings.bind("key3", BigDecimal.class);
        bindings.bind("key4", new TypeReference<Map<? extends List<?>, List<Integer>>>(){});

        Assert.assertTrue(bindings.contains("key1", String.class));
        Assert.assertTrue(bindings.contains("key2", new TypeReference<List<?>>(){}));
        Assert.assertTrue(bindings.contains("key3", BigDecimal.class));
        Assert.assertTrue(bindings.contains("key4", new TypeReference<Map<? extends List<?>, List<Integer>>>(){}));
    }

    @Test
    public void testBindings8() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class, "value");
        bindings.bind("key2", new TypeReference<List<?>>(){});
        bindings.bind("key3", BigDecimal.class, new BigDecimal("10.00"));
        bindings.bind("key4", new TypeReference<Map<? extends List<?>, List<Integer>>>(){});

        Binding<BigDecimal> binding = bindings.getBinding("key3");

        Assert.assertNotNull(binding);
        Assert.assertEquals(binding.getValue(), new BigDecimal("10.00"));
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
        Assert.assertTrue(bindings1.size() == 2);
        List<Binding<Map<? extends List<?>, List<Integer>>>> bindings2 = bindings.getBindings(
                new TypeReference<Map<? extends List<?>, List<Integer>>>() {}.getType());
        Assert.assertTrue(bindings2.size() == 1);
    }

    @Test
    public void testBindings10() {
        Bindings bindings1 = Bindings.builder().standard();
        bindings1.bind(key1 -> "hello", a -> 123, c -> 12.11);
        Assert.assertTrue(bindings1.contains("key1", String.class));
        Assert.assertTrue(bindings1.contains("a", int.class));
        Assert.assertTrue(bindings1.contains("c", double.class));
    }

    @Test
    public void testBindingsWithNoType() {
        List<Integer> values = new ArrayList<>();

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", 25);
        bindings.bind("b", "Hello world");
        bindings.bind("c", values);
        bindings.bind("d", Integer.class, null);

        Assert.assertTrue(bindings.size() == 4);
        Assert.assertTrue(bindings.contains("a", int.class));
        Assert.assertTrue(bindings.contains("b", String.class));
        Assert.assertTrue(bindings.contains("c", ArrayList.class));
    }

    @Test
    public void bindTest1() {
        Bindings bindings = Bindings.builder().standard();
        Binding binding = Binding.builder().with("key1").value("Hello World!").build();
        bindings.bind(binding);
        Binding match = bindings.getBinding("key1");
        Assert.assertTrue(match.equals(binding));
    }

    @Test
    public void bindTest2() {
        Bindings bindings = Bindings.builder().standard();
        Binding binding = Binding.builder().with(key1 -> "hello world!").build();
        bindings.bind(binding);
        Binding match = bindings.getBinding("key1");
        Assert.assertTrue(match.equals(binding));
    }

    @Test
    public void bindTest3() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", 100);
        Binding match = bindings.getBinding("key1");
        Assert.assertTrue(match.getName().equals("key1"));
        Assert.assertTrue(match.getValue().equals(100));
        Assert.assertTrue(match.getType().equals(Integer.class));
    }

    @Test
    public void bindTest4() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", int.class);
        Binding match = bindings.getBinding("key1");
        Assert.assertTrue(match.getName().equals("key1"));
        Assert.assertTrue(match.getValue().equals(0));
        Assert.assertTrue(match.getType().equals(int.class));
    }

    @Test
    public void bindTest5() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", new TypeReference<List<Integer>>() {
        });
        Binding match = bindings.getBinding("x");
        Assert.assertTrue(match.getName().equals("x"));
        Assert.assertTrue(match.getType().equals(new TypeReference<List<Integer>>() {
        }.getType()));
        Assert.assertTrue(match.getValue() == null);
    }

    @Test
    public void bindTest6() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 250);
        Binding match = bindings.getBinding("x");
        Assert.assertTrue(match.getName().equals("x"));
        Assert.assertTrue(match.getType().equals(int.class));
        Assert.assertTrue(match.getValue().equals(250));
    }

    @Test
    public void bindTest7() {
        List<Integer> values = new ArrayList<>();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", new TypeReference<List<Integer>>() {
        }, values);
        Binding match = bindings.getBinding("x");
        Assert.assertTrue(match.getName().equals("x"));
        Assert.assertTrue(match.getType().equals(new TypeReference<List<Integer>>() {
        }.getType()));
        Assert.assertTrue(match.getValue().equals(values));
    }

    @Test
    public void bindTest8() {
        Bindings bindings = Bindings.builder().standard();
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
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().with(key1 -> "hello world!").build());
        bindings.bind(Binding.builder().with(key2 -> 25).build());
        bindings.bind(Binding.builder().with(key3 -> new BigDecimal("100.00")).build());
        Assert.assertTrue(bindings.size() == 3);
    }

    @Test(expected = BindingAlreadyExistsException.class)
    public void bindTest10() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 250);
        bindings.bind("x", String.class, "Hello world");
    }

    @Test
    public void bindTest11() {
        Bindings bindings = Bindings.builder().standard();
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
        Bindings bindings = Bindings.builder().standard();
        Binding binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {
        }).build();
        bindings.bind(binding3);

        Assert.assertTrue(bindings.contains("x"));
        Assert.assertTrue(bindings.contains("X"));
        Assert.assertTrue(bindings.contains("X", Integer.class));
        Assert.assertTrue(bindings.contains("x", String.class));
        Assert.assertTrue(bindings.contains("y", new TypeReference<Map<List<Integer>, String>>() {
        }));
        Assert.assertTrue(!bindings.contains("a"));
    }

    @Test
    public void bindTest13() {
        Bindings bindings = Bindings.builder().standard();
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
        Bindings bindings = Bindings.builder().standard();
        Binding binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding binding2 = Binding.builder().with("X").value("101").build();
        bindings.bind(binding2);
        Binding binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {
        }).build();
        bindings.bind(binding3);
        Binding binding4 = Binding.builder().with("z").type(new TypeReference<Map<String, String>>() {
        }).build();
        bindings.bind(binding4);
        Binding binding5 = Binding.builder().with("a").type(new TypeReference<ArrayList<Integer>>() {
        }).value(new ArrayList<>()).build();
        bindings.bind(binding5);

        List<Binding<String>> matches1 = bindings.getBindings(String.class);
        Assert.assertTrue(matches1.contains(binding1));
        Assert.assertTrue(matches1.contains(binding2));
        Assert.assertTrue(!matches1.contains(binding3));

        List<Binding<Map>> matches2 = bindings.getBindings(Map.class);
        Assert.assertTrue(matches2.contains(binding3));
        List<Binding<Map<?, ?>>> matches3 = bindings.getBindings(new TypeReference<Map<?, ?>>() {
        }.getType());
        Assert.assertTrue(matches3.size() == 2 && matches3.contains(binding3) && matches3.contains(binding4));
        List<Binding<Map<List<Integer>, String>>> matches4 = bindings.getBindings(new TypeReference<Map<List<Integer>, String>>() {
        }.getType());
        Assert.assertTrue(matches4.size() == 1 && matches4.contains(binding3));
        List<Binding<List<Integer>>> matches5 = bindings.getBindings(new TypeReference<List<Integer>>() {
        }.getType());
        Assert.assertTrue(matches5.size() == 1 && matches5.contains(binding5));
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

        Assert.assertTrue(bindings.getValue("x").equals("Hello World!"));
        Assert.assertTrue(bindings.getValue("y").equals(values));
    }

    @Test(expected = NoSuchBindingException.class)
    public void bindTest16() {
        Bindings bindings = Bindings.builder().standard();
        bindings.getValue("x");
    }

    @Test
    public void bindTest17() {
        Bindings bindings = Bindings.builder().standard();
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
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, "Hello World!");
        bindings.setValue("x", 123);
    }

    @Test
    public void bindTest19() {
        List<Integer> values = new ArrayList<>();
        values.add(1);
        values.add(2);
        values.add(3);

        Bindings bindings = Bindings.builder().standard();
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
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 250);
        bindings.bind("y", Integer.class, 100);

        List<Binding<Integer>> matches = bindings.getBindings(Integer.class);
        Assert.assertTrue(matches.size() == 2);
    }

    @Test
    public void bindTest21() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", String.class);
        bindings.bind("b", new TypeReference<Set<Integer>>() {});
        bindings.bind("c", new TypeReference<List<Integer>>() {});
        bindings.bind("d", new TypeReference<Map<?, Long>>() {});

        List<Binding<List<?>>> matches = bindings.getBindings(new TypeReference<List<?>>() {}.getType());
        Assert.assertTrue(matches.size() == 1);
        List<Binding<Collection<?>>> matches1 = bindings.getBindings(new TypeReference<Collection<?>>() {}.getType());
        Assert.assertTrue(matches1.size() == 2);
    }

    @Test
    public void bindTest22() {
        Bindings bindings = Bindings.builder().standard();
        Binding b1 = bindings.bind("a", String.class, "hello world");
        Binding b2 = bindings.bind("b", new TypeReference<Set<Integer>>() {});

        Assert.assertEquals(b1.getName(), "a");
        Assert.assertEquals(b1.getType(), String.class);
        Assert.assertEquals(b1.getValue(), "hello world");
        Assert.assertEquals(b2.getName(), "b");
        Assert.assertEquals(b2.getType(), new TypeReference<Set<Integer>>() {}.getType());
    }
}