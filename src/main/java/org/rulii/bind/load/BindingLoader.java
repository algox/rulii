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
package org.rulii.bind.load;

import org.rulii.bind.Bindings;

/**
 * Loader class that can load a collection of Bindings from a given Container.
 *
 * For example : You may want to load all the properties/fields in an object as separate Bindings.
 *
 * @param <T> input type (Bean, Map, etc)
 *
 * @author Max Arulananthan
 * @since 1.0
 */
@FunctionalInterface
public interface BindingLoader<T> {

    /**
     * Builder instance.
     *
     * @return builder.
     */
    static BindingLoaderBuilderBuilder builder() {
        return BindingLoaderBuilderBuilder.getInstance();
    }

    /**
     * Loads multiple Bindings from the given value into the given Bindings.
     *
     * @param bindings Bindings to add to.
     * @param value input value (Bean/Map etc)
     */
    void load(Bindings bindings, T value);
}
