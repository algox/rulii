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

import org.rulii.lib.spring.util.Assert;
import org.rulii.util.RuleUtils;
import org.rulii.util.TypeReference;
import org.rulii.util.reflect.ReflectionUtils;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Builder class to construct Binding objects with various properties and configurations.
 * Provides a fluent API to set up the type, value, mutability, and other attributes of the Binding.
 * This builder is immutable and produces immutable Binding instances when {@link #build()} is invoked.
 */
public final class BindingBuilder {

    private final String name;
    private TypeReference<?> typeRef = null;
    private Object value = null;
    private boolean editable = true;
    private boolean isFinal = false;
    private boolean primary = false;
    private String description = null;

    private Supplier<?> getter;
    private Consumer<?> setter;

    private final List<BindingValueListener> listeners = new LinkedList<>();
    /**
     * Private Ctor taking the Binding name.
     *
     * @param name Binding name.
     */
    BindingBuilder(String name) {
        super();
        Assert.isTrue(RuleUtils.isValidName(name), "Invalid Binding name [" + name
                + "]. It must adhere to [" + RuleUtils.NAME_REGEX + "]");
        this.name = name;
    }

    /**
     * Type of the Bindings.
     *
     * @param type type of the Binding.
     * @return this for fluency.
     */
    public BindingBuilder type(Type type) {
        this.typeRef = TypeReference.with(type);
        return this;
    }

    /**
     * Type of the Bindings.
     *
     * @param typeRef type of the Binding.
     * @return this for fluency.
     */
    public BindingBuilder type(TypeReference<?> typeRef) {
        Assert.notNull(typeRef, "typeRef cannot be null");
        this.typeRef = typeRef;
        return this;
    }

    /**
     * Value of the Bindings.
     *
     * @param value value of the Binding.
     * @return this for fluency.
     */
    public BindingBuilder value(Object value) {
        this.value = value;
        if (typeRef == null) {
            // TODO : Handle dynamic proxies
            this.typeRef = value != null ?
                    TypeReference.with(value.getClass())
                    : TypeReference.with(Object.class);
        }
        return this;
    }

    /**
     * Value of the Bindings.
     *
     * @param supplier value supplier of the Binding. supplier.get() will be called immediately.
     * @param <T> generic type of the supplier.
     * @return this for fluency.
     */
    public <T> BindingBuilder value(Supplier<T> supplier) {
        Assert.notNull(supplier, "supplier cannot be null");
        this.getter = supplier;
        return this;
    }

    /**
     * Ensures a supplier is set for the 'getter' field only if it is currently absent,
     * and marks the binding as final. The supplier provided cannot be null.
     *
     * @param <T> the generic type of the supplier and corresponding binding value
     * @param supplier the supplier to compute and assign if absent
     * @return the updated BindingBuilder instance for fluent API usage
     */
    public <T> BindingBuilder computeIfAbsent(Supplier<T> supplier) {
        Assert.notNull(supplier, "supplier cannot be null");
        this.getter = supplier;
        this.isFinal = true;
        return this;
    }

    /**
     * Delegating Binding where the value is get/set using a supplier. This is useful for cases
     * where the value is retrieved from Bean using getter/setter.
     *
     * @param getter getter supplier.
     * @param setter setter supplier.
     * @param <T> type of the Binding.
     * @return this for fluency.
     */
    public <T> BindingBuilder delegate(Supplier<T> getter, Consumer<T> setter) {
        Assert.notNull(getter, "getter cannot be null");
        this.getter = getter;
        this.setter = setter;
        if (setter == null) editable(false);
        return this;
    }

    /**
     * Determines whether the Binding is editable or not.
     *
     * @param editable mutability of the Binding.
     * @return this for fluency.
     */
    public BindingBuilder editable(boolean editable) {
        this.editable = editable;
        return this;
    }

    /**
     * Determines whether the Binding is re-definable(in another scope) or not.
     *
     * @param isFinal true if re-definable; false otherwise.
     * @return this for fluency.
     */
    public BindingBuilder isFinal(boolean isFinal) {
        Assert.isTrue(setter == null, "setter must be null for final Bindings.");
        this.isFinal = isFinal;
        return this;
    }

    /**
     * Determines whether the Binding is a primary one or not.
     *
     * @param primary Primary Binding ?
     * @return this for fluency.
     */
    public BindingBuilder primary(boolean primary) {
        this.primary = primary;
        return this;
    }

    /**
     * Description of this Binding.
     *
     * @param description  Binding description
     * @return this for fluency.
     */
    public BindingBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Adds a value listener to the BindingBuilder.
     *
     * @param listener the value listener to be added
     * @return the updated BindingBuilder with the value listener
     */
    public BindingBuilder valueListener(BindingValueListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        listeners.add(listener);
        return this;
    }

    private Type getDefaultType() {
        return Object.class;
    }

    /**
     * Create a Binding with the given properties.
     * @param <T> desired Type.
     * @return new Binding.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Binding<T> build() {
        Type bindingType = typeRef != null ? typeRef.getType() : getDefaultType();
        Object bindingValue = value != null ? value : ReflectionUtils.getDefaultValue(bindingType);

        Binding<T> result;

        if (getter != null && setter != null) {
            result = new DelegatingBinding(name, bindingType, getter, setter, isFinal, primary, description);
        } else if (getter != null) {
            result = new SuppliedBinding(name, bindingType, getter, isFinal, primary, description);
        } else {
            result = new DefaultBinding(name, bindingType, bindingValue, editable, isFinal, primary, description);
        }

        // Add the listeners
        for (BindingValueListener listener : listeners) {
            result.addValueListener(listener);
        }

        return result;
    }
}
