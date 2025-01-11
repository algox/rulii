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

import org.rulii.lib.spring.util.Assert;

/**
 * Converter registry builder.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public final class ConverterRegistryBuilder {

    private final ConverterRegistry registry;

    private ConverterRegistryBuilder(ConverterRegistry registry) {
        super();
        Assert.notNull("registry", "registry cannot be null.");
        this.registry = registry;
    }

    /**
     * Creates the builder with no converters added to it.
     *
     * @return this for fluency.
     * @see DefaultConverterRegistry
     */
    public static ConverterRegistryBuilder empty() {
        return new ConverterRegistryBuilder(new DefaultConverterRegistry(false));
    }

    /**
     * Creates the builder with the standard converters added to it.
     *
     * @return this for fluency.
     * @see DefaultConverterRegistry
     */
    public static ConverterRegistryBuilder standard() {
        return new ConverterRegistryBuilder(new DefaultConverterRegistry(true));
    }

    /**
     * Adds the given converter to the registry.
     *
     * @param converter converter to be added.
     * @return this for fluency.
     * @see DefaultConverterRegistry
     */
    public ConverterRegistryBuilder converter(Converter converter) {
        Assert.notNull(converter, "converter cannot be null.");
        registry.register(converter);
        return this;
    }

    /**
     * Builds the registry.
     *
     * @return registry containing all the converters that were added.
     */
    public ConverterRegistry build() {
        return registry;
    }
}
