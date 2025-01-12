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

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        Binding<?> binding = Binding.builder().with(key1 -> new BigDecimal("100.01")).build();
        Assertions.assertEquals("key1", binding.getName());
        Assertions.assertEquals(binding.getValue(), new BigDecimal("100.01"));
        binding = Binding.builder().with(key2 -> null).build();
        Assertions.assertEquals("key2", binding.getName());
        Assertions.assertNull(binding.getValue());
        String[] values = {"a", "b", "c"};
        binding = Binding.builder().with(key3 -> values).build();
        Assertions.assertEquals("key3", binding.getName());
        Assertions.assertEquals(binding.getValue(), values);
        binding = Binding.builder().with(key4 -> "1").build();
        Assertions.assertEquals("key4", binding.getName());
        Assertions.assertEquals("1", binding.getValue());
    }

    @Test
    public void bindUsingNameTest() {
        Binding<?> binding = Binding.builder().with("key1").build();
        Assertions.assertEquals("key1", binding.getName());
        Assertions.assertEquals(binding.getType(), Object.class);
        Assertions.assertNull(binding.getValue());
        Assertions.assertNull(binding.getTextValue());
        Assertions.assertNull(binding.getDescription());
        Assertions.assertFalse(binding.isPrimary());
    }

    @Test
    public void bindUsingNameClassTypeTest() {
        Binding<String> binding = Binding.builder().with("key1").type(String.class).build();
        Assertions.assertEquals("key1", binding.getName());
        Assertions.assertEquals(binding.getType(), String.class);
        Assertions.assertNull(binding.getValue());
        Assertions.assertNull(binding.getTextValue());
        Assertions.assertNull(binding.getDescription());
        Assertions.assertFalse(binding.isPrimary());
    }

    @Test
    public void bindUsingNameTypeTest() {
        Binding<List<String>> binding = Binding.builder().with("key1").type(new TypeReference<List<String>>() {}.getType()).build();
        Assertions.assertEquals("key1", binding.getName());
        Assertions.assertEquals(binding.getType(), new TypeReference<List<String>>() {
        }.getType());
        Assertions.assertNull(binding.getValue());
        Assertions.assertNull(binding.getTextValue());
        Assertions.assertNull(binding.getDescription());
        Assertions.assertFalse(binding.isPrimary());
    }

    @Test
    public void bindUsingNameTypeReferenceTest() {
        Binding<List<String>> binding = Binding.builder().with("key1").type(new TypeReference<List<String>>() {}).build();
        Assertions.assertEquals("key1", binding.getName());
        Assertions.assertEquals(binding.getType(), new TypeReference<List<String>>() {
        }.getType());
        Assertions.assertNull(binding.getValue());
        Assertions.assertNull(binding.getTextValue());
        Assertions.assertNull(binding.getDescription());
        Assertions.assertFalse(binding.isPrimary());
    }

    @Test
    public void bindUsingNameTypeValueTest() {
        List<String> values = new ArrayList<>();
        Binding<List<String>> binding = Binding.builder().with("key1")
                .type(new TypeReference<List<String>>() {}).value(ArrayList::new).build();
        Assertions.assertEquals("key1", binding.getName());
        Assertions.assertEquals(binding.getType(), new TypeReference<List<String>>() {
        }.getType());
        Assertions.assertEquals(binding.getValue(), values);
        Assertions.assertNull(binding.getDescription());
        Assertions.assertFalse(binding.isPrimary());
    }

    @Test
    public void bindUsingNameTypeValueDescriptionTest() {
        List<String> values = new ArrayList<>();
        Binding<List<String>> binding = Binding.builder().with("key1")
                .type(new TypeReference<List<String>>() {}).value(ArrayList::new).description("some description").build();
        Assertions.assertEquals("key1", binding.getName());
        Assertions.assertEquals(binding.getType(), new TypeReference<List<String>>() {
        }.getType());
        Assertions.assertEquals(binding.getValue(), values);
        Assertions.assertEquals("some description", binding.getDescription());
        Assertions.assertFalse(binding.isPrimary());
    }

    @Test
    public void bindUsingNameTypeValuesTest() {
        Binding<?> binding = Binding.builder()
                .with("key1")
                .value(() -> "Hello world!")
                .type(String.class)
                .build();
        Assertions.assertEquals("key1", binding.getName());
        Assertions.assertEquals(binding.getType(), String.class);
        Assertions.assertEquals("Hello world!", binding.getValue());

        Optional<String> value = Optional.empty();
        binding = Binding.builder().with("key2").value(value).build();
        Assertions.assertEquals("key2", binding.getName());
        Assertions.assertEquals(binding.getType(), Optional.class);
        Assertions.assertEquals(binding.getValue(), value);

        value = Optional.of("Hello world!");
        binding = Binding.builder().with("key2").value(value).build();
        Assertions.assertEquals("key2", binding.getName());
        Assertions.assertEquals(binding.getType(), Optional.class);
        Assertions.assertEquals(binding.getValue(), value);
    }

    @Test
    public void bindEditableTest() {
        Assertions.assertThrows(InvalidBindingException.class, () -> {
            Binding<String> binding1 = Binding.builder().with("key1").value(() -> "Hello world!").editable(true).build();
            Assertions.assertFalse(binding1.isEditable());
            Binding<String> binding2 = Binding.builder().with("key2").value(() -> "Hello world!").editable(false).build();
            Assertions.assertFalse(binding2.isEditable());
            binding2.setValue("new value");
        });
    }

    @Test
    public void bindPrimaryTest() {
        Binding<String> binding1 = Binding.builder().with("key1").value(() -> "Hello world!").primary(true).build();
        Assertions.assertTrue(binding1.isPrimary());
        Binding<String> binding2 = Binding.builder().with("key1").value(() -> "Hello world!").primary(false).build();
        Assertions.assertFalse(binding2.isPrimary());
    }

    @Test
    public void bindPrimitiveTest() {
        Binding<Float> binding = Binding.builder().with("key1").type(float.class).build();
        Assertions.assertEquals(binding.getType(), float.class);
        Assertions.assertEquals(0.0f, binding.getValue());
    }

    @Test
    public void bindDeclaration() {
        Binding<String> binding = Binding.builder().with(a -> "hello world").build();
        Assertions.assertEquals("a", binding.getName());
        Assertions.assertEquals(binding.getType(), String.class);
        Assertions.assertEquals("hello world", binding.getValue());
    }

    @Test
    public void bindNameAndValue() {
        Binding<String> binding = Binding.builder().with("a", "hello world").build();
        Assertions.assertEquals("a", binding.getName());
        Assertions.assertEquals(binding.getType(), String.class);
        Assertions.assertEquals("hello world", binding.getValue());
    }

    @Test
    public void bindNameAndType() {
        Binding<String> binding = Binding.builder().with("a", String.class).build();
        Assertions.assertEquals("a", binding.getName());
        Assertions.assertEquals(binding.getType(), String.class);
    }

    @Test
    public void bindNameAndTypeReference() {
        Binding<Map<String, BigDecimal>> binding = Binding.builder().with("a", new TypeReference<Map<String, BigDecimal>>() {}).build();
        Assertions.assertEquals("a", binding.getName());
        Assertions.assertEquals(binding.getType(), new TypeReference<Map<String, BigDecimal>>() {
        }.getType());
    }

    /**
     * Tests that a correctly configured Binding object is created.
     */
    @Test
    void testBuild() {
        String name = "bindingName";
        Type type = String.class;
        String value = "TestValue";

        BindingBuilder bindingBuilder = Binding.builder().with(name);
        bindingBuilder.type(type);
        bindingBuilder.value(value);

        Binding<String> binding = bindingBuilder.build();

        Assertions.assertEquals(name, binding.getName());
        Assertions.assertEquals(type, binding.getType());
        Assertions.assertEquals(value, binding.getValue());
        Assertions.assertTrue(binding.isEditable());
        Assertions.assertFalse(binding.isFinal());
    }

    /**
     * Test that the binding object created with a getter has the correct value.
     */
    @Test
    void testBuildWithGetter() {
        String name = "bindingName";
        Type type = String.class;
        Supplier<String> getter = () -> "TestValueGetter";

        BindingBuilder bindingBuilder = Binding.builder().with(name);
        bindingBuilder.type(type);
        bindingBuilder.value(getter);

        Binding<String> binding = bindingBuilder.build();

        Assertions.assertEquals(name, binding.getName());
        Assertions.assertEquals(type, binding.getType());
        Assertions.assertEquals(getter.get(), binding.getValue());
        Assertions.assertFalse(binding.isEditable());
        Assertions.assertFalse(binding.isFinal());
    }

    @Test
    void testGetSetValue() {
        String name = "Test";
        Type type = String.class;
        String description = "Test description";
        AtomicReference<String> initialValue = new AtomicReference<>("Initial");
        String newValue = "New";
        Supplier<String> getter = () -> initialValue.get();
        Consumer<String> setter = value -> initialValue.set(value);

        Binding<String> underTest = Binding.builder().with("Test", type)
                .delegate(getter, setter)
                .description(description)
                .build();
        Assertions.assertEquals(initialValue.get(), underTest.getValue());
        underTest.setValue(newValue);
        Assertions.assertEquals(newValue, underTest.getValue());
    }

    @Test
    void testAsImmutable() {
        String name = "name";
        Type type = String.class;
        String description = "Test description";
        String value = "Value";
        Supplier<String> getter = () -> value;

        Binding<String> underTest = Binding.builder().with(name, type)
                .delegate(getter, null)
                .description(description)
                .build();

        Binding<String> immutableBinding = underTest.asImmutable();
        Assertions.assertEquals(name, immutableBinding.getName());
        Assertions.assertEquals(type, immutableBinding.getType());
        Assertions.assertEquals(description, immutableBinding.getDescription());
        Assertions.assertEquals(getter.get(), immutableBinding.getValue());
        Assertions.assertFalse(immutableBinding.isFinal());
    }

    // Define a Type for testing purposes
    private static class DummyType implements Supplier<String> {
        @Override
        public String get() {
            return "Test Value";
        }
    }

    // Supplier for test value
    DummyType dummyType = new DummyType();
    Type type = dummyType.getClass();

    /**
     *  Test behavior of `getValue()` with non-final value.
     */
    @Test
    public void testSupplierNonFinalValue() {
        // Create supplier object
        Supplier<String> supplier = dummyType::get;
        // Instantiate with non-final value
        Binding<String> suppliedBinding = Binding.builder().with("Test", type)
                .computeIfAbsent(supplier)
                .description("Test Binding").build();
        // Assert that object was created
        Assertions.assertNotNull(suppliedBinding);
        Assertions.assertEquals(suppliedBinding.getClass(), SuppliedBinding.class);
        // Call getValue() and assert expected result
        Assertions.assertEquals("Test Value", suppliedBinding.getValue());
    }

    /**
     * Test behavior of `getValue()` with final value.
     */
    @Test
    public void testSupplierFinalValue() {
        // Create supplier object
        Supplier<String> supplier = dummyType::get;
        // Instantiate with final value
        Binding<String> suppliedBinding = Binding.builder().with("Test", type)
                .computeIfAbsent(supplier)
                .description("Test Binding").build();
        // Assert that object was created
        Assertions.assertNotNull(suppliedBinding);
        Assertions.assertEquals(suppliedBinding.getClass(), SuppliedBinding.class);
        // Call getValue() multiple times and assert expected result
        Assertions.assertEquals("Test Value", suppliedBinding.getValue());
        Assertions.assertEquals("Test Value", suppliedBinding.getValue());
    }
}
