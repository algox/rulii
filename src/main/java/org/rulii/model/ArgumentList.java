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

import org.rulii.lib.spring.util.Assert;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;

/**
 * The resolved arguments of one method call: an unmodifiable list over the array the parameter resolver filled.
 * The engine hands that array to the method executor as it is, instead of copying the values out of a list again
 * for every call.
 *
 * <p>The list takes the array over: whoever wraps an array must not keep or change it afterwards. Reading is all
 * anyone else can do; {@link #toArray()} returns a copy. Values may be {@code null}.
 *
 * @author Max Arulananthan
 * @since 2.3
 */
public final class ArgumentList extends AbstractList<Object> implements RandomAccess, Serializable {

    private static final long serialVersionUID = 1L;

    private final Object[] values;

    private ArgumentList(Object[] values) {
        super();
        this.values = values;
    }

    /**
     * Wraps the given values without copying them.
     *
     * @param values the argument values, in parameter order; the caller gives the array up.
     * @return an unmodifiable list over the values.
     */
    public static ArgumentList wrap(Object[] values) {
        Assert.notNull(values, "values cannot be null.");
        return new ArgumentList(values);
    }

    @Override
    public Object get(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(values, values.length, Object[].class);
    }

    /**
     * The values themselves, for the engine to pass on to the method executor. Never handed out further.
     *
     * @return the wrapped array.
     */
    Object[] values() {
        return values;
    }
}
