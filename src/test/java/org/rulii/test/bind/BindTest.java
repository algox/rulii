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
import org.rulii.bind.BindingBuilder;
import org.rulii.bind.InvalidBindingException;
import org.rulii.lib.apache.reflect.TypeUtils;
import org.rulii.util.TypeReference;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;
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
        Binding binding = Binding.builder().with("name").build();
        Assert.assertTrue(binding != null && Object.class.equals(binding.getType()));
    }

    @Test
    public void testBindingBuilder() {
        Binding binding = Binding.builder().with("a").type(Integer.class).value(200).build();
        Assert.assertTrue(binding.getName().equals("a"));
        Assert.assertTrue(binding.getValue().equals(200));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBind1() {
        Binding.builder().with("a b c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBind2() {
        Binding.builder().with((String) null);
    }

    @Test
    public void testBind3() {
        BindingBuilder bindingBuilder = Binding.builder().with("a");
        Assert.assertTrue(bindingBuilder != null);
    }

    @Test
    public void testBind4() {
        BindingBuilder bindingBuilder = Binding.builder().with(a -> null);
        Assert.assertTrue(bindingBuilder != null);
        Binding binding = Binding.builder().with(a -> "hello").build();
        Assert.assertTrue(binding != null);
        Assert.assertEquals("a", binding.getName());
        Assert.assertEquals("hello", binding.getValue());
        Assert.assertEquals(String.class, binding.getType());
    }

    @Test
    public void testBind5() {
        Binding binding = Binding.builder()
                .with("a")
                .value("hello")
                .build();
        Assert.assertEquals(binding.getName(), "a");
        Assert.assertEquals(binding.getValue(), "hello");
        Assert.assertEquals(binding.getType(), String.class);
    }

    @Test
    public void testBind6() {
        Binding binding = Binding.builder()
                .with("someBinding")
                .type(String.class)
                .value("value")
                .build();
        Assert.assertEquals(binding.getName(), "someBinding");
        Assert.assertEquals(binding.getValue(), "value");
        Assert.assertEquals(binding.getType(), String.class);
    }

    @Test(expected = InvalidBindingException.class)
    public void testBind7() {
        Binding binding = Binding.builder()
                .with("someBinding")
                .type(Integer.class)
                .value("value")
                .build();
    }

    @Test
    public void testBind8() {
        Binding binding = Binding.builder()
                .with("someBinding")
                .type(new TypeReference<List<Integer>>() {})
                .build();
        Assert.assertEquals(binding.getType(), new TypeReference<List<Integer>>() {}.getType());
        Assert.assertTrue(TypeUtils.isAssignable(binding.getType(), new TypeReference<List<Integer>>() {}.getType()));
    }

    @Test
    public void testBind9() {
        List<Integer> values = new ArrayList<>();
        values.add(1);

        Binding binding = Binding.builder()
                .with("someBinding")
                .type(new TypeReference<List<Integer>>() {})
                .value(values)
                .build();

        Assert.assertTrue(binding.isEditable());
    }

    @Test
    public void testBind10() {
        List<Integer> values = new ArrayList<>();
        values.add(1);

        Binding binding = Binding.builder()
                .with("someBinding")
                .type(new TypeReference<List<Integer>>() {})
                .value(() -> values)
                .build();

        Assert.assertTrue(!binding.isEditable());
        Assert.assertEquals(values, binding.getValue());
    }

    @Test
    public void testBind11() {
        AtomicInteger value = new AtomicInteger(10);

        Binding binding = Binding.builder()
                .with("someBinding")
                .type(Integer.class)
                .delegate(() -> value.get(), (Integer x) -> value.set(x))
                .build();

        Assert.assertEquals(10, binding.getValue());
        binding.setValue(25);
        Assert.assertEquals(25, binding.getValue());
    }

    @Test
    public void testBind12() {
        Binding binding = Binding.builder()
                .with("someBinding")
                .type(Integer.class)
                .description("some description")
                .primary(true)
                .build();

        Assert.assertEquals(true, binding.isPrimary());
        Assert.assertEquals("some description", binding.getDescription());
    }

    @Test(expected = InvalidBindingException.class)
    public void testBind13() {
        Binding binding = Binding.builder()
                .with("b", new TypeReference<Map<String, List<BigDecimal>>>() {}).build();
        binding.setValue(new ArrayList<>());
    }

    @Test
    public void testBind14() {
        Binding binding = Binding.builder()
                .with("b", new TypeReference<Map<String, List<BigDecimal>>>() {}).build();
        Map<String, List<BigDecimal>> value = new HashMap<>();
        value.put("value1", Arrays.asList());
        binding.setValue(value);
    }

    @Test
    public void testBind15() {
        Binding binding = Binding.builder()
                .with("b", new TypeReference<Map<String, List<?>>>() {}).build();
        Assert.assertTrue(binding.isTypeAcceptable(new TypeReference<Map<String, List<?>>>() {}.getType()));
    }

    @Test
    public void testBind16() {
        Binding binding = Binding.builder()
                .with("b", ArrayList.class).build();
        Assert.assertTrue(binding.isAssignable(List.class));
    }

    @Test(expected = InvalidBindingException.class)
    public void testBind17() {
        Binding binding = Binding.builder().constant("c", "hello world!");
        binding.setValue("new value");
    }

    @Test
    public void testBind18() {
        Binding b = Binding.builder().build("binding", BigDecimal.class);
        Assert.assertTrue("BigDecimal".equals(b.getTypeName()));
    }

    @Test
    public void testBind19() {
        Binding b = Binding.builder().build("binding", new TypeReference<Map<String, List<?>>>() {});
        Assert.assertTrue("java.util.Map<java.lang.String, java.util.List<?>>".equals(b.getTypeName()));
    }

    @Test
    public void testBind20() {
        Binding b = Binding.builder().build("binding", String.class);
        Assert.assertTrue("String binding".equals(b.getTypeAndName()));
    }
}

