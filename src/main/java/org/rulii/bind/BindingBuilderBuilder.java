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
package org.rulii.bind;

import org.rulii.lib.spring.util.Assert;
import org.rulii.util.TypeReference;

import java.lang.reflect.Type;

/**
 * Builder to crete a BindingBuilder.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see BindingBuilder
 */
public final class BindingBuilderBuilder {

    private static final BindingBuilderBuilder instance = new BindingBuilderBuilder();

    private BindingBuilderBuilder() {
        super();
    }

    public static BindingBuilderBuilder getInstance() {
        return instance;
    }

    /**
     * Creates a new builder with the given name/value function.
     *
     * @param declaration function with name/value.
     * @return new Binding Builder.
     */
    public BindingBuilder with(BindingDeclaration<?> declaration) {
        Assert.notNull(declaration, "declaration cannot be null.");
        BindingBuilder result = new BindingBuilder(declaration.name());
        result.value(declaration.value());
        return result;
    }

    /**
     * Creates a new builder with the Binding name.
     *
     * @param name Binding name.
     * @return new Binding Builder.
     */
    public BindingBuilder with(String name) {
        return new BindingBuilder(name);
    }

    /**
     * Creates a new builder with the Binding name and value.
     *
     * @param name Binding name.
     * @param value Binding value.
     * @return new Binding Builder.
     */
    public BindingBuilder with(String name, Object value) {
        BindingBuilder result = new BindingBuilder(name);
        result.value(value);
        return result;
    }

    /**
     * Creates a new builder with the Binding name and type.
     *
     * @param name Binding name.
     * @param type Binding type.
     * @return new Binding Builder.
     */
    public BindingBuilder with(String name, Type type) {
        BindingBuilder result = new BindingBuilder(name);
        result.type(type);
        return result;
    }

    /**
     * Creates a new builder with the Binding name and type.
     *
     * @param name Binding name.
     * @param type Binding type.
     * @param value initial value.
     * @return new Binding Builder.
     */
    public BindingBuilder with(String name, Type type, Object value) {
        return with(name, type).value(value);
    }

    /**
     * Creates a new builder with the Binding name and type.
     *
     * @param name Binding name.
     * @param type Binding type.
     * @return new Binding Builder.
     */
    public BindingBuilder with(String name, TypeReference<?> type) {
        BindingBuilder result = new BindingBuilder(name);
        result.type(type);
        return result;
    }

    /**
     * Builds new constant Binding.
     *
     * @param name Binding name.
     * @param value Binding value.
     * @return new constant Binding.
     */
    public <T> Binding<T> constant(String name, Object value) {
        return with(name, value).editable(false).build();
    }

    /**
     * Builds new final(not re-definable) Binding.
     *
     * @param name Binding name.
     * @param value Binding value.
     * @return new final Binding.
     */
    public <T> Binding<T> finalBinding(String name, Object value) {
        return with(name, value).isFinal(true).build();
    }

    /**
     * Builds new final(not re-definable) constant(value cannot change) Binding.
     *
     * @param name Binding name.
     * @param value Binding value.
     * @return new final constant Binding.
     */
    public <T> Binding<T> finalConstant(String name, Object value) {
        return with(name, value).isFinal(true).editable(false).build();
    }

    /**
     * Creates a new binding with the given name/value function.
     *
     * @param declaration function with name/value.
     * @return new Binding.
     */
    public <T> Binding<T> build(BindingDeclaration<?> declaration) {
        return with(declaration).build();
    }

    /**
     * Creates a new Binding name and value.
     *
     * @param name Binding name.
     * @param value Binding value.
     * @return new Binding.
     */
    public <T> Binding<T> build(String name, Object value) {
        return with(name, value).build();
    }

    /**
     * Creates a new Binding name and type.
     *
     * @param name Binding name.
     * @param type Binding type.
     * @return new Binding.
     */
    public <T> Binding<T> build(String name, Type type) {
        return with(name, type).build();
    }

    /**
     * Creates a new Binding name and type.
     *
     * @param name Binding name.
     * @param type Binding type.
     * @return new Binding.
     */
    public <T> Binding<T> build(String name, TypeReference<?> type) {
        return with(name, type).build();
    }
}
