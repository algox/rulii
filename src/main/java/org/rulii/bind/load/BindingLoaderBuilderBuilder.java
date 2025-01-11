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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Convenient way to build a new BindingLoaderBuilder.
 *
 * @author Max Arulananthan.
 * @since 1.0
 */
public class BindingLoaderBuilderBuilder {

    private static final BindingLoaderBuilderBuilder INSTANCE = new BindingLoaderBuilderBuilder();

    private BindingLoaderBuilderBuilder() {
        super();
    }

    /**
     * Singleton instance.
     *
     * @return this instance.
     */
    public static BindingLoaderBuilderBuilder getInstance() {
        return INSTANCE;
    }

    /**
     * Property based binding loader.
     *
     * @param <T> container type.
     * @return property based binding loader.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> BindingLoaderBuilder<PropertyDescriptor, T> propertyLoaderBuilder() {
        return new BindingLoaderBuilder(new PropertyBindingLoader());
    }

    /**
     * Field based binding loader.
     *
     * @param <T> container type.
     * @return field based binding loader.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> BindingLoaderBuilder<Field, T> fieldLoaderBuilder() {
        return new BindingLoaderBuilder(new FieldBindingLoader());
    }

    /**
     * Map based binding loader.
     *
     * @return map based binding loader.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BindingLoaderBuilder<String, Map<String, Object>> mapLoaderBuilder() {
        return new BindingLoaderBuilder(new MapBindingLoader());
    }
}
