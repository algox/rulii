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
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.NoSuchBindingException;
import org.rulii.bind.load.PropertyBindingLoader;
import org.rulii.util.TypeReference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests for ImmutableBindings.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class ImmutableBindingsTest {

    public ImmutableBindingsTest() {
        super();
    }

    @Test
    public void immutabilityTest1() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            Bindings.builder().standard().asImmutable().bind(Binding.builder().with("x").build());
        });
    }

    @Test
    public void immutabilityTest2() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            Bindings.builder().standard().asImmutable().bind(a -> 123);
        });
    }

    @Test
    public void immutabilityTest3() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            Bindings.builder().standard().asImmutable().bind(a -> 123, b -> "xyz");
        });
    }

    @Test
    public void immutabilityTest4() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            Bindings.builder().standard().asImmutable().bind("a", 123);
        });
    }

    @Test
    public void immutabilityTest5() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            Bindings.builder().standard().asImmutable().bind("a", Integer.class);
        });
    }

    @Test
    public void immutabilityTest6() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            Bindings.builder().standard().asImmutable().bind("a", new TypeReference<List<Integer>>() {
            });
        });
    }

    @Test
    public void immutabilityTest7() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            Bindings.builder().standard().asImmutable().bind("a", int.class, 200);
        });
    }

    @Test
    public void immutabilityTest8() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            Bindings.builder().standard().asImmutable().bind("a", new TypeReference<List<Integer>>() {
            }, new ArrayList<>());
        });
    }

    @Test
    public void immutabilityTest10() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            Bindings.builder().standard().asImmutable().load(new PropertyBindingLoader<>(), new Object());
        });
    }

    @Test
    public void immutabilityTest11() {
        Bindings bindings = Bindings.builder().standard();
        Binding<String> binding = Binding.builder().with("key1").value("Hello World!").build();
        bindings.bind(binding);
        Binding<String> match = Bindings.builder().immutable(bindings).getBinding("key1");
        Assertions.assertEquals(match.getValue(), binding.getValue());
    }

    @Test
    public void immutabilityTest12() {
        Bindings bindings = Bindings.builder().standard();
        Binding<String> binding = Binding.builder().with(key1 -> "hello world!").build();
        bindings.bind(binding);
        Binding<String> match = Bindings.builder().immutable(bindings).getBinding("key1");
        Assertions.assertEquals(match.getName(), binding.getName());
    }

    @Test
    public void immutabilityTest13() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", 100);
        Binding<Integer> match = bindings.asImmutable().getBinding("key1");
        Assertions.assertEquals("key1", match.getName());
        Assertions.assertEquals(100, match.getValue());
        Assertions.assertEquals(match.getType(), Integer.class);
    }

    @Test
    public void immutabilityTest14() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", int.class);
        Binding<Integer> match = bindings.asImmutable().getBinding("key1");
        Assertions.assertEquals("key1", match.getName());
        Assertions.assertEquals(0, match.getValue());
        Assertions.assertEquals(match.getType(), int.class);
    }

    @Test
    public void immutabilityTest15() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", new TypeReference<List<Integer>>() {
        });

        Binding<List<Integer>> match = bindings.asImmutable().getBinding("x");
        Assertions.assertEquals("x", match.getName());
        Assertions.assertEquals(match.getType(), new TypeReference<List<Integer>>() {
        }.getType());
        Assertions.assertNull(match.getValue());
    }

    @Test
    public void immutabilityTest16() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 250);
        Binding<Integer> match = bindings.asImmutable().getBinding("x");
        Assertions.assertEquals("x", match.getName());
        Assertions.assertEquals(match.getType(), int.class);
        Assertions.assertEquals(250, match.getValue());
    }

    @Test
    public void immutabilityTest17() {
        List<Integer> values = new ArrayList<>();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", new TypeReference<List<Integer>>() {
        }, values);

        Binding<List<Integer>> match = bindings.asImmutable().getBinding("x");
        Assertions.assertEquals("x", match.getName());
        Assertions.assertEquals(match.getType(), new TypeReference<List<Integer>>() {
        }.getType());
        Assertions.assertEquals(match.getValue(), values);
    }

    @Test
    public void immutabilityTest18() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().with(key1 -> "hello world!").build());
        bindings.bind(Binding.builder().with(key2 -> 25).build());
        bindings.bind(Binding.builder().with(key3 -> new BigDecimal("100.00")).build());
        Assertions.assertEquals(3, Bindings.builder().immutable(bindings).size());
    }

    @Test
    public void immutabilityTest19() {
        Bindings bindings = Bindings.builder().standard();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<Integer> binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);

        Binding<String> match = bindings.asImmutable().getBinding("X");
        Assertions.assertEquals(match.getName(), binding2.getName());
        match = bindings.getBinding("key");
        Assertions.assertNull(match);
    }

    @Test
    public void immutabilityTest20() {
        Bindings bindings = Bindings.builder().standard();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<Integer> binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding<Map<List<Integer>, String>> binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {
        }).build();
        bindings.bind(binding3);

        Assertions.assertTrue(bindings.asImmutable().contains("x"));
        Assertions.assertTrue(bindings.asImmutable().contains("X"));
        Assertions.assertTrue(bindings.asImmutable().contains("X", Integer.class));
        Assertions.assertTrue(bindings.asImmutable().contains("x", String.class));
        Assertions.assertTrue(bindings.asImmutable().contains("y", new TypeReference<Map<List<Integer>, String>>() {
        }));
        Assertions.assertFalse(bindings.asImmutable().contains("a"));
    }

    @Test
    public void immutabilityTest21() {
        Bindings bindings = Bindings.builder().standard();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<Integer> binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding<Map<List<Integer>, String>> binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {
        }).build();
        bindings.bind(binding3);
        Binding<String> match1 = bindings.asImmutable().getBinding("x");
        Binding<Integer> match2 = bindings.asImmutable().getBinding("X", Integer.class);
        Binding<Map<List<Integer>, String>> match3 = bindings.asImmutable().getBinding("y", new TypeReference<Map<List<Integer>, String>>() {
        }.getType());
        Binding<?> match4 = bindings.asImmutable().getBinding("a");
        Binding<Integer> match5 = bindings.asImmutable().getBinding("a", Integer.class);
        Binding<Map<List<Integer>, String>> match6 = bindings.asImmutable().getBinding("a", new TypeReference<Map<List<Integer>, String>>() {
        }.getType());
        Assertions.assertEquals(match1.getValue(), binding1.getValue());
        Assertions.assertEquals(match2.getValue(), binding2.getValue());
        Assertions.assertEquals(match3.getType(), binding3.getType());
        Assertions.assertNull(match4);
        Assertions.assertNull(match5);
        Assertions.assertNull(match6);
    }

    @Test
    public void immutabilityTest22() {
        Bindings bindings = Bindings.builder().standard();
        Binding<String> binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding<String> binding2 = Binding.builder().with("X").value("101").build();
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

        List<Binding<String>> matches1 = bindings.asImmutable().getBindings(String.class);
        Assertions.assertEquals(matches1.get(0), binding1);
        Assertions.assertEquals(matches1.get(1), binding2);

        List<Binding<Map<?, ?>>> matches2 = bindings.asImmutable().getBindings(Map.class);
        Assertions.assertEquals(matches2.get(0), binding3);
        List<Binding<Map<?, ?>>> matches3 = bindings.getBindings(new TypeReference<Map<?, ?>>() {
        }.getType());
        Assertions.assertTrue(matches3.size() == 2 && matches3.contains(binding3) && matches3.contains(binding4));
        List<Binding<Map<List<Integer>, String>>> matches4 = bindings.asImmutable()
                .getBindings(new TypeReference<Map<List<Integer>, String>>() {}.getType());
        Assertions.assertTrue(matches4.size() == 1 && matches4.get(0).equals(binding3));
        List<Binding<List<Integer>>> matches5 = bindings.asImmutable().getBindings(new TypeReference<List<Integer>>() {
        }.getType());
        Assertions.assertTrue(matches5.size() == 1 && matches5.get(0).equals(binding5));
    }

    @Test
    public void immutabilityTest23() {
        List<Integer> values = new ArrayList<>();
        values.add(1);
        values.add(2);
        values.add(3);

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, "Hello World!");
        bindings.bind("y", List.class, values);

        Assertions.assertEquals("Hello World!", Bindings.builder().immutable(bindings).getValue("x"));
        Assertions.assertEquals(bindings.asImmutable().getValue("y"), values);
    }

    @Test
    public void immutabilityTest24() {
        Assertions.assertThrows(NoSuchBindingException.class, () -> {
            Bindings bindings = Bindings.builder().standard().asImmutable();
            bindings.getValue("x");
        });
    }

    @Test
    public void immutabilityTest25() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, "Hello World!");
        Assertions.assertEquals("Hello World!", bindings.asImmutable().getValue("x"));
        bindings.setValue("x", "new value");
        Assertions.assertEquals("new value", bindings.asImmutable().getValue("x"));
        bindings.bind("y", List.class);
        bindings.setValue("y", new ArrayList<>());
        Assertions.assertEquals(bindings.asImmutable().getValue("y"), new ArrayList<>());
    }

    @Test
    public void immutabilityTest26() {
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

        Map<String, ?> bindingsMap = bindings.asImmutable().asMap();
        Assertions.assertEquals(bindings.size(), bindingsMap.size());

        for (Binding<?> binding : bindings) {
            Assertions.assertEquals(binding.getValue(), bindingsMap.get(binding.getName()));
        }
    }

    @Test
    public void immutabilityTest27() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 250);
        bindings.bind("y", Integer.class, 100);
        List<Binding<Integer>> matches = Bindings.builder().immutable(bindings).getBindings(Integer.class);
        Assertions.assertEquals(2, matches.size());
    }
}
