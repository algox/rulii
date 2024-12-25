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
package org.rulii.bind;

import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Delegates the value get/set functions. This is used for cases where the value resides inside a Bean. The Binding
 * does not hold the actual value (as it may change) and delegates the retrieval and saving to the getter/setter.
 *
 * @param <T> Binding Type.
 * @author Max Arulananthan
 * @since 1.0
 */
public class DelegatingBinding<T> extends DefaultBinding<T> {

    private static final Log logger = LogFactory.getLog(DelegatingBinding.class);

    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public DelegatingBinding(String name, Type type, Supplier<T> getter, Consumer<T> setter,
                             boolean isFinal, boolean primary, String description) {
        super(name, type, setter != null, isFinal, primary, description);
        Assert.notNull(getter, "getter cannot be null.");
        Assert.isTrue(!isFinal, "Delegating Bindings cannot be final.");
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public T getValue() {
        return getter.get();
    }

    @Override
    protected void setValueInternal(T value) {
        if (setter != null) {

            if (logger.isDebugEnabled()) {
                logger.debug("Binding change. New Value [" + value + "]");
            }

            Object oldValue = getValue();
            setter.accept(value);
            fireListeners(oldValue, value);
        }
    }

    @Override
    public final DelegatingBinding<T> asImmutable() {
        return new DelegatingBinding<>(getName(), getType(), getter, null, isFinal(), isPrimary(), getDescription());
    }
}
