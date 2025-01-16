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
package org.rulii.test.convert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.convert.Converter;
import org.rulii.convert.ConverterRegistry;

import java.lang.reflect.Type;

/**
 * Test class for ConverterRegistry to verify registration and functionality of converters.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ConverterRegistryTest {

    public ConverterRegistryTest() {
        super();
    }

    // Creating a mock Converter to be used in the tests
    Converter<String, Integer> mockConverter = new Converter<>() {
        @Override
        public Type getSourceType() {
            return String.class;
        }

        @Override
        public Type getTargetType() {
            return Integer.class;
        }

        @Override
        public boolean canConvert(Type fromType, Type toType) {
            return fromType == getSourceType() && toType == getTargetType();
        }

        @Override
        public Integer convert(String value, Type toType) {
            return Integer.valueOf(value);
        }
    };


    /**
     * This test verifies that a converter can be instantiated and registered successfully.
     */
    @Test
    public void registerConverter_whenCalledWithValidConverter_shouldSucceed() {
        // Given
        ConverterRegistry converterRegistry = ConverterRegistry.builder(false).build();

        // When
        converterRegistry.register(mockConverter);

        // Then
        Converter<String, Integer> actualConverter = converterRegistry.find(String.class, Integer.class);
        Assertions.assertNotNull(actualConverter);
        Assertions.assertEquals(actualConverter, mockConverter);
    }

    /**
     * This test verifies that registration of a converter overwrites any existing converters that
     * have the same source/target combo.
     */
    @Test
    public void registerConverter_whenCalledTwiceWithSameConverter() {
        // Given
        ConverterRegistry converterRegistry = ConverterRegistry.builder(false).build();
        converterRegistry.register(mockConverter);

        Converter<String, Integer> mockConverter2 = new Converter<>() {
            @Override
            public Type getSourceType() {
                return String.class;
            }

            @Override
            public Type getTargetType() {
                return Integer.class;
            }

            @Override
            public boolean canConvert(Type fromType, Type toType) {
                return fromType == getSourceType() && toType == getTargetType();
            }

            @Override
            public Integer convert(String value, Type toType) {
                return Integer.parseInt(value) * 2;
            }
        };

        // When
        converterRegistry.register(mockConverter2);

        // Then
        Converter<String, Integer> actualConverter = converterRegistry.find(String.class, Integer.class);
        Assertions.assertNotNull(actualConverter);
        Assertions.assertEquals(actualConverter, mockConverter);
    }
}

