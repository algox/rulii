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

import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.Type;
import java.util.function.Supplier;


/**
 * Represents a binding implementation that retrieves its value from a {@link Supplier}.
 * Extends the functionality of {@link DefaultBinding} by supporting dynamic value computation
 * through a supplier, while also optionally caching the value when marked as final.
 *
 * @param <T> the type of the value associated with the binding
 * @author Max Arulananthan
 * @since 1.0
 */
public class SuppliedBinding<T> extends DefaultBinding<T> {

    private static final Log logger = LogFactory.getLog(SuppliedBinding.class);

    private final Supplier<T> supplier;
    private boolean valueSet = false;
    private T value = null;

    SuppliedBinding(String name, Type type, Supplier<T> supplier,
                             boolean isFinal, boolean primary, String description) {
        super(name, type, false, isFinal, primary, description);
        Assert.notNull(supplier, "supplier cannot be null.");
        this.supplier = supplier;
    }

    @Override
    public T getValue() {
        if (!isFinal()) return supplier.get();

        if (valueSet) return value;

        synchronized (this) {
            if (valueSet) return value;
            value = supplier.get();
            valueSet = true;
        }

        return value;
    }


    @Override
    public final SuppliedBinding<T> asImmutable() {
        return this;
    }
}
