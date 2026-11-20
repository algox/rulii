/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
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
import org.rulii.context.RuleContext;
import org.rulii.convert.Converter;
import org.rulii.convert.ConverterRegistry;
import org.rulii.convert.DefaultConverterRegistry;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    public void registerConverter_whenCalledTwiceWithSameSourceTarget_overwritesExisting() {
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
        Assertions.assertEquals(actualConverter, mockConverter2);
    }

    // The types the built-in converters turn text into.
    private static final Type[] BUILT_IN_TARGETS = {Integer.class, Long.class, TimeUnit.class, Double.class, Float.class,
            LocalDate.class, LocalDateTime.class, Date.class, URL.class, BigDecimal.class, Boolean.class, Currency.class,
            UUID.class, Byte.class, Short.class, Charset.class, BigInteger.class, String.class};

    // A converter of the very types of the built-in text-to-Integer converter.
    private static Converter<CharSequence, Integer> textToIntegerOfMyOwn() {
        return new Converter<>() {
            @Override
            public Type getSourceType() {
                return CharSequence.class;
            }

            @Override
            public Type getTargetType() {
                return Integer.class;
            }

            @Override
            public boolean canConvert(Type fromType, Type toType) {
                return fromType == String.class && toType == Integer.class;
            }

            @Override
            public Integer convert(CharSequence value, Type toType) {
                return -1;
            }
        };
    }

    /**
     * The built-in converters are created once and registered on every registry; each registry keeps a list of
     * its own, so what is registered on one is not seen by another.
     */
    @Test
    public void defaultRegistries_shareTheBuiltInConverters_butNotWhatIsRegisteredOnThem() {
        ConverterRegistry first = ConverterRegistry.builder().build();
        ConverterRegistry second = ConverterRegistry.builder().build();

        for (Type target : BUILT_IN_TARGETS) {
            Assertions.assertNotNull(first.find(String.class, target), "no converter to " + target);
            Assertions.assertSame(first.find(String.class, target), second.find(String.class, target));
        }

        Converter<Object, Object> builtIn = second.find(String.class, Integer.class);
        Converter<CharSequence, Integer> mine = textToIntegerOfMyOwn();
        first.register(mine);

        // Replaced on the first registry only.
        Assertions.assertSame(mine, first.find(String.class, Integer.class));
        Assertions.assertSame(builtIn, second.find(String.class, Integer.class));
        Assertions.assertSame(builtIn, ConverterRegistry.builder().build().find(String.class, Integer.class));
    }

    /**
     * Registering the defaults on an empty registry gives what a default registry has.
     */
    @Test
    public void registerDefaults_onAnEmptyRegistry_isADefaultRegistry() {
        DefaultConverterRegistry registry = new DefaultConverterRegistry(false);
        Assertions.assertNull(registry.find(String.class, Integer.class));

        registry.registerDefaults();
        ConverterRegistry standard = ConverterRegistry.builder().build();

        for (Type target : BUILT_IN_TARGETS) {
            Assertions.assertSame(standard.find(String.class, target), registry.find(String.class, target));
        }

        Converter<String, Integer> toInteger = registry.find(String.class, Integer.class);
        Assertions.assertEquals(42, toInteger.convert("42", Integer.class).intValue());
    }

    /**
     * A converter registered before the defaults is found before them (the first match wins), and the defaults
     * still cover the rest: the order rulii-spring registers in.
     */
    @Test
    public void registerDefaults_afterAConverterOfMyOwn_keepsMineFirst() {
        DefaultConverterRegistry registry = new DefaultConverterRegistry(false);
        registry.register(mockConverter);

        registry.registerDefaults();

        Assertions.assertSame(mockConverter, registry.find(String.class, Integer.class));

        ConverterRegistry standard = ConverterRegistry.builder().build();
        for (Type target : BUILT_IN_TARGETS) {
            if (target == Integer.class) continue;
            Assertions.assertSame(standard.find(String.class, target), registry.find(String.class, target));
        }
    }

    /**
     * A converter registered on one context's registry is not seen by another context.
     */
    @Test
    public void aConverterRegisteredOnOneContext_isNotSeenByAnother() {
        RuleContext first = RuleContext.builder().build();
        RuleContext second = RuleContext.builder().build();
        Assertions.assertNotSame(first.getConverterRegistry(), second.getConverterRegistry());

        Converter<CharSequence, Integer> mine = textToIntegerOfMyOwn();
        first.getConverterRegistry().register(mine);

        Assertions.assertSame(mine, first.getConverterRegistry().find(String.class, Integer.class));
        Assertions.assertNotSame(mine, second.getConverterRegistry().find(String.class, Integer.class));
    }
}

