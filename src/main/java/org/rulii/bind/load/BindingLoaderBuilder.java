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
package org.rulii.bind.load;

import org.rulii.lib.spring.util.Assert;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Binding Loader Builder.
 *
 * @param <S> container item type.
 * @param <T> container type.
 *
 * @author Max Arulananthan.
 * @since 1.0
 */
public class BindingLoaderBuilder<S, T> {

    private final AbstractBindingLoader<S, T> loader;

    public BindingLoaderBuilder(AbstractBindingLoader<S, T> loader) {
        super();
        Assert.notNull(loader, "loader cannot be null.");
        this.loader = loader;
    }

    /**
     * Function use to change the output BindingName,
     *
     * @param nameGenerator Function that output the BindingName,
     *
     * @return this for fluency.
     */
    public BindingLoaderBuilder<S, T> nameGenerator(Function<S, String> nameGenerator) {
        loader.setNameGenerator(nameGenerator);
        return this;
    }

    /**
     * Filter to restrict which items in the container become Bindings.
     *
     * @param filter restricting Filter.
     *
     * @return this for fluency.
     */
    public BindingLoaderBuilder<S, T> filter(Predicate<S> filter) {
        loader.setFilter(filter);
        return this;
    }

    /**
     * Array of container item names that won't get added.
     *
     * @param itemNames blacklisted item names.
     *
     * @return this for fluency.
     */
    public BindingLoaderBuilder<S, T> ignored(String...itemNames) {
        loader.setIgnoreItems(itemNames);
        return this;
    }

    /**
     * Array of item names that will get added.
     *
     * @param itemNames whitelisted item itemNames.
     *
     * @return this for fluency.
     */
    public BindingLoaderBuilder<S, T> include(String...itemNames) {
        loader.setIncludeItems(itemNames);
        return this;
    }

    /**
     * Returns a customized Binding Loader.
     *
     * @return customized BindingLoader.
     */
    public BindingLoader<T> build() {
        return loader;
    }
}
