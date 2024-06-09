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
package org.rulii.util;

import org.rulii.model.UnrulyException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Reference to a Generic Type. This class is used to capture the Generic Type at runtime.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public abstract class TypeReference<T> {

    private final Type type;

    protected TypeReference() {
        super();
        this.type = captureType();
    }

    private TypeReference(Type type) {
        super();
        this.type = type;
    }

    /**
     * Returns the captured/underlying Type.
     *
     * @return underlying type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Captures the generic type at runtime.
     *
     * @return The captured type.
     * @throws UnrulyException if the superclass is not a parameterized type.
     */
    private Type captureType() {
        Type superclass = getClass().getGenericSuperclass();

        if (!(superclass instanceof ParameterizedType)) {
            throw new UnrulyException("SimpleBinding must have a parameterized type. Ex: new SimpleBinding<String>();");
        }

        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /**
     * Static way to defaultBindings a Type Reference to an existing Type.
     *
     * @param type existing Type.
     * @param <T> desired Generic Type.
     * @return Generic Type reference.
     */
    public static <T> TypeReference<T> with(Type type) {
        return new TypeReference<T>(type){};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeReference<?> that = (TypeReference<?>) o;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
