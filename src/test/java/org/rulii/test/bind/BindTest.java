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
import org.rulii.bind.BindingBuilder;
import org.rulii.bind.InvalidBindingException;
import org.rulii.lib.apache.reflect.TypeUtils;
import org.rulii.util.TypeReference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * More Tests for Bindings/BindBuilder.
 *
 * @author Max Arulananthan
 */
public class BindTest {

    public BindTest() {
        super();
    }

    @Test
    public void testBuilder() {
        Binding<?> binding = Binding.builder().with("name").build();
        Assertions.assertEquals(Object.class, binding.getType());
    }

    @Test
    public void testBindingBuilder() {
        Binding<Integer> binding = Binding.builder().with("a").type(Integer.class).value(200).build();
        Assertions.assertEquals("a", binding.getName());
        Assertions.assertEquals(200, (int) binding.getValue());
    }

    @Test
    public void testBind1() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Binding.builder().with("a b c");
        });
    }

    @Test
    public void testBind2() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Binding.builder().with((String) null);
        });
    }

    @Test
    public void testBind3() {
        BindingBuilder bindingBuilder = Binding.builder().with("a");
        Assertions.assertNotNull(bindingBuilder);
    }

    @Test
    public void testBind4() {
        BindingBuilder bindingBuilder = Binding.builder().with(a -> null);
        Assertions.assertNotNull(bindingBuilder);
        Binding<String> binding = Binding.builder().with(a -> "hello").build();
        Assertions.assertNotNull(binding);
        Assertions.assertEquals("a", binding.getName());
        Assertions.assertEquals("hello", binding.getValue());
        Assertions.assertEquals(String.class, binding.getType());
    }

    @Test
    public void testBind5() {
        Binding<String> binding = Binding.builder()
                .with("a")
                .value("hello")
                .build();
        Assertions.assertEquals(binding.getName(), "a");
        Assertions.assertEquals(binding.getValue(), "hello");
        Assertions.assertEquals(binding.getType(), String.class);
    }

    @Test
    public void testBind6() {
        Binding<String> binding = Binding.builder()
                .with("someBinding")
                .type(String.class)
                .value("value")
                .build();
        Assertions.assertEquals(binding.getName(), "someBinding");
        Assertions.assertEquals(binding.getValue(), "value");
        Assertions.assertEquals(binding.getType(), String.class);
    }

    @Test
    public void testBind7() {
        Assertions.assertThrows(InvalidBindingException.class, () -> {
            Binding.builder()
                    .with("someBinding")
                    .type(Integer.class)
                    .value("value")
                    .build();
        });
    }

    @Test
    public void testBind8() {
        Binding<List<Integer>> binding = Binding.builder()
                .with("someBinding")
                .type(new TypeReference<List<Integer>>() {})
                .build();
        Assertions.assertEquals(binding.getType(), new TypeReference<List<Integer>>() {}.getType());
        Assertions.assertTrue(TypeUtils.isAssignable(binding.getType(), new TypeReference<List<Integer>>() {}.getType()));
    }

    @Test
    public void testBind9() {
        List<Integer> values = new ArrayList<>();
        values.add(1);

        Binding<List<Integer>> binding = Binding.builder()
                .with("someBinding")
                .type(new TypeReference<List<Integer>>() {})
                .value(values)
                .build();

        Assertions.assertTrue(binding.isEditable());
    }

    @Test
    public void testBind10() {
        List<Integer> values = new ArrayList<>();
        values.add(1);

        Binding<List<Integer>> binding = Binding.builder()
                .with("someBinding")
                .type(new TypeReference<List<Integer>>() {})
                .value(() -> values)
                .build();

        Assertions.assertFalse(binding.isEditable());
        Assertions.assertEquals(values, binding.getValue());
    }

    @Test
    public void testBind11() {
        AtomicInteger value = new AtomicInteger(10);

        Binding<Integer> binding = Binding.builder()
                .with("someBinding")
                .type(Integer.class)
                .delegate(() -> value.get(), (Integer x) -> value.set(x))
                .build();

        Assertions.assertEquals(10, binding.getValue());
        binding.setValue(25);
        Assertions.assertEquals(25, binding.getValue());
    }

    @Test
    public void testBind12() {
        Binding<Integer> binding = Binding.builder()
                .with("someBinding")
                .type(Integer.class)
                .description("some description")
                .primary(true)
                .build();

        Assertions.assertTrue(binding.isPrimary());
        Assertions.assertEquals("some description", binding.getDescription());
    }

    @Test
    public void testBind13() {
        Assertions.assertThrows(InvalidBindingException.class, () -> {
            Binding<Object> binding = Binding.builder()
                    .with("b", new TypeReference<Map<String, List<BigDecimal>>>() {
                    }).build();
            binding.setValue(new ArrayList<>());
        });
    }

    @Test
    public void testBind14() {
        Binding<Map<String, List<BigDecimal>>> binding = Binding.builder()
                .with("b", new TypeReference<Map<String, List<BigDecimal>>>() {}).build();
        Map<String, List<BigDecimal>> value = new HashMap<>();
        value.put("value1", List.of());
        binding.setValue(value);
    }

    @Test
    public void testBind15() {
        Binding<Map<String, List<?>>> binding = Binding.builder()
                .with("b", new TypeReference<Map<String, List<?>>>() {}).build();
        Assertions.assertTrue(binding.isTypeAcceptable(new TypeReference<Map<String, List<?>>>() {}.getType()));
    }

    @Test
    public void testBind16() {
        Binding<List<?>> binding = Binding.builder()
                .with("b", ArrayList.class).build();
        Assertions.assertTrue(binding.isAssignable(List.class));
    }

    @Test
    public void testBind17() {
        Assertions.assertThrows(InvalidBindingException.class, () -> {
            Binding<String> binding = Binding.builder().constant("c", "hello world!");
            binding.setValue("new value");
        });
    }

    @Test
    public void testBind18() {
        Binding<BigDecimal> b = Binding.builder().build("binding", BigDecimal.class);
        Assertions.assertEquals("BigDecimal", b.getTypeName());
    }

    @Test
    public void testBind19() {
        Binding<Map<String, List<?>>> b = Binding.builder().build("binding", new TypeReference<Map<String, List<?>>>() {});
        Assertions.assertEquals("java.util.Map<java.lang.String, java.util.List<?>>", b.getTypeName());
    }

    @Test
    public void testBind20() {
        Binding<String> b = Binding.builder().build("binding", String.class);
        Assertions.assertEquals("String binding", b.getTypeAndName());
    }
}

