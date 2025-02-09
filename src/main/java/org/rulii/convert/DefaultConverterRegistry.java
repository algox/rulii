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
package org.rulii.convert;

import org.rulii.convert.text.*;
import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of the ConverterRegistry.
 *
 * @author Max Arulananthan.
 * @since 1.0
 */
public class DefaultConverterRegistry implements ConverterRegistry {

    private final List<Converter<?, ?>> converters = Collections.synchronizedList(new ArrayList<>());

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

    public boolean register(Converter<?, ?> converter) {
        Assert.notNull(converter, "Converter cannot be null.");
        Assert.notNull(converter.getSourceType(), "Source Type cannot be null.");
        Assert.notNull(converter.getTargetType(), "Target Type cannot be null.");
        if (converters.contains(converter)) return false;
        return converters.add(converter);
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
    @SuppressWarnings("unchecked")
    public <S, T> Converter<S, T> find(Type source, Type target) {
        Converter<S, T> result = null;

        for (Converter<?, ?> converter : converters) {
            if (converter.canConvert(source, target)) {
                result = (Converter<S, T>) converter;
                break;
            }
        }

        return result;
    }

    private void init() {
        register(new TextToIntegerConverter());
        register(new TextToLongConverter());
        register(new TextToEnumConverter());
        register(new TextToDoubleConverter());
        register(new TextToFloatConverter());
        register(new TextToLocalDateConverter());
        register(new TextToLocalDateTimeConverter());
        register(new TextToDateConverter());
        register(new TextToUrlConverter());
        register(new TextToUrlConverter());
        register(new TextToBigDecimalConverter());
        register(new TextToBooleanConverter());
        register(new TextToCurrencyConverter());
        register(new TextToUUIDConverter());
        register(new TextToByteConverter());
        register(new TextToShortConverter());
        register(new TextToCharsetConverter());
        register(new TextToBigIntegerConverter());
        register(new TextToStringConverter());
    }

    @Override
    public String toString() {
        return "DefaultConverterRegistry{" +
                "converters=" + converters +
                '}';
    }
}
