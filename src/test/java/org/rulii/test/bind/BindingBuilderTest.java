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
import org.rulii.bind.InvalidBindingException;
import org.rulii.util.TypeReference;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for BindingBuilder.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class BindingBuilderTest {

    public BindingBuilderTest() {
        super();
    }

    @Test
    public void bindingDeclarationTest() {
        Binding binding = Binding.builder().with(key1 -> new BigDecimal("100.01")).build();
        Assert.assertTrue(binding.getName().equals("key1"));
        Assert.assertTrue(binding.getValue().equals(new BigDecimal("100.01")));
        binding = Binding.builder().with(key2 -> null).build();
        Assert.assertTrue(binding.getName().equals("key2"));
        Assert.assertTrue(binding.getValue() == null);
        String[] values = {"a", "b", "c"};
        binding = Binding.builder().with(key3 -> values).build();
        Assert.assertTrue(binding.getName().equals("key3"));
        Assert.assertTrue(binding.getValue().equals(values));
        binding = Binding.builder().with(key4 -> "1").build();
        Assert.assertTrue(binding.getName().equals("key4"));
        Assert.assertTrue(binding.getValue().equals("1"));
    }

    @Test
    public void bindUsingNameTest() {
        Binding binding = Binding.builder().with("key1").build();
        Assert.assertTrue(binding.getName().equals("key1"));
        Assert.assertTrue(binding.getType().equals(Object.class));
        Assert.assertTrue(binding.getValue() == null);
        Assert.assertTrue(binding.getTextValue() == null);
        Assert.assertTrue(binding.getDescription() == null);
        Assert.assertTrue(binding.isPrimary() == false);
    }

    @Test
    public void bindUsingNameClassTypeTest() {
        Binding binding = Binding.builder().with("key1").type(String.class).build();
        Assert.assertTrue(binding.getName().equals("key1"));
        Assert.assertTrue(binding.getType().equals(String.class));
        Assert.assertTrue(binding.getValue() == null);
        Assert.assertTrue(binding.getTextValue() == null);
        Assert.assertTrue(binding.getDescription() == null);
        Assert.assertTrue(binding.isPrimary() == false);
    }

    @Test
    public void bindUsingNameTypeTest() {
        Binding binding = Binding.builder().with("key1").type(new TypeReference<List<String>>() {}.getType()).build();
        Assert.assertTrue(binding.getName().equals("key1"));
        Assert.assertTrue(binding.getType().equals(new TypeReference<List<String>>() {}.getType()));
        Assert.assertTrue(binding.getValue() == null);
        Assert.assertTrue(binding.getTextValue() == null);
        Assert.assertTrue(binding.getDescription() == null);
        Assert.assertTrue(binding.isPrimary() == false);
    }

    @Test
    public void bindUsingNameTypeReferenceTest() {
        Binding binding = Binding.builder().with("key1").type(new TypeReference<List<String>>() {}).build();
        Assert.assertTrue(binding.getName().equals("key1"));
        Assert.assertTrue(binding.getType().equals(new TypeReference<List<String>>() {}.getType()));
        Assert.assertTrue(binding.getValue() == null);
        Assert.assertTrue(binding.getTextValue() == null);
        Assert.assertTrue(binding.getDescription() == null);
        Assert.assertTrue(binding.isPrimary() == false);
    }

    @Test
    public void bindUsingNameTypeValueTest() {
        List<String> values = new ArrayList<>();
        Binding binding = Binding.builder().with("key1")
                .type(new TypeReference<List<String>>() {}).value(ArrayList::new).build();
        Assert.assertTrue(binding.getName().equals("key1"));
        Assert.assertTrue(binding.getType().equals(new TypeReference<List<String>>() {}.getType()));
        Assert.assertTrue(binding.getValue().equals(values));
        Assert.assertTrue(binding.getDescription() == null);
        Assert.assertTrue(binding.isPrimary() == false);
    }

    @Test
    public void bindUsingNameTypeValueDescriptionTest() {
        List<String> values = new ArrayList<>();
        Binding binding = Binding.builder().with("key1")
                .type(new TypeReference<List<String>>() {}).value(ArrayList::new).description("some description").build();
        Assert.assertTrue(binding.getName().equals("key1"));
        Assert.assertTrue(binding.getType().equals(new TypeReference<List<String>>() {}.getType()));
        Assert.assertTrue(binding.getValue().equals(values));
        Assert.assertTrue(binding.getDescription().equals("some description"));
        Assert.assertTrue(binding.isPrimary() == false);
    }

    @Test
    public void bindUsingNameTypeValuesTest() {
        Binding binding = Binding.builder()
                .with("key1")
                .value(() -> "Hello world!")
                .type(String.class)
                .build();
        Assert.assertTrue(binding.getName().equals("key1"));
        Assert.assertTrue(binding.getType().equals(String.class));
        Assert.assertTrue(binding.getValue().equals("Hello world!"));

        Optional<String> value = Optional.empty();
        binding = Binding.builder().with("key2").value(value).build();
        Assert.assertTrue(binding.getName().equals("key2"));
        Assert.assertTrue(binding.getType().equals(Optional.class));
        Assert.assertTrue(binding.getValue().equals(value));

        value = Optional.of("Hello world!");
        binding = Binding.builder().with("key2").value(value).build();
        Assert.assertTrue(binding.getName().equals("key2"));
        Assert.assertTrue(binding.getType().equals(Optional.class));
        Assert.assertTrue(binding.getValue().equals(value));
    }

    @Test(expected = InvalidBindingException.class)
    public void bindEditableTest() {
        Binding binding1 = Binding.builder().with("key1").value(() -> "Hello world!").editable(true).build();
        Assert.assertTrue(!binding1.isEditable());
        Binding binding2 = Binding.builder().with("key2").value(() -> "Hello world!").editable(false).build();
        Assert.assertTrue(!binding2.isEditable());
        binding2.setValue("new value");
    }

    @Test
    public void bindPrimaryTest() {
        Binding binding1 = Binding.builder().with("key1").value(() -> "Hello world!").primary(true).build();
        Assert.assertTrue(binding1.isPrimary());
        Binding binding2 = Binding.builder().with("key1").value(() -> "Hello world!").primary(false).build();
        Assert.assertTrue(!binding2.isPrimary());
    }

    @Test
    public void bindPrimitiveTest() {
        Binding binding = Binding.builder().with("key1").type(float.class).build();
        Assert.assertTrue(binding.getType().equals(float.class));
        Assert.assertTrue(binding.getValue().equals(0.0f));
    }

    @Test
    public void bindDeclaration() {
        Binding binding = Binding.builder().with(a -> "hello world").build();
        Assert.assertTrue(binding.getName().equals("a"));
        Assert.assertTrue(binding.getType().equals(String.class));
        Assert.assertTrue(binding.getValue().equals("hello world"));
    }

    @Test
    public void bindNameAndValue() {
        Binding binding = Binding.builder().with("a", "hello world").build();
        Assert.assertTrue(binding.getName().equals("a"));
        Assert.assertTrue(binding.getType().equals(String.class));
        Assert.assertTrue(binding.getValue().equals("hello world"));
    }

    @Test
    public void bindNameAndType() {
        Binding binding = Binding.builder().with("a", String.class).build();
        Assert.assertTrue(binding.getName().equals("a"));
        Assert.assertTrue(binding.getType().equals(String.class));
    }

    @Test
    public void bindNameAndTypeReference() {
        Binding binding = Binding.builder().with("a", new TypeReference<Map<String, BigDecimal>>() {}).build();
        Assert.assertTrue(binding.getName().equals("a"));
        Assert.assertTrue(binding.getType().equals(new TypeReference<Map<String, BigDecimal>>() {}.getType()));
    }
}
