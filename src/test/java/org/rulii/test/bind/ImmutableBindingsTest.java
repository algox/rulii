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

import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.NoSuchBindingException;
import org.rulii.bind.load.PropertyBindingLoader;
import org.rulii.util.TypeReference;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

/**
 * Tests for ImmutableBindings.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ImmutableBindingsTest {

    public ImmutableBindingsTest() {
        super();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutabilityTest1() {
        Bindings.builder().standard().asImmutableBindings().bind(Binding.builder().with("x").build());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutabilityTest2() {
        Bindings.builder().standard().asImmutableBindings().bind(a -> 123);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutabilityTest3() {
        Bindings.builder().standard().asImmutableBindings().bind(a -> 123, b -> "xyz");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutabilityTest4() {
        Bindings.builder().standard().asImmutableBindings().bind("a", 123);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutabilityTest5() {
        Bindings.builder().standard().asImmutableBindings().bind("a", Integer.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutabilityTest6() {
        Bindings.builder().standard().asImmutableBindings().bind("a", new TypeReference<List<Integer>>() {});
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutabilityTest7() {
        Bindings.builder().standard().asImmutableBindings().bind("a", int.class, 200);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutabilityTest8() {
        Bindings.builder().standard().asImmutableBindings().bind("a", new TypeReference<List<Integer>>() {}, new ArrayList<>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutabilityTest10() {
        Bindings.builder().standard().asImmutableBindings().load(new PropertyBindingLoader<>(), new Object());
    }

    @Test
    public void immutabilityTest11() {
        Bindings bindings = Bindings.builder().standard();
        Binding binding = Binding.builder().with("key1").value("Hello World!").build();
        bindings.bind(binding);

        Binding match = Bindings.builder().immutable(bindings).getBinding("key1");
        Assert.assertTrue(match.getValue().equals(binding.getValue()));
    }

    @Test
    public void immutabilityTest12() {
        Bindings bindings = Bindings.builder().standard();
        Binding binding = Binding.builder().with(key1 -> "hello world!").build();
        bindings.bind(binding);

        Binding match = Bindings.builder().immutable(bindings).getBinding("key1");
        Assert.assertTrue(match.getName().equals(binding.getName()));
    }

    @Test
    public void immutabilityTest13() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", 100);

        Binding match = bindings.asImmutableBindings().getBinding("key1");
        Assert.assertTrue(match.getName().equals("key1"));
        Assert.assertTrue(match.getValue().equals(100));
        Assert.assertTrue(match.getType().equals(Integer.class));
    }

    @Test
    public void immutabilityTest14() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", int.class);

        Binding match = bindings.asImmutableBindings().getBinding("key1");
        Assert.assertTrue(match.getName().equals("key1"));
        Assert.assertTrue(match.getValue().equals(0));
        Assert.assertTrue(match.getType().equals(int.class));
    }

    @Test
    public void immutabilityTest15() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", new TypeReference<List<Integer>>() {
        });

        Binding match = bindings.asImmutableBindings().getBinding("x");
        Assert.assertTrue(match.getName().equals("x"));
        Assert.assertTrue(match.getType().equals(new TypeReference<List<Integer>>() {
        }.getType()));
        Assert.assertTrue(match.getValue() == null);
    }

    @Test
    public void immutabilityTest16() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 250);

        Binding match = bindings.asImmutableBindings().getBinding("x");
        Assert.assertTrue(match.getName().equals("x"));
        Assert.assertTrue(match.getType().equals(int.class));
        Assert.assertTrue(match.getValue().equals(250));
    }

    @Test
    public void immutabilityTest17() {
        List<Integer> values = new ArrayList<>();
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", new TypeReference<List<Integer>>() {
        }, values);

        Binding match = bindings.asImmutableBindings().getBinding("x");
        Assert.assertTrue(match.getName().equals("x"));
        Assert.assertTrue(match.getType().equals(new TypeReference<List<Integer>>() {
        }.getType()));
        Assert.assertTrue(match.getValue().equals(values));
    }

    @Test
    public void immutabilityTest18() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(Binding.builder().with(key1 -> "hello world!").build());
        bindings.bind(Binding.builder().with(key2 -> 25).build());
        bindings.bind(Binding.builder().with(key3 -> new BigDecimal("100.00")).build());
        Assert.assertTrue(Bindings.builder().immutable(bindings).size() == 3);
    }

    @Test
    public void immutabilityTest19() {
        Bindings bindings = Bindings.builder().standard();
        Binding binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);

        Binding<String> match = bindings.asImmutableBindings().getBinding("X");
        Assert.assertTrue(match.getName().equals(binding2.getName()));
        match = bindings.getBinding("key");
        Assert.assertTrue(match == null);
    }

    @Test
    public void immutabilityTest20() {
        Bindings bindings = Bindings.builder().standard();
        Binding binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {
        }).build();
        bindings.bind(binding3);

        Assert.assertTrue(bindings.asImmutableBindings().contains("x"));
        Assert.assertTrue(bindings.asImmutableBindings().contains("X"));
        Assert.assertTrue(bindings.asImmutableBindings().contains("X", Integer.class));
        Assert.assertTrue(bindings.asImmutableBindings().contains("x", String.class));
        Assert.assertTrue(bindings.asImmutableBindings().contains("y", new TypeReference<Map<List<Integer>, String>>() {
        }));
        Assert.assertTrue(!bindings.asImmutableBindings().contains("a"));
    }

    @Test
    public void immutabilityTest21() {
        Bindings bindings = Bindings.builder().standard();
        Binding binding1 = Binding.builder().with("x").value("Hello World!").build();
        bindings.bind(binding1);
        Binding binding2 = Binding.builder().with("X").value(101).build();
        bindings.bind(binding2);
        Binding binding3 = Binding.builder().with("y").type(new TypeReference<Map<List<Integer>, String>>() {
        }).build();
        bindings.bind(binding3);

        Binding match1 = bindings.asImmutableBindings().getBinding("x");
        Binding match2 = bindings.asImmutableBindings().getBinding("X", Integer.class);
        Binding match3 = bindings.asImmutableBindings().getBinding("y", new TypeReference<Map<List<Integer>, String>>() {
        }.getType());
        Binding match4 = bindings.asImmutableBindings().getBinding("a");
        Binding match5 = bindings.asImmutableBindings().getBinding("a", Integer.class);
        Binding match6 = bindings.asImmutableBindings().getBinding("a", new TypeReference<Map<List<Integer>, String>>() {
        }.getType());
        Assert.assertTrue(match1.getValue().equals(binding1.getValue()));
        Assert.assertTrue(match2.getValue().equals(binding2.getValue()));
        Assert.assertTrue(match3.getType().equals(binding3.getType()));
        Assert.assertTrue(match4 == null);
        Assert.assertTrue(match5 == null);
        Assert.assertTrue(match6 == null);
    }

    @Test
    public void immutabilityTest22() {
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

        List<Binding<String>> matches1 = bindings.asImmutableBindings().getBindings(String.class);
        Assert.assertTrue(matches1.get(0).equals(binding1));
        Assert.assertTrue(matches1.get(1).equals(binding2));

        List<Binding<Map>> matches2 = bindings.asImmutableBindings().getBindings(Map.class);
        Assert.assertTrue(matches2.get(0).equals(binding3));
        List<Binding<Map<?, ?>>> matches3 = bindings.getBindings(new TypeReference<Map<?, ?>>() {
        }.getType());
        Assert.assertTrue(matches3.size() == 2 && matches3.contains(binding3) && matches3.contains(binding4));
        List<Binding<Map<List<Integer>, String>>> matches4 = bindings.asImmutableBindings()
                .getBindings(new TypeReference<Map<List<Integer>, String>>() {}.getType());
        Assert.assertTrue(matches4.size() == 1 && matches4.get(0).equals(binding3));
        List<Binding<List<Integer>>> matches5 = bindings.asImmutableBindings().getBindings(new TypeReference<List<Integer>>() {
        }.getType());
        Assert.assertTrue(matches5.size() == 1 && matches5.get(0).equals(binding5));
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

        Assert.assertTrue(Bindings.builder().immutable(bindings).getValue("x").equals("Hello World!"));
        Assert.assertTrue(bindings.asImmutableBindings().getValue("y").equals(values));
    }

    @Test(expected = NoSuchBindingException.class)
    public void immutabilityTest24() {
        Bindings bindings = Bindings.builder().standard().asImmutableBindings();
        bindings.getValue("x");
    }

    @Test
    public void immutabilityTest25() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", String.class, "Hello World!");
        Assert.assertTrue(bindings.asImmutableBindings().getValue("x").equals("Hello World!"));
        bindings.setValue("x", "new value");
        Assert.assertTrue(bindings.asImmutableBindings().getValue("x").equals("new value"));
        bindings.bind("y", List.class);
        bindings.setValue("y", new ArrayList<>());
        Assert.assertTrue(bindings.asImmutableBindings().getValue("y").equals(new ArrayList<>()));
    }

    @Test
    public void immutabilityTest26() {
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

        Map<String, ?> bindingsMap = bindings.asImmutableBindings().asMap();
        Assert.assertTrue(bindings.size() == bindingsMap.size());

        for (Binding<?> binding : bindings) {
            Assert.assertTrue(Objects.equals(binding.getValue(), bindingsMap.get(binding.getName())));
        }
    }

    @Test
    public void immutabilityTest27() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("x", int.class, 250);
        bindings.bind("y", Integer.class, 100);

        List<Binding<Integer>> matches = Bindings.builder().immutable(bindings).getBindings(Integer.class);
        Assert.assertTrue(matches.size() == 2);
    }
}
