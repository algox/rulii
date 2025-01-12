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
package org.rulii.test.bind.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.bind.ScopedBindings;
import org.rulii.bind.match.BindingMatch;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.util.TypeReference;

import java.math.BigDecimal;
import java.util.*;

/**
 * Tests for BindingMatchingStrategy.
 *
 * @author Max Arulananthan
 */
public class BindingMatchingStrategyTest {

    public BindingMatchingStrategyTest() {
        super();
    }

    @Test
    public void testMatch1() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class);
        bindings.bind("key2", Integer.class);
        bindings.bind("key3", BigDecimal.class);
        bindings.bind("key4", new TypeReference<List<Long>>() {});
        bindings.bind("key5", new TypeReference<Map<?, ?>>() {});

        Binding<Integer> var2 = bindings.getBinding("key2");
        Binding<Map<?, ?>> var5 = bindings.getBinding("key5");

        List<Binding<Object>> matches = convert(BindingMatchingStrategy.builder().matchByName().match(bindings,
                "key2", Object.class));
        Assertions.assertEquals(1, matches.size());
        Assertions.assertTrue(matches.contains(var2));

        matches = convert(BindingMatchingStrategy.builder().matchByName().match(bindings, "key5", Object.class));
        Assertions.assertEquals(1, matches.size());
        Assertions.assertTrue(matches.contains(var5));

        matches = convert(BindingMatchingStrategy.builder().matchByName().match(bindings, "key6", Object.class));
        Assertions.assertEquals(0, matches.size());
    }

    @Test
    public void testMatch2() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class);
        bindings.bind("key2", Integer.class);
        bindings.bind("key3", BigDecimal.class);
        bindings.bind("key4", new TypeReference<List<Long>>() {});
        bindings.bind("key5", new TypeReference<Map<?, ?>>() {});
        bindings.bind("key6", new TypeReference<Map<String, String>>() {});

        Binding<String> var1 = bindings.getBinding("key1");
        Binding<Integer> var2 = bindings.getBinding("key2");
        Binding<BigDecimal> var3 = bindings.getBinding("key3");
        Binding<List<Long>> var4 = bindings.getBinding("key4");
        Binding<Map<?, ?>> var5 = bindings.getBinding("key5");
        Binding<Map<String, String>> var6 = bindings.getBinding("key6");

        Collection<Binding<Integer>> matches1 = convert(BindingMatchingStrategy.builder().matchByType().match(bindings,
                null, Integer.class));
        Assertions.assertEquals(1, matches1.size());
        Assertions.assertTrue(matches1.contains(var2));

        Collection<Binding<Number>> matches2 = convert(BindingMatchingStrategy.builder().matchByType().match(bindings,
                null, Number.class));
        Assertions.assertEquals(2, matches2.size());
        Assertions.assertTrue(matches2.contains(var3) && matches2.contains(var2));

        Collection<Binding<Map<?, ?>>> matches3 = convert(BindingMatchingStrategy.builder().matchByType().match(bindings,
                null, new TypeReference<Map<?, ?>>() {}.getType()));
        Assertions.assertEquals(2, matches3.size());
        Assertions.assertTrue(matches3.contains(var5) && matches3.contains(var6));

        Collection<Binding<List<Integer>>> matches4 = convert(BindingMatchingStrategy.builder().matchByType()
                .match(bindings, null, new TypeReference<List<Integer>>() {}.getType()));
        Assertions.assertEquals(0, matches4.size());

        Collection<Binding<CharSequence>> matches5 = convert(BindingMatchingStrategy.builder().matchByType().match(bindings,
                null, CharSequence.class));
        Assertions.assertEquals(1, matches5.size());
        Assertions.assertTrue(matches5.contains(var1));

        Collection<Binding<Collection<?>>> matches6 = convert(BindingMatchingStrategy.builder().matchByType().match(bindings,
                null, new TypeReference<Collection<?>>() {}.getType()));
        Assertions.assertEquals(1, matches6.size());
        Assertions.assertTrue(matches6.contains(var4));

        Collection<Binding<Map<?,?>>> matches7 = convert(BindingMatchingStrategy.builder().matchByType().match(bindings,
                null, Map.class));
        Assertions.assertEquals(2, matches7.size());
        Assertions.assertTrue(matches7.contains(var5));
        Assertions.assertTrue(matches7.contains(var6));
    }

    @Test
    public void testMatch3() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class);
        bindings.bind("key2", Integer.class);
        bindings.bind("key3", BigDecimal.class);
        bindings.bind("key4", new TypeReference<List<Long>>() {});
        bindings.bind("key5", new TypeReference<Map<?, ?>>() {});
        bindings.bind("key6", new TypeReference<Map<String, String>>() {});

        Binding<String> var1 = bindings.getBinding("key1");
        Binding<BigDecimal> var3 = bindings.getBinding("key3");
        Binding<Map<?, ?>> var5 = bindings.getBinding("key5");
        Binding<Map<String, String>> var6 = bindings.getBinding("key6");

        Collection<Binding<Integer>> matches1 = convert(BindingMatchingStrategy.builder().matchByName().match(bindings, "key1", Integer.class));
        Assertions.assertEquals(1, matches1.size());
        Assertions.assertTrue(matches1.contains(var1));

        Collection<Binding<BigDecimal>> matches2 = convert(BindingMatchingStrategy.builder().matchByType().match(bindings, "x", BigDecimal.class));
        Assertions.assertEquals(1, matches2.size());
        Assertions.assertTrue(matches2.contains(var3));

        Collection<Binding<Map<?, ?>>> matches3 = convert(BindingMatchingStrategy.builder().matchByType()
                .match(bindings, "x", new TypeReference<Map<?, ?>>() {}.getType()));
        Assertions.assertEquals(2, matches3.size());
        Assertions.assertTrue(matches3.contains(var5) && matches3.contains(var6));

        Collection<Binding<List<Integer>>> matches4 = convert(BindingMatchingStrategy.builder().matchByNameAndType()
                .match(bindings, "x", new TypeReference<List<Integer>>() {}.getType()));
        Assertions.assertEquals(0, matches4.size());
    }

    @Test
    public void testMatch4() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class);
        Binding<Integer> var2 = bindings.bind("key2", Integer.class);
        Binding<BigDecimal> var3 = bindings.bind("key3", BigDecimal.class);
        bindings.bind("key4", new TypeReference<List<Long>>() {});
        bindings.bind("key5", new TypeReference<Map<?, ?>>() {});
        bindings.bind("key6", new TypeReference<Map<String, String>>() {});

        Collection<Binding<Number>> matches1 = convert(BindingMatchingStrategy.builder().matchByNameAndType()
                .match(bindings, "key3", Number.class));
        Assertions.assertEquals(1, matches1.size());
        Assertions.assertTrue(matches1.contains(var3));

        Collection<Binding<Number>> matches2 = convert(BindingMatchingStrategy.builder().matchByNameAndTypeThenByJustType().match(bindings, "key7", Number.class));
        Assertions.assertEquals(2, matches2.size());
        Assertions.assertTrue(matches2.contains(var3) && matches2.contains(var2));
    }

    @Test
    public void testMatch5() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("key1", String.class);
        Binding<Integer> var2 = bindings.bind("key2", Integer.class);
        Binding<BigDecimal> var3 = bindings.bind("key3", BigDecimal.class);
        bindings.bind("key4", new TypeReference<List<Long>>() {});
        bindings.bind("key5", new TypeReference<Map<?, ?>>() {});
        bindings.bind("key6", new TypeReference<Map<String, String>>() {});
        Binding<TestClass> var7 = bindings.bind("key7", new TestClass());

        Collection<Binding<Number>> matches1 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "key3", Number.class));
        Assertions.assertEquals(1, matches1.size());
        Assertions.assertTrue(matches1.contains(var3));

        Collection<Binding<TestClass>> matches2 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "x", TestClass.class));
        Assertions.assertEquals(1, matches2.size());
        Assertions.assertTrue(matches2.contains(var7));

        Collection<Binding<Integer>> matches3 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "x", Integer.class));
        Assertions.assertEquals(1, matches3.size());
        Assertions.assertTrue(matches3.contains(var2));

        Collection<Binding<BigDecimal>> matches4 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "x", BigDecimal.class));
        Assertions.assertEquals(1, matches4.size());
        Assertions.assertTrue(matches4.contains(var3));

        Collection<Binding<Map<?,?>>> matches5 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "x", Map.class, false));
        Assertions.assertEquals(2, matches5.size());
    }

    @Test
    public void testMatch6() {
        ScopedBindings bindings = Bindings.builder().scoped();
        Binding<String> var1 = bindings.bind("key1", String.class);
        bindings.bind("key2", Integer.class);
        Binding<TestClass> var2 = bindings.bind("testClass", new TestClass());

        bindings.addScope();
        bindings.bind("key3", BigDecimal.class);
        Binding<TestClass> var3 = bindings.bind("testClass", new TestClass());
        bindings.addScope();
        Binding<Long> var4 = bindings.bind("key1", Long.class, 1000L);

        Collection<Binding<Long>> matches1 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "key1", Long.class));
        Assertions.assertEquals(1, matches1.size());
        Assertions.assertTrue(matches1.contains(var4));

        Collection<Binding<TestClass>> matches3 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "testClass", TestClass.class));
        Assertions.assertEquals(1, matches3.size());
        Assertions.assertTrue(matches3.contains(var3));

        bindings.removeScope();

        Collection<Binding<String>> matches2 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "key1", String.class));
        Assertions.assertEquals(1, matches2.size());
        Assertions.assertFalse(matches2.contains(var4));
        Assertions.assertTrue(matches2.contains(var1));

        bindings.removeScope();

        Collection<Binding<TestClass>> matches4 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "testClass", TestClass.class));
        Assertions.assertEquals(1, matches4.size());
        Assertions.assertTrue(matches4.contains(var2));
    }

    @Test
    public void testMatch7() {
        ScopedBindings bindings = Bindings.builder().scoped();
        Binding<String> var1 = bindings.bind("key1", String.class);
        bindings.bind("key2", Integer.class);
        Binding<TestClass> var2 = bindings.bind("testClass", new TestClass());

        bindings.addScope();
        Binding<BigDecimal> var3 = bindings.bind("key3", BigDecimal.class);

        Collection<Binding<TestClass>> matches1 = convert(BindingMatchingStrategy.builder().matchByName()
                .match(bindings, "testClass", TestClass.class));
        Assertions.assertEquals(1, matches1.size());
        Assertions.assertTrue(matches1.contains(var2));

        Collection<Binding<String>> matches2 = convert(BindingMatchingStrategy.builder().matchByType()
                .match(bindings, "x", String.class));
        Assertions.assertEquals(1, matches1.size());
        Assertions.assertTrue(matches2.contains(var1));

        Collection<Binding<String>> matches3 = convert(BindingMatchingStrategy.builder().matchByType()
                .match(bindings, "key1", String.class));
        Assertions.assertEquals(1, matches3.size());
        Assertions.assertTrue(matches3.contains(var1));

        Collection<Binding<TestClass>> matches4 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "x", TestClass.class));
        Assertions.assertEquals(1, matches4.size());
        Assertions.assertTrue(matches4.contains(var2));

        Collection<Binding<TestClass>> matches5 = convert(BindingMatchingStrategy.builder().matchByNameAndType()
                .match(bindings, "key1", TestClass.class));
        Assertions.assertEquals(0, matches5.size());

        Collection<Binding<TestClass>> matches6 = convert(BindingMatchingStrategy.builder().matchByNameAndTypeThenByJustType()
                .match(bindings, "x", TestClass.class));
        Assertions.assertEquals(1, matches6.size());
        Assertions.assertTrue(matches6.contains(var2));

        Collection<Binding<BigDecimal>> matches7 = convert(BindingMatchingStrategy.builder().matchByNameAndType()
                .match(bindings, "key3", BigDecimal.class));
        Assertions.assertEquals(1, matches7.size());
        Assertions.assertTrue(matches7.contains(var3));
    }

    @Test
    public void testMatch8() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", String.class);
        bindings.bind("b", Integer.class);
        bindings.bind("testClass", new TestClass());
        Binding<BigDecimal> var3 = bindings.bind("key3", BigDecimal.class);

        bindings.addScope();
        Binding<Integer> var4 = bindings.bind("c", int.class);

        Collection<Binding<TestClass>> matches1 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "z", Integer.class, false));
        Assertions.assertEquals(2, matches1.size());
        Assertions.assertTrue(matches1.contains(var4));

        Collection<Binding<TestClass>> matches2 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "z", BigDecimal.class, false));
        Assertions.assertEquals(1, matches2.size());
        Assertions.assertTrue(matches2.contains(var3));
    }

    @Test
    public void testMatch9() {
        ScopedBindings bindings = Bindings.builder().scoped();
        Binding<List<String>> var1 = bindings.bind("a", new TypeReference<List<String>>() {
        });
        Binding<Set<Integer>> var2 = bindings.bind("b", new TypeReference<Set<Integer>>() {
        });
        Binding<TestClass> var3 = bindings.bind("testClass", new TestClass());
        bindings.bind("key3", new TypeReference<Map<String, BigDecimal>>() {});

        bindings.addScope();
        Binding<Integer> var5 = bindings.bind("c", int.class);
        Binding<TestClassDeux<TestClass>> var6 = bindings.bind("x", new TypeReference<TestClassDeux<TestClass>>() {
        });

        Collection<Binding<TestClass>> matches1 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "z", Collection.class, false));
        Assertions.assertEquals(2, matches1.size());
        Assertions.assertTrue(matches1.contains(var1));
        Assertions.assertTrue(matches1.contains(var2));

        Collection<Binding<Set<Integer>>> matches2 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "z", new TypeReference<Set<Integer>>(){}.getType(), false));
        Assertions.assertEquals(0, matches2.size());

        Collection<Binding<Set<Integer>>> matches3 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "z", new TypeReference<Set<Integer>>(){}.getType(), true));
        Assertions.assertEquals(1, matches3.size());

        Collection<Binding<Set<Integer>>> matches4 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "z", new TypeReference<TestClassDeux<TestClass>>(){}.getType(), true));
        Assertions.assertEquals(1, matches4.size());
        Assertions.assertTrue(matches4.contains(var6));

        Collection<Binding<Set<Integer>>> matches5 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "z", new TypeReference<TestClassDeux<Integer>>(){}.getType(), true));
        Assertions.assertEquals(0, matches5.size());

        Collection<Binding<Set<Integer>>> matches6 = convert(BindingMatchingStrategy.builder().matchByNameThenByType()
                .match(bindings, "z", new TypeReference<Map<?,?>>(){}.getType(), false));
        Assertions.assertEquals(0, matches6.size());

        Collection<Binding<TestClass>> matches7 = convert(BindingMatchingStrategy.builder().matchByName()
                .match(bindings, "testClass", new TypeReference<TestClass>(){}.getType(), false));
        Assertions.assertEquals(1, matches7.size());
        Assertions.assertTrue(matches7.contains(var3));

        Collection<Binding<Integer>> matches8 = convert(BindingMatchingStrategy.builder().matchByType()
                .match(bindings, "z", new TypeReference<Integer>(){}.getType(), false));
        Assertions.assertEquals(1, matches8.size());
        Assertions.assertTrue(matches8.contains(var5));
    }

    @Test
    public void testMatch10() {
        ScopedBindings bindings = Bindings.builder().scoped();
        bindings.bind("a", new TypeReference<TestClass>() {
        });
        Binding<TestClassDeux<TestClass>> var2 = bindings.bind("b", new TypeReference<TestClassDeux<TestClass>>() {
        });

        bindings.addScope();
        bindings.bind("c", int.class);
        bindings.addScope();
        bindings.bind("d", int.class);
        bindings.addScope();
        Binding<TestClassDeux<Integer>> var7 = bindings.bind("e", new TypeReference<TestClassDeux<Integer>>() {
        });

        Collection<Binding<TestClassDeux<TestClass>>> matches1 = convert(BindingMatchingStrategy.builder().matchByNameAndType()
                .match(bindings, "b", new TypeReference<TestClassDeux<TestClass>>(){}.getType(), true));
        Assertions.assertEquals(1, matches1.size());
        Assertions.assertTrue(matches1.contains(var2));

        Collection<Binding<TestClassDeux<TestClass>>> matches2 = convert(BindingMatchingStrategy.builder().matchByNameAndTypeThenByJustType()
                .match(bindings, "z", new TypeReference<TestClassDeux<TestClass>>(){}.getType(), true));
        Assertions.assertEquals(1, matches2.size());
        Assertions.assertTrue(matches2.contains(var2));

        Collection<Binding<TestClassDeux<?>>> matches3 = convert(BindingMatchingStrategy.builder().matchByNameAndTypeThenByJustType()
                .match(bindings, "z", new TypeReference<TestClassDeux<?>>(){}.getType(), true));
        Assertions.assertEquals(2, matches3.size());
        Assertions.assertTrue(matches3.contains(var2));
        Assertions.assertTrue(matches3.contains(var7));
    }
    private static class TestClass {}

    private static class TestClassDeux<T> {
        private final List<T> values = new ArrayList<>();

        public List<T> getValues() {
            return values;
        }
    }

    private static <T> List<Binding<T>> convert(List<BindingMatch<T>> matches) {
        if (matches == null) return null;
        List<Binding<T>> result = new ArrayList<>();
        matches.forEach(m -> result.add(m.getBinding()));
        return result;
    }
}
