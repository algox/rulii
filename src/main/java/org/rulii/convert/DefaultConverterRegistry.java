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
package org.rulii.convert;

import org.rulii.convert.text.TextToByteConverter;
import org.rulii.convert.text.TextToDateConverter;
import org.rulii.convert.text.TextToEnumConverter;
import org.rulii.convert.text.TextToUrlConverter;
import org.rulii.convert.text.TextToLocalDateConverter;
import org.rulii.convert.text.TextToBigDecimalConverter;
import org.rulii.convert.text.TextToBigIntegerConverter;
import org.rulii.convert.text.TextToBooleanConverter;
import org.rulii.convert.text.TextToCharsetConverter;
import org.rulii.convert.text.TextToCurrencyConverter;
import org.rulii.convert.text.TextToDoubleConverter;
import org.rulii.convert.text.TextToFloatConverter;
import org.rulii.convert.text.TextToIntegerConverter;
import org.rulii.convert.text.TextToLocalDateTimeConverter;
import org.rulii.convert.text.TextToLongConverter;
import org.rulii.convert.text.TextToShortConverter;
import org.rulii.convert.text.TextToUUIDConverter;
import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the ConverterRegistry.
 *
 * @author Max Arulananthan.
 * @since 1.0
 */
public class DefaultConverterRegistry implements ConverterRegistry {

    private final List<Converter<?, ?>> converters = Collections.synchronizedList(new ArrayList<>());
    private final Map<ConverterKey, Converter<?, ?>> converterCache = new ConcurrentHashMap();

    /**
     * Ctor to create a new ConverterRegistry.
     *
     * @param registerDefaults register all the standard converters.
     */
    public DefaultConverterRegistry(boolean registerDefaults) {
        super();
        if (registerDefaults) init();
    }

    /**
     * Register a new Converter. It will overwrite any existing converters that have the same source/target combo.
     *
     * @param converter new converter.
     */

    public void register(Converter<?, ?> converter) {
        Assert.notNull(converter, "Converter cannot be null.");
        converters.add(converter);
    }

    /**
     * Finds a Convert for the desired source/target types.
     *
     * @param source source type.
     * @param target target type.
     * @param <S> source generic type.
     * @param <T> target generic type.
     * @return converter if one is found; null otherwise.
     */
    public <S, T> Converter<S, T> find(Type source, Type target) {
        Converter result = converterCache.get(new ConverterKey(source, target));

        if (result != null) return result;

        int size = converters.size();

        for (int i = size - 1; i >=0; i--) {
            if (converters.get(i).canConvert(source, target)) {
                result = converters.get(i);
                break;
            }
        }

        // TODO : Dont' cache if source or target is Object.class
        if (result != null) converterCache.put(new ConverterKey(source, target), result);

        return result;
    }

    private static class ConverterKey {
        private final Type sourceType;
        private final Type targetType;

        public ConverterKey(Type sourceType, Type targetType) {
            super();
            Assert.notNull(sourceType, "sourceType cannot be null.");
            Assert.notNull(targetType, "targetType cannot be null.");
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConverterKey that = (ConverterKey) o;
            return sourceType.equals(that.sourceType) &&
                    targetType.equals(that.targetType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceType, targetType);
        }
    }

    private void init() {
        register(new TextToBigDecimalConverter());
        register(new TextToBigIntegerConverter());
        register(new TextToBooleanConverter());
        register(new TextToByteConverter());
        register(new TextToDoubleConverter());
        register(new TextToFloatConverter());
        register(new TextToIntegerConverter());
        register(new TextToLongConverter());
        register(new TextToShortConverter());
        register(new TextToEnumConverter());
        register(new TextToLocalDateConverter());
        register(new TextToLocalDateTimeConverter());
        register(new TextToDateConverter());
        register(new TextToUrlConverter());
        register(new TextToCharsetConverter());
        register(new TextToCurrencyConverter());
        register(new TextToUrlConverter());
        register(new TextToUUIDConverter());
    }

    @Override
    public String toString() {
        return "DefaultConverterRegistry{" +
                "converters=" + converters +
                ", converterCache=" + converterCache +
                '}';
    }
}
