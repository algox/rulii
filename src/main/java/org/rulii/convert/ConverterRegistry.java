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

import java.lang.reflect.Type;

/**
 * Registry to hold all the Converters.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public interface ConverterRegistry {

    static ConverterRegistryBuilder builder() {
        return builder(true);
    }

    static ConverterRegistryBuilder builder(boolean registerDefaults) {
        return registerDefaults ? ConverterRegistryBuilder.standard() : ConverterRegistryBuilder.empty();
    }

    /**
     * Register a new Converter. It will overwrite any existing converters that have the same source/target combo.
     *
     * @param converter new converter.
     */
    void register(Converter<?, ?> converter);

    /**
     * Finds a Convert for the desired source/target types.
     *
     * @param source source type.
     * @param target target type.
     * @param <S> source generic type.
     * @param <T> target generic type.
     * @return converter if one is found; null otherwise.
     */
    <S, T> Converter<S, T> find(Type source, Type target);
}
