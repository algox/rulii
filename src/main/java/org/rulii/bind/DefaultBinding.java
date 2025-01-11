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
import org.rulii.util.RuleUtils;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Binding is a mapping between a name and a value.
 *
 * @param <T> generic type of the Binding.
 *
 * @author Max Arulananthan
 * @since 1.0
 * @see Binding
 */
public class DefaultBinding<T> implements Binding<T> {

    private static final Log logger = LogFactory.getLog(DefaultBinding.class);

    private final String id;
    private final String name;
    private final Type type;
    private T value;
    private final boolean primary;
    // Cannot be final as change it to editable = false after we set the value in the ctor
    private boolean editable;
    private final boolean isFinal;
    private final String description;

    private final List<BindingValueListener> listeners = new LinkedList<>();
    /**
     * Creates a new DefaultBinding
     *
     * @param name name of the Binding.
     * @param type Type of the Binding.
     * @param isFinal determines whether this Binding is re-definable in another scope.
     * @param primary determines whether this Binding is a Primary candidate or not.
     */
    DefaultBinding(String name, Type type, boolean isFinal, boolean primary, String description) {
        super();
        Assert.notNull(name, "name cannot be null");
        Assert.notNull(type, "type cannot be null");
        Assert.isTrue(!name.trim().isEmpty(), "name length must be > 0");
        Assert.isTrue(RuleUtils.isValidName(name), "Binding name [" + name + "] must match [" + RuleUtils.NAME_REGEX + "]");
        Assert.isTrue(!name.trim().isEmpty(), "name length must be > 0");
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.editable = true;
        this.isFinal = isFinal;
        this.primary = primary;
        this.description = description;
    }

    /**
     * Creates a new DefaultBinding
     *
     * @param name name of the Binding.
     * @param type Type of the Binding.
     * @param editable determines whether this Binding is editable or not.
     * @param isFinal determines whether this Binding is re-definable in another scope.
     * @param primary determines whether this Binding is a Primary candidate or not.
     */
    DefaultBinding(String name, Type type, boolean editable, boolean isFinal, boolean primary, String description) {
        this(name, type, isFinal, primary, description);
        this.editable = editable;
    }

    /**
     * Creates a new DefaultBinding
     *
     * @param name name of the Binding.
     * @param type Type of the Binding.
     * @param value initial value of the Binding.
     * @param editable determines whether this Binding is editable or not.
     * @param isFinal determines whether this Binding is re-definable in another scope.
     * @param primary determines whether this Binding is a Primary candidate or not.
     */
    DefaultBinding(String name, Type type, T value, boolean editable, boolean isFinal, boolean primary, String description) {
        this(name, type, isFinal, primary, description);
        setValue(value);
        this.editable = editable;
    }

    @Override
    public void addValueListener(BindingValueListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        this.listeners.add(listener);
    }

    @Override
    public boolean removeValueListener(BindingValueListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        return this.listeners.remove(listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(T value) {

        // Make sure we can edit this value
        if (!isEditable()) {
            throw new InvalidBindingException("Attempting to change a immutable Binding [" + name + "]");
        }

        setValueInternal(value);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isPrimary() {
        return primary;
    }

    protected void setValueInternal(T value) {
        if (logger.isDebugEnabled()) {
            logger.debug("Binding change. Old Value [" + this.value +"] new Value [" + value + "]");
        }

        // Looks like they are passing us a wrong value type
        if (value != null && !isTypeAcceptable(value.getClass())) {
            throw new InvalidBindingException(name, type, value);
        }

        Object oldValue = this.value;
        this.value = value;

        fireListeners(oldValue, value);
    }

    protected void fireListeners(Object oldValue, Object newValue) {
        // Fire change listeners
        for (BindingValueListener listener : listeners) {
            listener.onChange(this, oldValue, newValue);
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getTypeName() {
        if (type == null) return null;
        if (type instanceof Class) return ((Class<?>) type).getSimpleName();
        return type.getTypeName();
    }

    @Override
    public String getTypeAndName() {
        return getTypeName() + " " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Binding<?> that = (Binding<?>) o;
        return name.equals(that.getName()) &&
                type.equals(that.getType()) &&
                Objects.equals(value, that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, value);
    }

    @Override
    public String getSummary() {
        return "(" + getTypeAndName() + " = " + value + ")";
    }

    @Override
    public String toString() {
        return "(Binding : name = " + name +
                ", type = " + getTypeName() +
                ", value = " + RuleUtils.getSummaryTextValue(value) + ")";
    }
}
