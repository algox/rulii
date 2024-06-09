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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the Bindings.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class DefaultBindings implements Bindings, PromiscuousBinder {

    private static final Log logger = LogFactory.getLog(DefaultBindings.class);

    // Stores all the Bindings
    private final Map<String, Binding<?>> bindings = createBindings();
    private final List<BindingListener> listeners = new LinkedList<>();

    /**
     * Default Ctor. Self Reference added.
     */
    DefaultBindings() {
        super();
    }

    @Override
    public <T> void bind(Binding<T> binding) {
        Assert.notNull(binding, "binding cannot be null");

        // Make sure Binding Name is not reserved.
        if (ReservedBindings.isReserved(binding.getName())) {
            throw new ReservedBindingNameException(binding.getName());
        }

        promiscuousBind(binding);
    }

    @Override
    public <T> void promiscuousBind(Binding<T> binding) {
        Assert.notNull(binding, "binding cannot be null");

        // Try and put the Binding
        Binding<?> existingBinding = bindings.putIfAbsent(binding.getName(), binding);

        // Looks like we already have a binding
        if (existingBinding != null) {
            throw new BindingAlreadyExistsException(existingBinding, binding);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("New Binding created. Name [" + binding.getName() + "] Type [" + binding.getTypeName()
                    + "] Value [" + binding.getTextValue() + "]");
        }

        // Add the value listeners
        for (BindingListener listener : listeners) {
            binding.addValueListener(listener);
        }

        // Fire all the listeners
        fireBindingListeners(binding);
    }

    @Override
    public int size() {
        return bindings.size();
    }

    @Override
    public Iterator<Binding<?>> iterator() {
        return bindings.values().iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Binding<T> getBinding(String name) {
        Assert.notNull(name, "name cannot be null.");
        return (Binding<T>) bindings.get(name);
    }

    @Override
    public <T> T getValue(String name) {
        Binding<T> result = getBinding(name);
        // Could not find Binding
        if (result == null) throw new NoSuchBindingException(name);
        return result.getValue();
    }

    @Override
    public <T> void setValue(String name, T value) {
        Binding<T> result = getBinding(name);
        // Could not find Binding
        if (result == null) throw new NoSuchBindingException(name);
        result.setValue(value);
    }

    @Override
    public <T> Binding<T> getBinding(String name, Type typeRef) {
        Binding<T> result = getBinding(name);
        // Make sure it also matches the Type
        if (result == null || !result.isAssignable(typeRef)) return null;
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<Binding<T>> getBindings(Type typeRef) {
        List<Binding<T>> result = new LinkedList<>();

        for (Binding<?> binding : bindings.values()) {
            if (binding.isAssignable(typeRef)) {
                result.add((Binding<T>) binding);
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public Map<String, ?> asMap() {
        Map<String, Object> result = new HashMap<>();

        for (Binding<?> binding : this) {
            result.put(binding.getName(), binding.getValue());
        }

        return result;
    }

    @Override
    public Set<String> getNames() {
        return bindings.keySet();
    }

    /**
     * Creates the data structure to store all the Bindings.
     *
     * @return creates the data structure that will ultimately store the Bindings. Defaulted to HashMap.
     */
    protected Map<String, Binding<?>> createBindings() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public void addBindingListener(BindingListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        this.listeners.add(listener);
    }

    @Override
    public boolean removeBindingListener(BindingListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        return this.listeners.remove(listener);
    }

    protected void fireBindingListeners(Binding<?> binding) {
        Assert.notNull(binding, "binding cannot be null.");

        for (BindingListener listener : listeners) {
            listener.onBind(binding);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!Bindings.class.isAssignableFrom(o.getClass())) return false;
        Bindings other = (Bindings) o;
        return asMap().equals(other.asMap());
    }

    @Override
    public int hashCode() {
        return Objects.hash(bindings);
    }

    @Override
    public String prettyPrint(String prefix) {
        StringBuilder result = new StringBuilder(System.lineSeparator());

        for (Binding<?> binding : bindings.values()) {
            if (binding.getValue() instanceof Bindings) continue;

            result.append(prefix)
                    .append(binding.getSummary())
                    .append(System.lineSeparator());
        }

        return result.toString();
    }

    @Override
    public String toString() {
        return prettyPrint("");
    }
}
