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
package org.rulii.model;

import org.rulii.model.function.Function;

import java.util.Objects;

/**
 * Declares an input a RuleSet or RuleFlow expects to find in its bindings.
 *
 * <p>Two parameters are equal when their names are equal.
 *
 * @param name         binding name.
 * @param type         expected type.
 * @param required     whether the binding must exist.
 * @param defaultValue supplies the value when the binding is absent; null when there is no default.
 * @param description  what the parameter is for; null when not given.
 * @param <T>          parameter type.
 * @author Max Arulananthan
 * @since 2.0
 */
public record InputParameter<T>(String name, Class<T> type, boolean required, Function<T> defaultValue,
                                String description) {

    /**
     * Creates a parameter without a description.
     *
     * @param name         binding name.
     * @param type         expected type.
     * @param required     whether the binding must exist.
     * @param defaultValue supplies the value when the binding is absent; may be null.
     */
    public InputParameter(String name, Class<T> type, boolean required, Function<T> defaultValue) {
        this(name, type, required, defaultValue, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputParameter<?> that = (InputParameter<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "InputParameter{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", required=" + required +
                ", defaultValue=" + defaultValue +
                ", description='" + description + '\'' +
                '}';
    }
}
